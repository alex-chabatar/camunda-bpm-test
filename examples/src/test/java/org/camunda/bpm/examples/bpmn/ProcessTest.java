package org.camunda.bpm.examples.bpmn;

import java.util.Map;

import org.camunda.bpm.examples.AbstractExampleTest;
import org.junit.jupiter.api.Test;

class ProcessTest extends AbstractExampleTest {

  @Test
  void ensureProcess() {

    Map<String, Object> model = Map.of(
        "message", "Hello World!"
    );

    given()
        .a_process_engine()
        .a_business_key("12345")
        .a_process_model(model);

    when()
        .create_a_process_by_message("TestProcess");

    then()
        .active_processes(1)
        .active_processes(1, "TestProcess")
        .process_runtime()
        .contains(model)
        .active_tasks(1)
        .active_task("UserTask_ReviewResults")
        .with_task_name("Review Results");

    when()
        .complete_task("UserTask_ReviewResults");

    then()
        .no_active_processes()
        .completed_tasks(1)
        .completed_tasks(1, "UserTask_ReviewResults");

  }

}
