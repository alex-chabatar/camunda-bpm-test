package org.camunda.bpm.extension.test.process.scenario;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.extension.test.utils.DateUtils.dateInFutureFrom;

import java.time.Duration;
import java.util.*;

import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.extension.test.process.model.UserTaskProperties;
import org.camunda.bpm.extension.test.report.formatter.ObjectArrayFormatter;
import org.springframework.util.ObjectUtils;

import com.tngtech.jgiven.annotation.*;
import com.tngtech.jgiven.integration.spring.JGivenStage;

@JGivenStage
public class ProcessThen<SELF extends ProcessThen<SELF>> extends AbstractProcessStage<SELF> {

  protected enum ModelType {
    RUNTIME, HISTORY, DETAIL, TASK_RUNTIME, TASK_HISTORY
  }

  @ExpectedScenarioState
  protected String processId;

  @ExpectedScenarioState
  private Map<String, String> processNameToId;

  @ExpectedScenarioState
  private MessageCorrelationResult messageCorrelationResult;

  private MessageEntity messageEntity;

  @ScenarioState
  protected String businessKey;

  @ProvidedScenarioState
  public Map<String, Object> runtimeModel;

  @ProvidedScenarioState
  public Map<String, Object> historyModel;

  @ProvidedScenarioState
  public Map<String, Object> historyDetailModel;

  @ProvidedScenarioState
  public String taskId;

  @ProvidedScenarioState
  protected final Map<String, String> taskNameToId = new HashMap<>();

  @ProvidedScenarioState
  public String historyTaskId;

  @ProvidedScenarioState
  public Map<String, Object> taskRuntimeModel;

  @ProvidedScenarioState
  public Map<String, Object> taskHistoryModel;

  private ModelType modelType;

  @ExpectedScenarioState
  protected Exception exception;

  @AfterScenario
  public void cleanUp() {
    // cleanup
    workflowHelper.deleteProcesses();
    // reset clock
    workflowHelper.resetWorkflowCurrentTime();
  }

  // -- Models (rooting & contains-check)

  protected ModelType getModelType() {
    return modelType;
  }

  protected Map<String, Object> getModel() {
    if (ModelType.RUNTIME.equals(modelType)) {
      return runtimeModel;
    } else if (ModelType.HISTORY.equals(modelType)) {
      return historyModel;
    } else if (ModelType.DETAIL.equals(modelType)) {
      return historyDetailModel;
    } else if (ModelType.TASK_RUNTIME.equals(modelType)) {
      return taskRuntimeModel;
    } else if (ModelType.TASK_HISTORY.equals(modelType)) {
      return taskHistoryModel;
    }
    return emptyMap();
  }

  @As("contains $key")
  public SELF containsKey(@SingleQuoted String key) {
    assertThat(getModel()).containsKey(key);
    return self();
  }

  @As("contains $key with not empty value")
  public SELF containsKeyWithNotEmptyValue(@SingleQuoted String key) {
    containsKey(key);
    assertThat(getModel().get(key)).isNotNull();
    return self();
  }

  @As("contains $key with empty value")
  public SELF containsKeyWithEmptyValue(@SingleQuoted String key) {
    containsKey(key);
    assertThat(getModel().get(key)).isNull();
    return self();
  }

  @As("contains $keys")
  public SELF containsKeys(@SingleQuoted String... keys) {
    for (String key : keys) {
      containsKey(key);
    }
    return self();
  }

  @As("contains $keys with not empty values")
  public SELF containsKeysWithNotEmptyValue(@SingleQuoted String... keys) {
    for (String key : keys) {
      containsKeyWithNotEmptyValue(key);
    }
    return self();
  }

  @As("contains $keys with empty values")
  public SELF containsKeysWithEmptyValue(@SingleQuoted String... keys) {
    for (String key : keys) {
      containsKeyWithEmptyValue(key);
    }
    return self();
  }

  @As("contains $key = $value")
  public SELF contains(@SingleQuoted String key, @SingleQuoted Object value) {
    assertThat(getModel()).containsEntry(key, value);
    return self();
  }

