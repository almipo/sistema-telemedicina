package com.telemedicina.backend.service;

import com.telemedicina.backend.exception.RecursoNoEncontradoException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
public abstract class CrudService<T> {
 private final JpaRepository<T,UUID> repo;
 protected CrudService(JpaRepository<T,UUID> repo){this.repo=repo;}
 @Transactional(readOnly=true) public List<T> listar(){return repo.findAll();}
 @Transactional(readOnly=true) public T obtener(UUID id){return repo.findById(id).orElseThrow(()->new RecursoNoEncontradoException("Registro inexistente: "+id));}
 @Transactional protected T guardar(T e){return repo.save(e);}
 @Transactional public void eliminar(UUID id){obtener(id);repo.deleteById(id);}
}
