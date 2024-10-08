package org.camunda.bpm.extension.test.utils;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.util.*;
import java.util.function.Supplier;

import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.*;
import org.camunda.bpm.engine.task.*;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Service
public class WorkflowTestHelper {

  private static final Logger log = LoggerFactory.getLogger(WorkflowTestHelper.class);

  // Camunda services

  @Autowired
  private ProcessEngineConfiguration processEngineConfiguration;

  @Autowired
  private RepositoryService repositoryService;

  @Autowired
  private RuntimeService runtimeService;

  @Autowired
  private HistoryService historyService;

  @Autowired
  private DecisionService decisionService;

  @Autowired
  private TaskService taskService;

  @Autowired
  private IdentityService identityService;

  @Autowired
  private ExternalTaskService externalTaskService;

  @Autowired
  private ManagementService managementService;

  public ProcessEngineConfiguration getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }

  // --- Definitions

  public List<ProcessDefinition> getProcessDefinitions() {
    return repositoryService
        .createProcessDefinitionQuery()
        .latestVersion()
        .list();
  }

  public ProcessDefinition getProcessDefinition(String processDefinitionKey) {
    return repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(processDefinitionKey)
        .latestVersion()
        .singleResult();
  }

  public ProcessDefinition getProcessDefinitionById(String processDefinitionId) {
    if (!ObjectUtils.isEmpty(processDefinitionId)) {
      return repositoryService
          .createProcessDefinitionQuery()
          .processDefinitionId(processDefinitionId)
          .singleResult();
    }
    return null;
  }

  public String getProcessDefinitionKeyById(String processDefinitionId) {
    ProcessDefinition definition = getProcessDefinitionById(processDefinitionId);
    return definition != null ? definition.getKey() : null;
  }

  private final LoadingCache<String, BpmnModelInstance> bpmnModelInstanceCache = CacheBuilder.newBuilder()
      .build(new CacheLoader<>() {
        @Override
        public BpmnModelInstance load(String processDefinitionId) {
          return repositoryService.getBpmnModelInstance(processDefinitionId);
        }
      });

  // --- Create processes

  public String createProcess(String processDefinitionKey, Map<String, Object> values) {
    return createProcessInstance(processDefinitionKey, values).getId();
  }

  public ProcessInstance createProcessInstance(String processDefinitionKey, Map<String, Object> values) {
    return runtimeService.startProcessInstanceByKey(processDefinitionKey, values);
  }

  public String createProcess(String processDefinitionKey, String businessKey, Map<String, Object> values) {
    return createProcessInstance(processDefinitionKey, businessKey, values).getId();
  }

  public ProcessInstance createProcessInstance(String processDefinitionKey, String businessKey,
      Map<String, Object> values) {
    var startTime = System.currentTimeMillis();
    var result = createProcessInstanceBuilder(processDefinitionKey, businessKey, values).execute();
    log.info("Created workflow process '{}' of type '{}' with businessKey '{}' and values {}, time used {} ms",
        result.getId(), processDefinitionKey, businessKey, values, System.currentTimeMillis() - startTime);
    return result;
  }

  public String createProcessBeforeActivity(String processDefinitionKey, String businessKey, String activityId,
      Map<String, Object> values) {
    return createProcessInstanceBeforeActivity(processDefinitionKey, businessKey, activityId, values).getId();
  }

  public ProcessInstance createProcessInstanceBeforeActivity(String processDefinitionKey, String businessKey,
      String activityId, Map<String, Object> values) {
    return createProcessInstanceBuilder(processDefinitionKey, businessKey, values)
        .startBeforeActivity(activityId)
        .execute();
  }

  public String createProcessAfterActivity(String processDefinitionKey, String businessKey, String activityId,
      Map<String, Object> values) {
    return createProcessInstanceAfterActivity(processDefinitionKey, businessKey, activityId, values).getId();
  }

  public ProcessInstance createProcessInstanceAfterActivity(String processDefinitionKey, String businessKey,
      String activityId, Map<String, Object> values) {
    return createProcessInstanceBuilder(processDefinitionKey, businessKey, values)
        .startAfterActivity(activityId)
        .execute();
  }

  private ProcessInstantiationBuilder createProcessInstanceBuilder(String processDefinitionKey,
      Map<String, Object> values) {
    return runtimeService.createProcessInstanceByKey(processDefinitionKey)
        .setVariables(values);
  }

  private ProcessInstantiationBuilder createProcessInstanceBuilder(String processDefinitionKey, String businessKey,
      Map<String, Object> values) {
    return createProcessInstanceBuilder(processDefinitionKey, values)
        .businessKey(businessKey);
  }

  // --- Get processes

  public List<ProcessInstance> getProcessInstances() {
    return createProcessInstanceQuery().list();
  }

  public ProcessInstance getProcessInstance(String processId) {
    return createProcessInstanceQuery()
        .processInstanceId(processId)
        .singleResult();
  }

  public List<ProcessInstance> getProcessInstances(String processDefinitionKey) {
    return createProcessInstanceQuery()
        .processDefinitionKey(processDefinitionKey)
        .list();
  }

  public ProcessInstanceQuery createProcessInstanceQuery() {
    return runtimeService.createProcessInstanceQuery();
  }

  public Map<String, Object> getProcessVariables(String executionId) {
    Map<String, Object> variables = new HashMap<>();
    variableInstanceQuery()
        .executionIdIn(executionId)
        .list()
        .forEach(item -> variables.put(item.getName(), item.getValue()));
    return variables;
  }

  public VariableInstanceQuery variableInstanceQuery() {
    return runtimeService.createVariableInstanceQuery();
  }

  // --- Delete processes

  public void deleteProcess(String processId, String reason) {
    runtimeService.deleteProcessInstance(processId, reason);
  }

  public void deleteProcesses(String reason) {
    getProcessInstances()
        .forEach(process -> deleteProcess(process.getId(), reason));
  }

  public void deleteProcesses() {
    deleteProcesses("cleanup");
//    deleteHistoryProcesses();
//    closeCaseInstances();
//    deleteHistoryCaseInstances();
//    deleteHistoricDecisionProcesses();
  }

  // -- User tasks

  public List<Task> getTasks() {
    return tasksQuery().list();
  }

  public Task getTask(String taskId) {
    if (!ObjectUtils.isEmpty(taskId)) {
      return tasksQuery()
          .taskId(taskId)
          .singleResult();
    }
    return null;
  }

  public List<Task> getTasksByKey(String taskDefinitionKey) {
    return tasksByKeyQuery(taskDefinitionKey).list();
  }

  public List<Task> getTasksByKey(String processId, String taskDefinitionKey) {
    return tasksByKeyQuery(taskDefinitionKey)
        .processInstanceId(processId)
        .list();
  }

  public TaskQuery tasksByKeyQuery(String taskDefinitionKey) {
    return tasksQuery().taskDefinitionKey(taskDefinitionKey);
  }

  public List<Task> getProcessTasks(String processId) {
    return processTasksQuery(processId).list();
  }

  public TaskQuery processTasksQuery(String processId) {
    return tasksQuery().processInstanceId(processId);
  }

  public TaskQuery tasksQuery() {
    return taskService
        .createTaskQuery()
        .initializeFormKeys();
  }

  public List<HistoricTaskInstance> getHistoryTasks() {
    return historyService
        .createHistoricTaskInstanceQuery()
        .list();
  }

  public HistoricTaskInstance getHistoryTask(String taskId) {
    if (!ObjectUtils.isEmpty(taskId)) {
      return historyService
          .createHistoricTaskInstanceQuery()
          .taskId(taskId)
          .singleResult();
    }
    return null;
  }

  public List<HistoricTaskInstance> getHistoryTasksByKey(String taskDefinitionKey) {
    return getHistoryTasks(taskDefinitionKey, null);
  }

  public List<HistoricTaskInstance> getHistoryTasks(String taskDefinitionKey, Map<String, Object> taskLocalVariables) {
    return getHistoryTasksQuery(taskDefinitionKey, taskLocalVariables).list();
  }

  public List<HistoricTaskInstance> getHistoryTasks(String processId, String taskDefinitionKey,
      Map<String, Object> taskLocalVariables) {
    return getHistoryTasksQuery(taskDefinitionKey, taskLocalVariables)
        .processInstanceId(processId)
        .list();
  }

  private HistoricTaskInstanceQuery getHistoryTasksQuery(String taskDefinitionKey,
      Map<String, Object> taskLocalVariables) {
    HistoricTaskInstanceQuery query = getHistoryTasksQuery().taskDefinitionKey(taskDefinitionKey);
    if (!ObjectUtils.isEmpty(taskLocalVariables)) {
      taskLocalVariables.entrySet()
          .forEach(variable -> query.taskVariableValueEquals(variable.getKey(), variable.getValue()));
    }
    return query;
  }

  public HistoricTaskInstanceQuery getHistoryTasksQuery() {
    return historyService.createHistoricTaskInstanceQuery();
  }

  public void setTaskLocalVariable(TaskEntity task, String variableName, Object value) {
    task.setVariableLocal(variableName, value);
  }

  public Map<String, Object> getTaskLocalVariables(String taskId) {
    return Optional.ofNullable(taskService.getVariablesLocal(taskId)).orElse(emptyMap());
  }

  public List<String> getCandidateUsers(String taskId) {
    return getTaskUsers(taskId, IdentityLinkType.CANDIDATE);
  }

  public List<String> getAssignedUsers(String taskId) {
    return getTaskUsers(taskId, IdentityLinkType.ASSIGNEE);
  }

  // assignee + candidates
  public List<String> getInvolvedUsers(String taskId) {
    Set<String> involvedUsers = Sets.newHashSet(getAssignedUsers(taskId));
    involvedUsers.addAll(getCandidateUsers(taskId));
    return Lists.newArrayList(involvedUsers);
  }

  private List<String> getTaskUsers(String taskId, String identityLinkType) {
    return getIdentityLinksForTask(taskId).stream()
        .filter(id -> identityLinkType.equals(id.getType()) && id.getUserId() != null)
        .map(IdentityLink::getUserId)
        .distinct()
        .collect(toList());
  }

  private List<IdentityLink> getIdentityLinksForTask(String taskId) {
    return Optional.ofNullable(taskService.getIdentityLinksForTask(taskId))
        .orElse(emptyList());
  }

  public List<String> getCandidateGroups(String taskId) {
    return getTaskGroups(taskId, IdentityLinkType.CANDIDATE);
  }

  private List<String> getTaskGroups(String taskId, String identityLinkType) {
    return getIdentityLinksForTask(taskId).stream()
        .filter(id -> identityLinkType.equals(id.getType()) && id.getGroupId() != null)
        .map(IdentityLink::getGroupId)
        .distinct()
        .collect(toList());
  }

  public void completeTask(String taskId) {
    taskService.complete(taskId);
  }

  public void completeTask(String taskId, Map<String, Object> variables) {
    taskService.complete(taskId, variables);
  }

  public void completeTasks(String taskDefinitionKey) {
    completeTasks(taskDefinitionKey, Maps.newHashMap());
  }

  public void completeTasks(String taskDefinitionKey, Map<String, Object> variables) {
    getTasksByKey(taskDefinitionKey)
        .forEach(task -> completeTask(task.getId(), variables));
  }

  // -- Variables

  public void setVariables(String executionId, Map<String, Object> values) {
    runtimeService.setVariables(executionId, values);
  }

  // -- Messages

  /**
   * Start process by message start event
   *
   * @param message
   *          message name
   * @param payload
   *          payload
   * @return Process instance or NULL.
   */
  public synchronized ProcessInstance sendStartMessage(String message, Map<String, Object> payload) {
    return sendStartMessage(message, null, payload);
  }

  /**
   * Start process by message start event
   *
   * @param message
   *          message name
   * @param businessKey
   *          businessKey
   * @param payload
   *          payload
   * @return Process instance or NULL.
   */
  public synchronized ProcessInstance sendStartMessage(String message, String businessKey,
      Map<String, Object> payload) {
    var builder = messageCorrelationBuilder(message, payload);
    if (!ObjectUtils.isEmpty(businessKey)) { // ensureNotNull("businessKey", businessKey) in Camunda
      builder.processInstanceBusinessKey(businessKey);
    }
    var startTime = System.currentTimeMillis();
    var result = builder.correlateStartMessage();
    log.info("Correlated start message '{}' to process {} with businessKey {} and payload {}, time used {} ms",
        message, result.getId(), businessKey, payload, System.currentTimeMillis() - startTime);
    return result;
  }

  /**
   * Start process by message start event
   *
   * @param message
   *          message name
   * @param payload
   *          payload
   * @return MessageCorrelationResult.
   */
  public MessageCorrelationResult sendStartMessageWithResult(String message, Map<String, Object> payload) {
    var startTime = System.currentTimeMillis();
    var result = messageCorrelationBuilder(message, payload)
        .startMessageOnly()
        .correlateWithResult();
    log.info("Correlated start message '{}' to process {} with payload {}, time used {} ms",
        message, result.getProcessInstance().getId(), payload, System.currentTimeMillis() - startTime);
    return result;
  }

  private MessageCorrelationBuilder messageCorrelationBuilder(String message, Map<String, Object> payload) {
    return runtimeService
        .createMessageCorrelation(message)
        .setVariables(payload);
  }

  // -- Activities

  public List<HistoricActivityInstance> getActivityTasks(String processId) {
    return historicActivityInstanceQuery(processId)
        .orderByHistoricActivityInstanceStartTime().asc()
        .list();
  }

  public List<HistoricActivityInstance> getActivityTasksById(String processId, String activityId) {
    return getActivityTasks(processId).stream()
        .filter(activity -> activityId.equals(activity.getActivityId()))
        .collect(toList());
  }

  public HistoricActivityInstance getLastActivityTaskById(String processId, String activityId) {
    return Optional.ofNullable(getActivityTasksById(processId, activityId)).orElse(emptyList()).stream()
        .sorted(comparing(HistoricActivityInstance::getStartTime).reversed())
        .findFirst() // latest
        .orElse(null);
  }

  public List<HistoricActivityInstance> getActivityTasksByName(String processId, String activityName) {
    return getActivityTasks(processId).stream()
        .filter(activity -> activityName.equals(activity.getActivityName()))
        .collect(toList());
  }

  public HistoricActivityInstance getLastActivityTaskByName(String processId, String activityName) {
    return Optional.ofNullable(getActivityTasksByName(processId, activityName)).orElse(emptyList()).stream()
        .sorted(comparing(HistoricActivityInstance::getStartTime).reversed())
        .findFirst() // latest
        .orElse(null);
  }

  public boolean hasActivity(String processId, String activity) {
    return getActivityTasks(processId).stream()
        .anyMatch(p -> activity.equals(p.getActivityName()) || activity.equals(p.getActivityId()));
  }

  public List<HistoricActivityInstance> getActivitiesByType(String processId, String activityType) {
    return getActivityTasks(processId).stream()
        .filter(activity -> activityType.equals(activity.getActivityType()))
        .collect(toList());
  }

  public List<HistoricActivityInstance> getSubProcesses(String processId) {
    return getActivitiesByType(processId, ActivityTypes.SUB_PROCESS);
  }

  public HistoricActivityInstanceQuery historicActivityInstanceQuery(String processId) {
    return historyService
        .createHistoricActivityInstanceQuery()
        .processInstanceId(processId);
  }

  // -- Comments

  public List<Comment> getProcessComments(String processId) {
    return taskService.getProcessInstanceComments(processId);
  }

  public Comment addProcessComment(String processId, String comment) {
    return addProcessComment(processId, null, comment);
  }

  public Comment addProcessComment(String processId, String userId, String comment) {
    return withAuthenticatedUser(userId, () -> taskService.createComment(null, processId, comment));
  }

  public List<Comment> getTaskComments(String taskId) {
    return taskService.getTaskComments(taskId);
  }

  public Comment addTaskComment(String taskId, String comment) {
    return addTaskComment(taskId, null, comment);
  }

  public Comment addTaskComment(String taskId, String userId, String comment) {
    return withAuthenticatedUser(userId, () -> taskService.createComment(taskId, null, comment));
  }

  public Comment addComment(String taskId, String processId, String userId, String comment) {
    return withAuthenticatedUser(userId, () -> taskService.createComment(taskId, processId, comment));
  }

  private <T> T withAuthenticatedUser(String userId, Supplier<T> supplier) {
    if (!ObjectUtils.isEmpty(userId)) {
      identityService.setAuthenticatedUserId(userId);
      T result = supplier.get();
      identityService.setAuthenticatedUserId(null);
      return result;
    } else {
      return supplier.get();
    }
  }

  // -- Clock

  public Date getWorkflowCurrentTime() {
    return ClockUtil.getCurrentTime();
  }

  public void setWorkflowCurrentTime(Date currentTime) {
    ClockUtil.setCurrentTime(currentTime);
  }

  public void resetWorkflowCurrentTime() {
    ClockUtil.reset();
  }

}