  public SELF contains(Map<String, Object> model) {
    for (Map.Entry<String, Object> entry : model.entrySet()) {
      contains(entry.getKey(), entry.getValue());
    }
    return self();
  }

  // -- Process Definitions

  @As("$number deployed BPMN process definition(s)")
  public SELF deployed_process_definitions(int number) {
    assertThat(workflowHelper.getProcessDefinitions()).hasSize(number);
    return self();
  }

  @As("no deployed BPMN process definitions")
  public SELF no_deployed_process_definitions() {
    return deployed_process_definitions(0);
  }

  @As("BPMN process definition of type $processType")
  public SELF deployed_process_definition(@SingleQuoted String processType) {
    assertThat(workflowHelper.getProcessDefinition(processType)).isNotNull();
    return self();
  }

  @As("$number active BPMN process(es)")
  public SELF active_processes(int number) {
    assertThat(workflowHelper.getProcessInstances()).hasSize(number);
    return self();
  }

  // -- Processes

  @As("$number active BPMN process(es) of type $processType")
  public SELF active_processes(int number, @SingleQuoted String processType) {
    assertThat(workflowHelper.getProcessInstances(processType)).hasSize(number);
    return self();
  }

  @As("no active BPMN processes")
  public SELF no_active_processes() {
    return active_processes(0);
  }

  @As("no active BPMN process(es) of type $processType")
  public SELF no_active_processes(@SingleQuoted String processType) {
    return active_processes(0, processType);
  }

  public SELF process_referenced_as_$(String processName) {
    assertThat(processNameToId).containsKey(processName);
    processId = processNameToId.get(processName);
    return self();
  }

  // -- Process runtime

  public SELF process_runtime() {
    modelType = ModelType.RUNTIME;
    runtimeModel = workflowHelper.getProcessVariables(processId);
    return self();
  }

  @As("process runtime for $processName")
  public SELF process_runtime(@SingleQuoted String processName) {
    assertThat(processNameToId).containsKey(processName);
    modelType = ModelType.RUNTIME;
    runtimeModel = workflowHelper.getProcessVariables(processNameToId.get(processName));
    return self();
  }

  // -- Process history
  // -- Process history detail

  // -- User tasks

  @As("$number active task(s)")
  public SELF active_tasks(int number) {
    assertThat(workflowHelper.getTasks()).hasSize(number);
    return self();
  }

  @As("active task of type $taskType")
  public SELF active_task(@SingleQuoted String taskType) {
    active_tasks(1, taskType);
    taskId = workflowHelper.getTasksByKey(taskType).get(0).getId();
    return self();
  }

  public SELF with_task_local_variable(@SingleQuoted String key, @SingleQuoted Object value) {
    assertThat(taskId).isNotNull();
    assertThat(workflowHelper.getTaskLocalVariables(taskId)).containsEntry(key, value);
    return self();
  }

  public SELF with_task_properties(UserTaskProperties taskProperties) {
    return with_task_name(taskProperties.getName())
        .with_assignee(taskProperties.getAssignee())
        .with_candidate_user(taskProperties.getKandidat())
        .with_candidate_group(taskProperties.getRolle())
        .with_priority(taskProperties.getPrio())
        .with_due_date(taskProperties.getDueDate())
        .with_follow_up_date(taskProperties.getFollowUpDate())
        .with_description(taskProperties.getDescription())
        .with_comment(taskProperties.getComment());
  }

  public SELF with_task_name(@SingleQuoted String taskName) {
    assertThat(taskId).isNotNull();
    if (!ObjectUtils.isEmpty(taskName)) {
      assertThat(workflowHelper.getTask(taskId).getName()).isEqualTo(taskName);
    }
    return self();
  }

  public SELF with_candidate_user(@SingleQuoted String user) {
    assertThat(taskId).isNotNull();
    if (ObjectUtils.isEmpty(user)) {
      assertThat(workflowHelper.getCandidateUsers(taskId)).isEmpty();
    } else {
      assertThat(workflowHelper.getCandidateUsers(taskId)).contains(user);
    }
    return self();
  }

