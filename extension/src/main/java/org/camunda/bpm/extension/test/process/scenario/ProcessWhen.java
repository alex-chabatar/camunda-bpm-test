package org.camunda.bpm.extension.test.process.scenario;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.extension.test.report.formatter.ObjectArrayFormatter;

import com.tngtech.jgiven.annotation.*;
import com.tngtech.jgiven.integration.spring.JGivenStage;

@JGivenStage
public class ProcessWhen<SELF extends ProcessWhen<SELF>> extends AbstractProcessStage<SELF> {

  @ExpectedScenarioState
  protected String businessKey;

  @ExpectedScenarioState
  private Map<String, Object> model;

  @ExpectedScenarioState
  private String beforeActivity;

  @ExpectedScenarioState
  private String afterActivity;

  @ProvidedScenarioState
  protected String processId;

  @ProvidedScenarioState
  protected final Map<String, String> processNameToId = new HashMap<>();

  @ProvidedScenarioState
  private MessageCorrelationResult messageCorrelationResult;

  @ProvidedScenarioState
  protected Exception exception;

  // -- Generic

  @As("Execute $comment")
  public <T> SELF execute(@SingleQuoted String comment, @Hidden Supplier<T> supplier) {
    supplier.get();
    return self();
  }

  // -- Process

  public SELF create_a_process(@SingleQuoted String processDefinitionKey) {
    return create_a_process(processDefinitionKey, businessKey);
  }

  @As("Create a process of type $processDefinitionKey with businessKey=$businessKey")
  public SELF create_a_process(@SingleQuoted String processDefinitionKey, @SingleQuoted String businessKey) {

    if (beforeActivity != null) {
      processId = workflowHelper.createProcessBeforeActivity(processDefinitionKey, businessKey, beforeActivity, model);
    } else if (afterActivity != null) {
      processId = workflowHelper.createProcessAfterActivity(processDefinitionKey, businessKey, afterActivity, model);
    } else {
      processId = workflowHelper.createProcess(processDefinitionKey, businessKey, model);
    }

    assertThat(processId).isNotNull();

    log.info("Created process {}", processId);
    waitForJobExecutorToProcessAllJobs();

    return self();
  }

  public SELF create_a_process_by_message(@SingleQuoted String message) {
    processId = workflowHelper.sendStartMessage(message, businessKey, model).getProcessInstanceId();
    assertThat(processId).isNotNull();

    log.info("Created process {}", processId);
    waitForJobExecutorToProcessAllJobs();

    return self();
  }

  public SELF create_a_process_by_message_$_with_result(@SingleQuoted String message) {
    messageCorrelationResult = workflowHelper.sendStartMessageWithResult(message, model);
    assertThat(messageCorrelationResult).isNotNull();
    processId = messageCorrelationResult.getProcessInstance().getId();
    assertThat(processId).isNotNull();

    log.info("Created process {}", processId);
    waitForJobExecutorToProcessAllJobs();

    return self();
  }

  public SELF referenced_as(@SingleQuoted String processName) {
    processNameToId.put(processName, processId);
    return self();
  }

  public SELF set_variables(Map<String, Object> values) {
    workflowHelper.setVariables(processId, values);
    waitForJobExecutorToProcessAllJobs();
    return self();
  }

  public SELF set_variables(@SingleQuoted String processName, Map<String, Object> values) {
    assertThat(processNameToId).containsKey(processName);
    workflowHelper.setVariables(processNameToId.get(processName), values);
    waitForJobExecutorToProcessAllJobs();
    return self();
  }

  // -- User tasks

  @As("complete active task of type $taskDefinitionKey")
  public SELF complete_task(@SingleQuoted String taskDefinitionKey) {
    return complete_task(taskDefinitionKey, emptyMap());
  }

  @As("complete active task of type $taskDefinitionKey")
  public SELF complete_all_tasks(@SingleQuoted String taskDefinitionKey) {
    List<Task> tasks = workflowHelper.getTasksByKey(taskDefinitionKey);
    for (Task task: tasks) {
      try {
        workflowHelper.completeTask(task.getId(), emptyMap());
      } catch (Exception ex) {
        // note the exception here
        exception = ex;
      }
    }
    waitForJobExecutorToProcessAllJobs();
    return self();
  }

  @As("complete active task of type $taskDefinitionKey with $variables")
  public SELF complete_task(@SingleQuoted String taskDefinitionKey, Map<String, Object> variables) {
    List<Task> tasks = workflowHelper.getTasksByKey(taskDefinitionKey);
    assertThat(tasks).hasSize(1);

    try {
      workflowHelper.completeTask(tasks.get(0).getId(), variables);
    } catch (Exception ex) {
      // note the exception here
      exception = ex;
    }

    waitForJobExecutorToProcessAllJobs();

    return self();
  }

  @As("complete all active task(s)")
  public SELF complete_tasks() {
    workflowHelper.getTasks().forEach(task -> workflowHelper.completeTask(task.getId()));
    waitForJobExecutorToProcessAllJobs();
    return self();
  }

  @As("complete all active task(s) of type $taskDefinitionKey")
  public SELF complete_tasks(@SingleQuoted String taskDefinitionKey) {
    workflowHelper.completeTasks(taskDefinitionKey);
    waitForJobExecutorToProcessAllJobs();
    return self();
  }

  public SELF complete_tasks(@ObjectArrayFormatter String... taskDefinitionKeys) {
    Stream.of(taskDefinitionKeys).forEach(this::complete_tasks);
    return self();
  }

  @As("complete all active tasks of type $taskDefinitionKey with $variables")
  public SELF complete_tasks(@SingleQuoted String taskDefinitionKey, Map<String, Object> variables) {
    workflowHelper.completeTasks(taskDefinitionKey, variables);
    waitForJobExecutorToProcessAllJobs();
    return self();
  }

}
