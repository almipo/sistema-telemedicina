"use strict";
const $ = id => document.getElementById(id);
const cfg = window.APP_CONFIG || {};
const base = String(cfg.API_BASE_URL || "http://localhost:8080").replace(/\/$/, "");
const metricasBase = String(cfg.METRICAS_API_URL || "http://localhost:8081").replace(/\/$/, "");
const alertasBase = String(
    cfg.ALERTAS_API_URL || "http://localhost:8082"
).replace(/\/$/, "");
const wsAddress = String(cfg.ALERTAS_WS_URL || "ws://localhost:8082/ws/alertas");
const definitions = {
    pacientes: {
        name: "Pacientes",
        path: "pacientes",
        fields: {
            nombre: "text",
            apellido: "text",
            documento: "text",
            fechaNacimiento: "date",
            telefono: "text",
            email: "email"
        }
    },
    medicos: {
        name: "Médicos",
        path: "medicos",
        fields: {
            nombre: "text",
            apellido: "text",
            documento: "text",
            fechaNacimiento: "date",
            matricula: "text",
            especialidad: "text",
            telefono: "text",
            email: "email"
        }
    },
    turnos: {
        name: "Turnos",
        path: "turnos",
        fields: {
            fecha: "date",
            hora: "time",
            estado: "turno-estados",
            pacienteId: "pacientes",
            medicoId: "medicos"
        }
    },
    dispositivos: {
        name: "Dispositivos",
        path: "dispositivos",
        fields: {
            tipo: [
                "PULSOMETRO",
                "TERMOMETRO",
                "TENSIOMETRO",
                "MONITOR_MULTIPARAMETRICO"
            ],
            numeroSerie: "text",
            estado: ["ACTIVO", "INACTIVO"],
            nombre: "text",
            pacienteId: "pacientes"
        }
    },
    historias: {
        name: "Historias clínicas",
        path: "historias",
        fields: {pacienteId: "pacientes", estado: ["ACTIVO", "ARCHIVADO"]}
    }
};
const labels = {
    nombre: "Nombre",
    apellido: "Apellido",
    documento: "Documento",
    fechaNacimiento: "Fecha de nacimiento",
    telefono: "Teléfono",
    email: "Correo electrónico",
    matricula: "Matrícula",
    especialidad: "Especialidad",
    numeroSerie: "Número de serie",
    pacienteId: "Paciente",
    medicoId: "Médico",
    historiaClinicaId: "Historia clínica",
    fecha: "Fecha",
    hora: "Hora",
    estado: "Estado",
    tipo: "Tipo",
    valor: "Valor",
    nombreMedicacion: "Medicación"
};
const columns = {
    pacientes: ["nombre", "apellido", "documento", "email"],
    medicos: ["nombre", "apellido", "matricula", "especialidad"],
    turnos: ["fecha", "hora", "estado", "pacienteId", "medicoId"],
    dispositivos: ["nombre", "tipo", "numeroSerie", "estado"],
    historias: ["estado"]
};
let active = "inicio", patient = null, patientTab = "general", cache = {}, socket = null, socketRetry = null,
    unread = 0, editing = null, session = 0;

function el(tag, text, cls) {
    const n = document.createElement(tag);
    if (text !== undefined && text !== null) n.textContent = String(text);
    if (cls) n.className = cls;
    return n;
}

function replace(id, ...nodes) {
    $(id).replaceChildren(...nodes);
}

function inform(text) {
    $("notice").textContent = text || "";
}

function status(id, msg, ok) {
    const n = $(id);
    n.textContent = msg;
    n.classList.toggle("online", ok);
    n.classList.toggle("offline", !ok);
}

async function api(path, method = "GET", body) {
    const response = await fetch(`${base}/api/${path}`, {
        method,
        headers: {Accept: "application/json", ...(body !== undefined ? {"Content-Type": "application/json"} : {})}, ...(body !== undefined ? {body: JSON.stringify(body)} : {})
    });
    let data = null;
    const text = await response.text();
    if (text) {
        try {
            data = JSON.parse(text);
        } catch {
            data = text;
        }
    }
    if (!response.ok) {
        let reason = typeof data === "string" ? data : data?.detail || data?.message || data?.error || data?.detalles?.join("; ") || "Error de solicitud";
        throw Error(`HTTP ${response.status}: ${String(reason).slice(0, 220)}`);
    }
    return data;
}

function nameOf(x) {
    return [x?.nombre, x?.apellido].filter(Boolean).join(" ") || x?.numeroSerie || String(x?.id || "—");
}

function relation(kind, id) {
    const found = (cache[kind] || []).find(x => String(x.id) === String(id));
    return found ? nameOf(found) : String(id || "—");
}


