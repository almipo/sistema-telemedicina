package com.telemedicina.metricas.config;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
@Configuration
public class HttpConfig {
 @Bean public RestClient restClient(@Value("${services.timeout-segundos:5}") int timeout) {
  SimpleClientHttpRequestFactory factory=new SimpleClientHttpRequestFactory();
  factory.setConnectTimeout(Duration.ofSeconds(timeout));factory.setReadTimeout(Duration.ofSeconds(timeout));
  return RestClient.builder().requestFactory(factory).build();
 }
}