  public SELF with_candidate_users(@ObjectArrayFormatter String... users) {
    assertThat(taskId).isNotNull();
    for (String user : users) {
      with_candidate_user(user);
    }
    return self();
  }

  public SELF no_candidate_user(@SingleQuoted String user) {
    assertThat(taskId).isNotNull();
    if (!ObjectUtils.isEmpty(user)) {
      assertThat(workflowHelper.getCandidateUsers(taskId)).doesNotContain(user);
    }
    return self();
  }

  public SELF with_assignee(@SingleQuoted String assignee) {
    assertThat(taskId).isNotNull();
    if (ObjectUtils.isEmpty(assignee)) {
      assertThat(workflowHelper.getAssignedUsers(taskId)).isEmpty();
    } else {
      assertThat(workflowHelper.getAssignedUsers(taskId)).contains(assignee);
    }
    return self();
  }

  public SELF no_assignee(@SingleQuoted String assignee) {
    assertThat(taskId).isNotNull();
    assertThat(workflowHelper.getAssignedUsers(taskId)).doesNotContain(assignee);
    return self();
  }

  public SELF no_users_assigned() {
    assertThat(taskId).isNotNull();
    assertThat(workflowHelper.getAssignedUsers(taskId)).isEmpty();
    return self();
  }

  public SELF no_candidate_users() {
    assertThat(taskId).isNotNull();
    assertThat(workflowHelper.getCandidateUsers(taskId)).isEmpty();
    return self();
  }

  public SELF with_candidate_group(@SingleQuoted String group) {
    assertThat(taskId).isNotNull();
    if (ObjectUtils.isEmpty(group)) {
      assertThat(workflowHelper.getCandidateGroups(taskId)).isEmpty();
    } else {
      assertThat(workflowHelper.getCandidateGroups(taskId)).contains(group);
    }
    return self();
  }

  public SELF with_candidate_groups(@ObjectArrayFormatter String... groups) {
    assertThat(taskId).isNotNull();
    for (String group : groups) {
      with_candidate_group(group);
    }
    return self();
  }

  public SELF no_candidate_group(@SingleQuoted String group) {
    assertThat(taskId).isNotNull();
    if (!ObjectUtils.isEmpty(group)) {
      assertThat(workflowHelper.getCandidateGroups(taskId)).doesNotContain(group);
    }
    return self();
  }

  public SELF no_candidate_groups() {
    assertThat(taskId).isNotNull();
    assertThat(workflowHelper.getCandidateGroups(taskId)).isEmpty();
    return self();
  }

  public SELF with_form_key(@SingleQuoted String formKey) {
    assertThat(taskId).isNotNull();
    assertThat(workflowHelper.getTask(taskId).getFormKey()).isEqualTo(formKey);
    return self();
  }

  public SELF with_priority(@SingleQuoted int priority) {
    assertThat(taskId).isNotNull();
    assertThat(workflowHelper.getTask(taskId).getPriority()).isEqualTo(priority);
    return self();
  }

  public SELF with_follow_up_date(@SingleQuoted Date date) {
    assertThat(taskId).isNotNull();
    var followUpDate = workflowHelper.getTask(taskId).getFollowUpDate();
    if (date != null) {
      assertThat(followUpDate).isNotNull();
      assertThat(followUpDate).isEqualTo(date);
    } else {
      assertThat(followUpDate).isNull();
    }
    return self();
  }

  @As("with dueDate in Future $duration")
  public SELF with_due_date_in_future(@SingleQuoted Duration duration) {
    assertThat(taskId).isNotNull();
    Date createTime = workflowHelper.getTask(taskId).getCreateTime();
    assertThat(createTime).isNotNull();
    return with_due_date_in_future_from(duration, createTime);
  }

  @As("with dueDate in Future $duration from $from")
  public SELF with_due_date_in_future_from(@SingleQuoted Duration duration, @SingleQuoted Date from) {
    assertThat(taskId).isNotNull();
    return with_due_date(dateInFutureFrom(from, duration));
  }