function htmlField(key, type, value = "", options = {}) {
 const wrap = el("div", null, "field");
 const lab = el("label", labels[key] || key);
 const id = `f-${key}-${Math.random().toString(36).slice(2, 8)}`;
 lab.htmlFor = id;

 let input;

 if (Array.isArray(type) || definitions[type] || type === "turno-estados") {
  input = el("select");
  input.append(new Option("Seleccionar...", ""));

  const opciones = Array.isArray(type)
      ? type.map(v => [v, v])
      : type === "turno-estados"
          ? (cache.turnoEstados || []).map(v => [
           v,
           v.replaceAll("_", " ")
          ])
          : (cache[type] || []).map(x => [
           x.id,
           nameOf(x)
          ]);

  opciones.forEach(([v, t]) => {
   input.append(new Option(t, v));
  });

 } else if (type === "textarea") {
  input = el("textarea");
 } else {
  input = el("input");
  input.type = type;

  if (type === "number") {
   input.step = "any";
  }
 }

 input.id = id;
 input.name = key;
 input.value = value ?? "";

 input.required = !(
     ["telefono"].includes(key) ||
     (key === "fechaNacimiento" &&
         type === "date" &&
         options.medicoOptional)
 );

 if (options.readonly) {
  input.disabled = true;
  input.required = false;
 }

 wrap.append(lab, input);
 return wrap;
}


function collect(form, schema) {
    const data = {};
    for (const [key, type] of Object.entries(schema)) {
        const input = form.elements.namedItem(key);
        if (!input) continue;
        const raw = input.value;
        if (raw === "") continue;
        data[key] = type === "number" ? Number(raw) : raw;
    }
    return data;
}

function formFor(kind, item, params = {}) {
    const conf = definitions[kind], form = el("form");
    const schema = conf.fields;
    for (const [key, type] of Object.entries(schema)) form.append(htmlField(key, type, params.fixed?.[key] ?? item?.[key] ?? "", {
        readonly: key in (params.fixed || {}),
        medicoOptional: kind === "medicos"
    }));
    const bar = el("div", null, "actions");
    const save = el("button", item ? "Guardar cambios" : "Crear registro");
    save.type = "submit";
    bar.append(save);
    if (params.cancel) {
        const cancel = el("button", "Cancelar", "secondary");
        cancel.type = "button";
        cancel.onclick = params.cancel;
        bar.append(cancel);
    }
    form.append(bar);
    form.onsubmit = async e => {
        e.preventDefault();
        const data = collect(form, schema);
        Object.assign(data, params.fixed || {});
        try {
            await api(conf.path + (item ? `/${item.id}` : ""), item ? "PUT" : "POST", data);
            inform("Registro guardado correctamente.");
            if (params.onSaved) await params.onSaved(); else await load();
        } catch (err) {
            inform(err.message);
        }
    };
    return form;
}

async function getDeps() {
    const kinds = ["pacientes", "medicos"];
    await Promise.all(kinds.map(async kind => {
        try {
            cache[kind] = await api(definitions[kind].path) || [];
        } catch {
            cache[kind] = [];
        }
    }));
}

async function cargarEstadosTurno() {
 const estados = await api("turnos/estados");

 if (!Array.isArray(estados) ||
     !estados.length ||
     !estados.every(x => typeof x === "string")) {
  throw new Error(
      "El backend no devolvió estados válidos."
  );
 }

 cache.turnoEstados = estados;
}

function cellValue(k, v) {
    if (k === "pacienteId") return relation("pacientes", v);
    if (k === "medicoId") return relation("medicos", v);
    return v == null ? "—" : typeof v === "object" ? JSON.stringify(v) : String(v);
}

function makeTable(items, keys, actions) {
    const wrap = el("div", null, "table-wrap");
    if (!items.length) {
        wrap.append(el("p", "No hay registros disponibles.", "empty"));
        return wrap;
    }
    const table = el("table"), thead = el("thead"), tr = el("tr");
    keys.forEach(key => tr.append(el("th", labels[key] || key)));
    if (actions) tr.append(el("th", "Acciones"));
    thead.append(tr);
    table.append(thead);
    const tbody = el("tbody");
    items.forEach(item => {
        const row = el("tr");
        keys.forEach(key => row.append(el("td", cellValue(key, item[key]))));
        if (actions) {
            const td = el("td"), bar = el("div", null, "row-actions");
            actions(item, bar);
            td.append(bar);
            row.append(td);
        }
        tbody.append(row);
    });
    table.append(tbody);
    wrap.append(table);
    return wrap;
}

function button(label, fn, style = "secondary") {
    const b = el("button", label, style);
    b.type = "button";
    b.onclick = fn;
    return b;
}

