package org.camunda.bpm.extension.test.report.tags;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.tngtech.jgiven.annotation.IsTag;

@Epics
@IsTag(description = "Epic / User Story", color = "rgb(139,115,85)")
@Retention(RetentionPolicy.RUNTIME)
public @interface Epic {
  String value();
}