  @As("with dueDate $date")
  public SELF with_due_date(@SingleQuoted Date date) {
    assertThat(taskId).isNotNull();
    Date dueDate = workflowHelper.getTask(taskId).getDueDate();
    if (date != null) {
      assertThat(dueDate).isNotNull();
      assertThat(dueDate).isEqualTo(date);
    }
    return self();
  }

  @As("with description $description")
  public SELF with_description(@SingleQuoted String description) {
    assertThat(taskId).isNotNull();
    assertThat(workflowHelper.getTask(taskId).getDescription()).isEqualTo(description);
    return self();
  }

  @As("with comment $comment")
  public SELF with_comment(@SingleQuoted String comment) {
    assertThat(taskId).isNotNull();
    assertThat(workflowHelper.getTaskComments(taskId).stream()
        .map(Comment::getFullMessage))
        .contains(comment);
    return self();
  }

  @As("$number active task(s) of type $taskType")
  public SELF active_tasks(int number, @SingleQuoted String taskType) {
    assertThat(workflowHelper.getTasksByKey(taskType)).hasSize(number);
    return self();
  }

  @As("$number active task(s) with $taskName name")
  public SELF active_tasks_with_name(int number, @SingleQuoted String taskName) {
    assertThat(workflowHelper.getTasks().stream()
        .filter(task -> task.getName().equals(taskName)))
        .hasSize(number);
    return self();
  }

  @As("$number active not assigned task(s) of type $taskType")
  public SELF active_not_assigned_tasks(int number, @SingleQuoted String taskType) {
    assertThat(workflowHelper.getTasksByKey(taskType).stream()
        .filter(task -> Objects.isNull(task.getAssignee())))
        .hasSize(number);
    return self();
  }

  public SELF no_active_tasks() {
    return active_tasks(0);
  }

  @As("no active task(s) of type $taskType")
  public SELF no_active_tasks(@SingleQuoted String taskType) {
    return active_tasks(0, taskType);
  }

  @As("$number history task(s)")
  public SELF completed_tasks(int number) {
    assertThat(workflowHelper.getHistoryTasks().stream()
        .filter(task -> Objects.nonNull(task.getEndTime())))
        .hasSize(number);
    return self();
  }

  @As("$number history task(s) of type $taskType")
  public SELF completed_tasks(int number, @SingleQuoted String taskType) {
    assertThat(workflowHelper.getHistoryTasksByKey(taskType).stream()
        .filter(task -> Objects.nonNull(task.getEndTime())))
        .hasSize(number);
    return self();
  }

  @As("completed task of type $taskType")
  public SELF completed_task(@SingleQuoted String taskType) {
    completed_tasks(1, taskType);
    historyTaskId = workflowHelper.getHistoryTasksByKey(taskType).stream()
        .filter(task -> Objects.nonNull(task.getEndTime()))
        .map(HistoricTaskInstance::getId)
        .findFirst().orElse(null);
    assertThat(historyTaskId).isNotNull();
    return self();
  }

  public SELF completed_task_referenced_as_$(@SingleQuoted String taskName) {
    assertThat(taskNameToId).containsKey(taskName);
    var historicTask = workflowHelper.getHistoryTask(taskNameToId.get(taskName));
    assertThat(historicTask).isNotNull();
    assertThat(historicTask.getEndTime()).isNotNull();
    historyTaskId = historicTask.getId();
    return self();
  }

  // -- Activities

  public SELF withActivity(@SingleQuoted String activityName) {
    assertThat(workflowHelper.hasActivity(processId, activityName)).isTrue();
    return self();
  }

  public SELF withoutActivity(@SingleQuoted String activityName) {
    assertThat(workflowHelper.hasActivity(processId, activityName)).isFalse();
    return self();
  }

  public SELF withActivities(String... activityNames) {
    for (String activityName : activityNames) {
      withActivity(activityName);
    }
    return self();
  }

  public SELF withActivities(List<String> activityNames) {
    for (String activityName : activityNames) {
      withActivity(activityName);
    }
    return self();
  }

  // -- Task runtime
  // -- Task history


}