async function remove(kind, id, after) {
    if (!confirm("¿Eliminar este registro? Esta acción no se puede deshacer.")) return;
    try {
        await api(`${definitions[kind].path}/${id}`, "DELETE");
        inform("Registro eliminado.");
        await (after ? after() : load());
    } catch (e) {
        inform(e.message);
    }
}

async function loadStandard(kind, version) {
    const conf = definitions[kind];
    $("section-title").textContent = `${conf.name} · ${editing ? "Editar" : "Nuevo registro"}`;
    $("records").textContent = "Cargando...";

 await getDeps();
 if (version !== session) return;

 if (kind === "turnos") {
  try {
   await cargarEstadosTurno();
  } catch (e) {
   if (version !== session) return;

   replace("form", formFor(kind, editing, {
    cancel: editing ? () => {
     editing = null;
     load();
    } : null
   }));

   inform(
       "Verificá que el backend exponga GET /api/turnos/estados."
   );
   return;
  }
 }

 if (version !== session) return;

 replace("form", formFor(kind, editing, {
    cancel: editing ? () => { editing = null; load(); } : null
 }));
    try {
        const data = await api(conf.path) || [];
        if (version !== session) return;
        cache[kind] = data;
        $("record-count").textContent = `${data.length} registros`;
        const actions = (item, bar) => {
            if (kind === "pacientes") bar.append(button("Ver ficha", () => openPatient(item)));
            bar.append(button("Editar", () => {
                editing = item;
                load();
            }));
            bar.append(button("Eliminar", () => remove(kind, item.id), "danger"));
        };
        replace("records", makeTable(data, columns[kind], actions));
        status("connection", "Backend conectado", true);
    } catch (e) {
        if (version !== session) return;
        replace("records", el("p", "No se pudieron consultar los registros.", "error-line"));
        inform(e.message);
        status("connection", "Backend sin conexión", false);
    }
}

function navigate(to) {
    session++;
    active = to;
    editing = null;
    patient = null;
    inform("");
    document.querySelectorAll("#tabs button").forEach(b => b.classList.toggle("active", b.dataset.tab === to));
    $("home").hidden = to !== "inicio";
    $("workspace").hidden = to === "inicio" || to === "paciente";
    $("patient-view").hidden = to !== "paciente";
    $("page-heading").textContent = to === "inicio" ? "Inicio" : to === "paciente" ? "Ficha del paciente" : to === "alertas" ? "Alertas" : to === "umbrales" ? "Umbrales" : definitions[to]?.name || "";
    $("page-description").textContent = to === "inicio" ? "Resumen de la atención y de las alertas recientes." : to === "paciente" ? "Información clínica organizada por paciente." : to === "alertas" ? "Seguimiento de eventos clínicos y notificaciones en tiempo real." : to === "umbrales" ? "Consulta y ajuste de los rangos generales de evaluación de mediciones." : "Administración y consulta de registros.";
    if (to === "alertas") {
        unread = 0;
        $("unread").hidden = true;
    }
    load();
}

async function load() {
    const version = ++session;
    if (active === "inicio") return loadHome(version);
    if (active === "paciente") return loadPatient(version);
    if (active === "alertas") return loadAlerts(version);
    if (active === "umbrales") return loadUmbrales(version);
    return loadStandard(active, version);
}

async function metricasApi(path, method = "GET", body) {
    const response = await fetch(`${metricasBase}/api/${path}`, {
        method,
        headers: {"Accept": "application/json", ...(body ? {"Content-Type": "application/json"} : {})},
        ...(body ? {body: JSON.stringify(body)} : {})
    });
    const raw = await response.text();
    let data;
    try { data = raw ? JSON.parse(raw) : null; } catch { data = raw; }
    if (!response.ok) throw new Error(`HTTP ${response.status}: ${data?.mensaje || data?.message || "No se pudo realizar la operación"}`);
    return data;
}

