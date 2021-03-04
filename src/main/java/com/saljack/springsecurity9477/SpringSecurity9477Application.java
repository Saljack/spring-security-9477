package com.saljack.springsecurity9477;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@RestController
public class SpringSecurity9477Application {

  @Autowired
  private WebClient webClient;

  public static void main(String[] args) {
    SpringApplication.run(SpringSecurity9477Application.class, args);
  }

  @GetMapping(path = "/")
  public String getFromAuthResourceServer() {
    return webClient
      .get()
      .uri("http://localhost:18081/")
      .retrieve()
      .bodyToMono(String.class)
      .block();
  }

}
