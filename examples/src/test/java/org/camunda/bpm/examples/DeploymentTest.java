package org.camunda.bpm.examples;

import org.junit.jupiter.api.Test;

class DeploymentTest extends AbstractExampleTest {

  @Test
  void ensureDeployment() {

    given()
        .a_process_engine();

    then()
        .deployed_process_definitions(1)
        .deployed_process_definition("TestProcess");

  }

}