async function loadUmbrales(version) {
    $("section-title").textContent = "";
    $("list-title").textContent = "";
    $("record-count").textContent = "";
    replace("form");
    replace("records", el("p", "Cargando rangos...", "muted"));
    try {
        const umbrales = await metricasApi("umbrales");
        if (version !== session) return;
        const nombres = {
            PULSO: "Frecuencia cardíaca (lpm)",
            PRESION_SISTOLICA: "Presión sistólica (mmHg)",
            PRESION_DIASTOLICA: "Presión diastólica (mmHg)",
            TEMPERATURA: "Temperatura (°C)"
        };
        const container = el("div");
        const tabla = el("table"), head = el("thead"), filaHead = el("tr");
        ["Medición", "Mínimo", "Máximo", "Acción"].forEach(t => filaHead.append(el("th", t)));
        head.append(filaHead);
        tabla.append(head);
        const cuerpo = el("tbody");
        umbrales.forEach(u => {
            const fila = el("tr");
            const minimo = el("input"), maximo = el("input");
            minimo.type = maximo.type = "number";
            minimo.step = maximo.step = "any";
            minimo.value = u.minimo;
            maximo.value = u.maximo;
            const guardar = button("Guardar", async () => {
                const a = Number(minimo.value), b = Number(maximo.value);
                if (!minimo.value || !maximo.value || !Number.isFinite(a) || !Number.isFinite(b) || a >= b) {
                    inform("El mínimo debe ser menor que el máximo.");
                    return;
                }
                guardar.disabled = true;
                try {
                    await metricasApi(`umbrales/${encodeURIComponent(u.tipo)}`, "PUT", {minimo: a, maximo: b});
                    inform(`Umbral de ${nombres[u.tipo] || u.tipo} actualizado correctamente.`);
                    await load();
                } catch (e) {
                    inform(`No se pudo guardar el umbral: ${e.message}`);
                    guardar.disabled = false;
                }
            });
            [el("td", nombres[u.tipo] || u.tipo)].forEach(td => fila.append(td));
            for (const campo of [minimo, maximo]) { const td = el("td"); td.append(campo); fila.append(td); }
            const accion = el("td"); accion.append(guardar); fila.append(accion);
            cuerpo.append(fila);
        });
        tabla.append(cuerpo);
        container.append(tabla);
        replace("records", container);
        $("record-count").textContent = `${umbrales.length} tipos`;
    } catch (e) {
        if (version !== session) return;
        replace("records", el("p", "No se pudieron consultar los umbrales de Métricas.", "error-line"));
        inform(e.message);
    }
}

async function loadHome(version) {
    const keys = ["pacientes", "medicos", "turnos", "alertas"];
    const responses = await Promise.allSettled(keys.map(k => k === "alertas" ? alertasApi("alertas") : api(k)));
    if (version !== session) return;
    const stats = [];
    keys.forEach((key, i) => {
        const v = responses[i].status === "fulfilled" ? responses[i].value : null;
        const count = Array.isArray(v) ? v.length : "—";
        const b = el("button", null, "stat");
        b.append(el("strong", count), el("span", key === "alertas" ? "Alertas" : definitions[key].name));
        b.onclick = () => navigate(key);
        stats.push(b);
    });
    replace("stats", ...stats);
    const ok = responses.some(r => r.status === "fulfilled");
    status("connection", ok ? "Backend conectado" : "Backend sin conexión", ok);
    const activities = el("div");
    const counts = ["pacientes", "medicos", "turnos"].map((k, i) => {
        const data = responses[i].status === "fulfilled" ? responses[i].value : [];
        return `${Array.isArray(data) ? data.length : "—"} ${definitions[k].name.toLowerCase()}`;
    });
    activities.append(el("p", "Vista general de la actividad registrada."), el("div", counts.join(" · "), "activity-row"), el("p", "Seleccioná una sección en la navegación para administrar sus registros.", "muted"));
    replace("overview", activities);
    const alerts = responses[3].status === "fulfilled" && Array.isArray(responses[3].value) ? responses[3].value : [];
    renderRecent(alerts);
}

function renderRecent(items) {
    const area = el("div");
    if (!items.length) area.append(el("p", "No hay alertas recientes o el servicio no está disponible.", "empty"));
    items.slice().sort((a, b) => String(b.fechaHora || "").localeCompare(String(a.fechaHora || ""))).slice(0, 5).forEach(a => {
        const row = el("div", null, "recent");
        row.append(el("strong", a.tipo || "Alerta"), el("small", `${a.mensaje || ""}${a.estado ? " · " + a.estado : ""}`));
        area.append(row);
    });
    replace("recent-alerts", area);
}

async function loadAlerts(version) {
    $("section-title").textContent = "Seguimiento de alertas";
    replace("form", el("p", "Las alertas se generan automáticamente desde las mediciones. Las nuevas se notifican en tiempo real mientras la aplicación está abierta.", "muted"));
    $("records").textContent = "Cargando...";
    try {
        const list = await alertasApi("alertas") || [];
        if (version !== session) return;
        $("record-count").textContent = `${list.length} registros`;
        replace("records", makeTable(list, ["tipo", "mensaje", "fechaHora", "estado", "pacienteId"], (a, bar) => {
            if (a.pacienteId) bar.append(button("Datos", () => openAlertPatient(a.pacienteId)));
            if (a.estado !== "ATENDIDA") bar.append(button("Atender", () => changeAlert(a.id, "ATENDIDA")));
            if (a.estado !== "DESCARTADA") bar.append(button("Descartar", () => changeAlert(a.id, "DESCARTADA")));
        }));
        status("connection", "Backend conectado", true);
    } catch (e) {
        if (version !== session) return;
        replace("records", el("p", "No se pudieron consultar las alertas.", "error-line"));
        inform(e.message);
    }
}

