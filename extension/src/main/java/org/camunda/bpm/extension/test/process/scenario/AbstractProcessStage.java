package org.camunda.bpm.extension.test.process.scenario;

import static org.camunda.bpm.extension.test.utils.DateUtils.dateInFutureFrom;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.extension.test.utils.WorkflowTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.SingleQuoted;

public abstract class AbstractProcessStage<SELF extends AbstractProcessStage<SELF>> extends Stage<SELF> {

  protected Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  protected WorkflowTestHelper workflowHelper;

  private final ExecutorService executorService = Executors.newFixedThreadPool(10);

  public SELF test_case(@SingleQuoted String testCase) {
    return self();
  }

  // -- Jobs

  protected void executeJob(Callable<SELF> job) {
    doJob(job, false);
  }

  protected void scheduleJob(Callable<SELF> job) {
    doJob(job, true);
  }

  private void doJob(Callable<SELF> job, boolean async) {
    try {
      if (async) {
        executorService.submit(job);
      } else {
        job.call();
      }
    } catch (Exception ex) {
      log.error("Job execution failed", ex);
    }
  }

  protected void waitForJobExecutorToProcessAllJobs() {
    waitForJobExecutorToProcessAllJobs(60 * 1000L, 25L);
  }

  private void waitForJobExecutorToProcessAllJobs(long maxMillisToWait, long intervalMillis) {
    TestHelper.waitForJobExecutorToProcessAllJobs(
        (ProcessEngineConfigurationImpl) workflowHelper.getProcessEngineConfiguration(),
        maxMillisToWait,
        intervalMillis);
  }

  // -- Clock

  protected Date incrementClock(long milliseconds, boolean waitForJobExecutorToProcessAllJobs) {
    return incrementClock(Duration.ofMillis(milliseconds), waitForJobExecutorToProcessAllJobs);
  }

  protected Date incrementClock(Duration duration, boolean waitForJobExecutorToProcessAllJobs) {
    log.info("Increment clock by {}", duration);
    Date currentTime = workflowHelper.getWorkflowCurrentTime();
    Date nextCurrentTime = dateInFutureFrom(currentTime, duration.toMillis(), ChronoUnit.MILLIS);
    workflowHelper.setWorkflowCurrentTime(nextCurrentTime);
    if (waitForJobExecutorToProcessAllJobs) {
      waitForJobExecutorToProcessAllJobs();
    }
    return workflowHelper.getWorkflowCurrentTime();
  }

  protected Date setClock(Date nextCurrentTime, boolean waitForJobExecutorToProcessAllJobs) {
    workflowHelper.setWorkflowCurrentTime(nextCurrentTime);
    if (waitForJobExecutorToProcessAllJobs) {
      waitForJobExecutorToProcessAllJobs();
    }
    return workflowHelper.getWorkflowCurrentTime();
  }

}
