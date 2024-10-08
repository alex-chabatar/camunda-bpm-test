package org.camunda.bpm.examples.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProcessTestData implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) {
    log.info("Processing test data...");
  }

}