async function changeAlert(id, state) {
    try {
        await alertasApi(`alertas/${id}/estado`, "PATCH", {estado: state});
        inform("Estado actualizado.");
        await load();
    } catch (e) {
        inform(e.message);
    }
}

async function openAlertPatient(pacienteId) {
    try {
        const paciente = await api(`pacientes/${encodeURIComponent(pacienteId)}`);
        openPatient(paciente);
    } catch (e) {
        inform(`No se pudo abrir la ficha del paciente: ${e.message}`);
    }
}

function openPatient(item) {
    patient = item;
    patientTab = "general";
    active = "paciente";
    editing = null;
    inform("");
    $("home").hidden = true;
    $("workspace").hidden = true;
    $("patient-view").hidden = false;
    $("page-heading").textContent = "Ficha del paciente";
    $("page-description").textContent = "Datos, seguimiento y atención asociados al paciente.";
    document.querySelectorAll("#tabs button").forEach(b => b.classList.toggle("active", b.dataset.tab === "pacientes"));
    load();
}

async function loadPatient(version) {
    if (!patient) return navigate("pacientes");
    $("patient-name").textContent = nameOf(patient);
    $("patient-info").textContent = `Documento: ${patient.documento || "—"} · ${patient.email || "Sin correo"}`;
    document.querySelectorAll("#patient-tabs button").forEach(b => b.classList.toggle("active", b.dataset.patientTab === patientTab));
    replace("patient-content", el("p", "Cargando ficha...", "muted"));
    await getDeps();
    if (version !== session) return;
    if (patientTab === "general") return showPatientGeneral();
    if (patientTab === "dispositivos") return showPatientDevices(version);
    if (patientTab === "historia") return showPatientHistories(version);
    if (patientTab === "historial") return showPatientTimeline(version);
    return showPatientAppointments(version);
}

function patientNavigate(tab) {
    patientTab = tab;
    load();
}

function panel(title) {
    const p = el("section", null, "panel"), h = el("h3", title);
    p.append(h);
    return p;
}

function showPatientGeneral() {
    const wrapper = el("div", null, "patient-grid"), details = panel("Datos personales"),
        info = el("div", null, "note-list");
    for (const k of ["nombre", "apellido", "documento", "fechaNacimiento", "telefono", "email"]) {
        const row = el("div", null, "simple-row");
        row.append(el("strong", `${labels[k]}: `), el("span", patient[k] || "—"));
        info.append(row);
    }
    details.append(info);
    const actions = panel("Administración");
    actions.append(el("p", "La información general se puede actualizar desde el listado de pacientes.", "muted"), button("Editar paciente", () => navigate("pacientes")));
    actions.append(button("Abrir historia clínica", () => patientNavigate("historia")), button("Ver historial y alertas", () => patientNavigate("historial")));
    wrapper.append(details, actions);
    replace("patient-content", wrapper);
}

async function showPatientDevices(version) {
    const host = el("div", null, "patient-grid"), dPanel = panel("Dispositivos asociados"),
        mPanel = panel("Mediciones recientes");
    mPanel.classList.add("wide");
    replace("patient-content", host);
    host.append(dPanel, mPanel);
    const dList = await Promise.allSettled([api(`consultas/pacientes/${patient.id}/dispositivos`), api(`mediciones?pacienteId=${encodeURIComponent(patient.id)}`)]);
    if (version !== session) return;
    const devs = dList[0].status === "fulfilled" && Array.isArray(dList[0].value) ? dList[0].value : [],
        measures = dList[1].status === "fulfilled" && Array.isArray(dList[1].value) ? dList[1].value : [];
    cache.dispositivos = devs;
    const tools = el("div", null, "section-tools");
    tools.append(el("span", `${devs.length} dispositivo(s)`, "muted"), button("Agregar dispositivo", () => deviceEditor(dPanel)));
    dPanel.append(tools, makeTable(devs, ["nombre", "tipo", "numeroSerie", "estado"], (d, bar) => {
        bar.append(button("Editar", () => deviceEditor(dPanel, d)), button("Eliminar", () => remove("dispositivos", d.id, () => loadPatient(++session)), "danger"));
    }));
    if (dList[0].status === "rejected") dPanel.append(el("p", `No se pudieron cargar dispositivos: ${dList[0].reason.message}`, "error-line"));
    mPanel.append(el("p", "Mediciones recibidas desde dispositivos IoT. La carga se realiza a través del sistema de métricas.", "muted"), makeTable(measures.slice(0, 30), ["tipo", "valor", "fechaHora", "dispositivoId"], null));
    if (dList[1].status === "rejected") mPanel.append(el("p", `No se pudieron cargar mediciones: ${dList[1].reason.message}`, "error-line"));
}

