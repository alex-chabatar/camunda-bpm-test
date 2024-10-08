package org.camunda.bpm.examples;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableProcessApplication("test")
public class TestApp {

  public static void main(String... args) {
    SpringApplication.run(TestApp.class, args);
  }

}
