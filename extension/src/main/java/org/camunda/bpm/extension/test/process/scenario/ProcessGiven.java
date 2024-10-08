package org.camunda.bpm.extension.test.process.scenario;

import java.util.HashMap;
import java.util.Map;

import com.tngtech.jgiven.annotation.As;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.SingleQuoted;
import com.tngtech.jgiven.integration.spring.JGivenStage;

@JGivenStage
public class ProcessGiven<SELF extends ProcessGiven<SELF>> extends AbstractProcessStage<SELF> {

  @ProvidedScenarioState
  private String businessKey;

  @ProvidedScenarioState
  private Map<String, Object> model;

  @ProvidedScenarioState
  private String beforeActivity;

  @ProvidedScenarioState
  private String afterActivity;

  public SELF a_process_engine() {
    return self();
  }

  public SELF a_business_key(@SingleQuoted String businessKey) {
    this.businessKey = businessKey;
    return self();
  }

  public SELF a_process_model() {
    model = new HashMap<>();
    return self();
  }

  public SELF a_process_model(Map<String, Object> model) {
    this.model = model;
    return self();
  }

  @As("with $key = $value")
  public SELF with(@SingleQuoted String key, @SingleQuoted Object value) {
    model.put(key, value);
    return self();
  }

  @As("with $values")
  public SELF with(Map<String, Object> values) {
    model.putAll(values);
    return self();
  }

  @As("before $activity")
  public SELF before_activity(@SingleQuoted String activity) {
    this.beforeActivity = activity;
    this.afterActivity = null;
    return self();
  }

  @As("after $activity")
  public SELF after_activity(@SingleQuoted String activity) {
    this.afterActivity = activity;
    this.beforeActivity = null;
    return self();
  }

}