function deviceEditor(container, item) {
    container.querySelector(".sub-form")?.remove();
    const area = el("div", null, "sub-form");
    area.append(el("h4", item ? "Editar dispositivo" : "Nuevo dispositivo"), formFor("dispositivos", item, {
        fixed: {pacienteId: patient.id},
        cancel: () => area.remove(),
        onSaved: () => loadPatient(++session)
    }));
    container.append(area);
    area.scrollIntoView({behavior: "smooth", block: "nearest"});
}

async function showPatientHistories(version) {
    const root = el("div", null, "patient-grid"), main = panel("Historia clínica del paciente");
    main.classList.add("wide");
    root.append(main);
    replace("patient-content", root);
    let histories;
    try {
        histories = await api(`consultas/pacientes/${patient.id}/historias`) || [];
    } catch (e) {
        main.append(el("p", e.message, "error-line"));
        return;
    }
    if (version !== session) return;
    const toolbar = el("div", null, "section-tools"), actions = el("div", null, "row-actions");
    toolbar.append(el("span", `${histories.length} historia(s) registrada(s)`, "muted"), actions);
    main.append(toolbar);
    if (!histories.length) {
        actions.append(button("Crear historia clínica", async () => {
            try {
                await api("historias", "POST", {pacienteId: patient.id, estado: "ACTIVO"});
                inform("Historia clínica creada.");
                await load();
            } catch (e) {
                inform(e.message);
            }
        }, "primary"));
        main.append(el("p", "Todavía no existe una historia clínica para este paciente. Creala para registrar notas y medicación.", "empty"));
        return;
    }
    actions.append(button("Ver historial y alertas", () => patientNavigate("historial")));
    for (const h of histories) {
        const card = panel(`Historia clínica · ${h.estado || "—"}`);
        card.classList.add("wide");
        root.append(card);
        await showHistoryDetails(h.id, card, version);
    }
}

function dateToday() {
    return new Date().toLocaleDateString("en-CA");
}

function formInput(form, name, title, type = "text", required = true) {
    const w = el("div", null, "field"), lab = el("label", title),
        field = type === "textarea" ? el("textarea") : el("input");
    if (type !== "textarea") field.type = type;
    field.name = name;
    field.required = required;
    if (type === "date") field.value = dateToday();
    w.append(lab, field);
    form.append(w);
    return field;
}

function historyForm(title, fields, onSave) {
    const section = el("details", null, "sub-form"), summary = el("summary", title), form = el("form");
    section.append(summary, form);
    fields.forEach(([key, label, type, required]) => formInput(form, key, label, type, required !== false));
    const save = el("button", "Guardar");
    save.type = "submit";
    form.append(save);
    form.onsubmit = async e => {
        e.preventDefault();
        save.disabled = true;
        const data = Object.fromEntries(new FormData(form).entries());
        try {
            await onSave(data);
            inform("Registro clínico guardado.");
            await load();
        } catch (err) {
            inform(err.message);
            save.disabled = false;
        }
    };
    return section;
}

