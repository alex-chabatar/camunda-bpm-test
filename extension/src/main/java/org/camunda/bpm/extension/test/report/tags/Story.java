package org.camunda.bpm.extension.test.report.tags;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.tngtech.jgiven.annotation.IsTag;

@IsTag(name = "Stories", description = "User Story / Feature", color = "rgb(69,158,19)")
@Retention(RetentionPolicy.RUNTIME)
public @interface Story {
  String[] value();
}