async function showHistoryDetails(id, card, version) {
    const results = await Promise.allSettled([api(`consultas/historias/${id}/notas`), api(`consultas/historias/${id}/medicaciones`), api(`consultas/historias/${id}/medicos`)]);
    if (version !== session) return;
    const [notes, meds, doctors] = results.map(x => x.status === "fulfilled" && Array.isArray(x.value) ? x.value : []);
    const zone = el("div", null, "patient-grid"), n = panel("Notas médicas"), m = panel("Medicación"),
        d = panel("Profesionales asociados");
    card.append(zone);
    zone.append(n, m, d);
    n.append(el("p", `${notes.length} nota(s) registrada(s)`, "muted"));
    if (!notes.length) n.append(el("p", "Sin notas registradas.", "empty"));
    notes.forEach(note => {
        const entry = el("div", null, "clinical-entry");
        entry.append(el("strong", note.fechaNota || "Sin fecha"), el("p", note.nota || "—"), el("small", note.medicoId ? `Profesional: ${relation("medicos", note.medicoId)}` : "Sin profesional indicado"));
        n.append(entry);
    });
    const noteForm = historyForm("+ Agregar nota médica", [["nota", "Contenido de la nota", "textarea"], ["fechaNota", "Fecha de la nota", "date"]], async data => {
        data.medicoId = noteDoctor.value || null;
        await api(`historias/${id}/notas`, "POST", data);
    });
    const noteDoctor = el("select");
    noteDoctor.name = "medicoId";
    noteDoctor.append(new Option("Profesional (opcional)", ""));
    (cache.medicos || []).forEach(x => noteDoctor.append(new Option(nameOf(x), x.id)));
    const doctorWrap = el("div", null, "field");
    doctorWrap.append(el("label", "Profesional que registra la nota"), noteDoctor);
    noteForm.querySelector("form button").before(doctorWrap);
    n.append(noteForm);
    m.append(el("p", `${meds.length} registro(s) de medicación`, "muted"));
    if (!meds.length) m.append(el("p", "Sin medicación registrada.", "empty"));
    meds.forEach((med, i) => {
        const entry = el("div", null, "clinical-entry");
        entry.append(el("strong", med.nombreMedicacion || "Medicación"), el("p", `${med.presentacionMedicacion || ""} · ${med.dosisMedicacion || ""}`), el("small", med.fechaMedicacion || "Sin fecha"), button("Eliminar", () => deleteMedication(id, i), "danger small"));
        m.append(entry);
    });
    m.append(historyForm("+ Agregar medicación", [["nombreMedicacion", "Nombre de la medicación", "text"], ["presentacionMedicacion", "Presentación", "text"], ["dosisMedicacion", "Dosis indicada", "text"], ["fechaMedicacion", "Fecha de indicación", "date"]], data => api(`historias/${id}/medicaciones`, "POST", data)));
    renderDoctors(d, doctors);
    const form = el("form", null, "sub-form"), select = el("select");
    select.name = "medicoId";
    select.required = true;
    select.append(new Option("Seleccionar profesional...", ""));
    (cache.medicos || []).filter(x => !doctors.some(doc => String(doc.id) === String(x.id))).forEach(x => select.append(new Option(nameOf(x), x.id)));
    const add = el("button", "Asociar profesional");
    add.type = "submit";
    form.append(select, add);
    form.onsubmit = async e => {
        e.preventDefault();
        try {
            await api(`historias/${id}/medicos/${select.value}`, "POST");
            inform("Profesional asociado.");
            await load();
        } catch (err) {
            inform(err.message);
        }
    };
    d.append(form);
    results.forEach((r, i) => {
        if (r.status === "rejected") [n, m, d][i].append(el("p", r.reason.message, "error-line"));
    });
}

function renderDoctors(target, items) {
    if (!items.length) target.append(el("p", "Sin profesionales asociados.", "empty"));
    const ul = el("ul", null, "note-list");
    items.forEach(x => ul.append(el("li", nameOf(x))));
    target.append(ul);
}

async function deleteMedication(id, index) {
    if (!confirm("¿Eliminar esta medicación?")) return;
    try {
        await api(`historias/${id}/medicaciones/${index}`, "DELETE");
        inform("Medicación eliminada.");
        await load();
    } catch (e) {
        inform(e.message);
    }
}

function timelineRow(category, when, title, detail) {
    const row = el("article", null, "timeline-row");
    row.append(el("span", category, "timeline-label"), el("strong", title), el("small", when || "Sin fecha"));
    if (detail) row.append(el("p", detail));
    return row;
}

async function showPatientTimeline(version) {
    const root = el("div", null, "patient-grid"), main = panel("Historial clínico y alertas guardadas");
    main.classList.add("wide");
    root.append(main);
    replace("patient-content", root);
    main.append(el("p", "Registros asociados al paciente, ordenados por fecha. Las alertas provienen del microservicio de Alertas y las mediciones del microservicio de Métricas.", "muted"));
    const sources = await Promise.allSettled([api(`consultas/pacientes/${patient.id}/historias`), alertasApi(`alertas?pacienteId=${encodeURIComponent(patient.id)}`), api(`mediciones?pacienteId=${encodeURIComponent(patient.id)}`)]);
    if (version !== session) return;
    const [histories, alerts, measures] = sources.map(x => x.status === "fulfilled" && Array.isArray(x.value) ? x.value : []);
    const all = [];
    alerts.forEach(a => all.push({
        date: a.fechaHora || a.fecha || "",
        node: timelineRow("Alerta", a.fechaHora || a.fecha, a.tipo || "Alerta", `${a.mensaje || ""} · Estado: ${a.estado || "—"}`)
    }));
    measures.forEach(m => all.push({
        date: m.fechaHora || "",
        node: timelineRow("Medición", m.fechaHora, m.tipo || "Medición", `${m.valor ?? "—"} ${m.unidad || ""}`)
    }));
    const noteResults = await Promise.allSettled(histories.map(h => api(`consultas/historias/${h.id}/notas`)));
    const medicationResults = await Promise.allSettled(histories.map(h => api(`consultas/historias/${h.id}/medicaciones`)));
    if (version !== session) return;
    noteResults.forEach(r => {
        if (r.status === "fulfilled" && Array.isArray(r.value)) r.value.forEach(n => all.push({
            date: n.fechaNota || "",
            node: timelineRow("Nota médica", n.fechaNota, `Nota de ${n.medicoId ? relation("medicos", n.medicoId) : "profesional"}`, n.nota)
        }));
    });
    medicationResults.forEach(r => {
        if (r.status === "fulfilled" && Array.isArray(r.value)) r.value.forEach(m => all.push({
            date: m.fechaMedicacion || "",
            node: timelineRow("Medicación", m.fechaMedicacion, m.nombreMedicacion || "Medicación", `${m.presentacionMedicacion || ""} · ${m.dosisMedicacion || ""}`)
        }));
    });
    all.sort((a, b) => String(b.date).localeCompare(String(a.date)));
    const info = el("div", null, "section-tools");
    info.append(el("span", `${alerts.length} alerta(s) · ${measures.length} medición(es) · ${all.length} eventos`, "muted"), button("Abrir historia clínica", () => patientNavigate("historia")));
    main.append(info);
    if (!all.length) main.append(el("p", "No hay eventos clínicos registrados para este paciente.", "empty")); else all.forEach(x => main.append(x.node));
    sources.forEach((r, i) => {
        if (r.status === "rejected") main.append(el("p", `No se pudieron consultar ${["historias clínicas", "alertas", "mediciones"][i]}: ${r.reason.message}`, "error-line"));
    });
    noteResults.forEach(r => {
        if (r.status === "rejected") main.append(el("p", `No se pudieron recuperar notas: ${r.reason.message}`, "error-line"));
    });
    medicationResults.forEach(r => {
        if (r.status === "rejected") main.append(el("p", `No se pudieron recuperar medicaciones: ${r.reason.message}`, "error-line"));
    });
}

async function showPatientAppointments(version) {
    const area = panel("Turnos del paciente");
    replace("patient-content", area);
    try {
        const rows = await api(`consultas/pacientes/${patient.id}/turnos`) || [];
        if (version !== session) return;
        area.append(makeTable(rows, ["fecha", "hora", "estado", "medicoId"], null), el("p", "Los turnos se crean y administran desde la sección Turnos.", "muted"));
    } catch (e) {
        area.append(el("p", e.message, "error-line"));
    }
}



function connectSocket() {
    if (socketRetry) clearTimeout(socketRetry);
    if (socket) {
        socket.onclose = null;
        socket.close();
    }
    status("ws-status", "Alertas: conectando", false);
    try {
        socket = new WebSocket(wsAddress);
        socket.onopen = () => status("ws-status", "Alertas en vivo", true);
        socket.onmessage = evt => {
            let a;
            try {
                a = JSON.parse(evt.data);
            } catch {
                return;
            }
            unread++;
            $("unread").hidden = false;
            $("unread").textContent = String(unread);
            inform(`Nueva alerta: ${a.tipo || "Alerta"}. ${a.mensaje || ""}`);
            if (active === "inicio" || active === "alertas") load();
        };
        socket.onerror = () => status("ws-status", "Alertas desconectadas", false);
        socket.onclose = () => {
            status("ws-status", "Alertas desconectadas", false);
            socketRetry = setTimeout(connectSocket, 5000);
        };
    } catch {
        status("ws-status", "Alertas no disponibles", false);
        socketRetry = setTimeout(connectSocket, 5000);
    }
}

for (const b of document.querySelectorAll("#tabs button")) b.onclick = () => navigate(b.dataset.tab);
for (const b of document.querySelectorAll("#patient-tabs button")) b.onclick = () => {
    patientTab = b.dataset.patientTab;
    load();
};
$("back-patients").onclick = () => navigate("pacientes");
$("refresh").onclick = () => load();
$("view-alerts").onclick = () => navigate("alertas");
navigate("inicio");
connectSocket();




async function alertasApi(path, method = "GET", body) {
    const response = await fetch(
        `${alertasBase}/api/${path}`,
        {
            method,
            headers: {
                "Accept": "application/json",
                ...(body !== undefined
                    ? {"Content-Type": "application/json"}
                    : {})
            },
            ...(body !== undefined
                ? {body: JSON.stringify(body)}
                : {})
        }
    );

    const raw = await response.text();
    let data = null;
    if (raw) {
        try { data = JSON.parse(raw); } catch { data = raw; }
    }

    if (!response.ok) {
        const detail = typeof data === "string" ? data :
            data?.detalles?.join("; ") || data?.error || data?.message || "Error de alertas";
        throw new Error(`HTTP ${response.status}: ${detail}`);
    }

    return data;
}
