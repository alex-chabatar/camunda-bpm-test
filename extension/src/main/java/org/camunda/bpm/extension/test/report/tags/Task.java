package org.camunda.bpm.extension.test.report.tags;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.tngtech.jgiven.annotation.IsTag;

@IsTag(name = "Tasks", description = "task", color = "rgb(28,134,238)")
@Retention(RetentionPolicy.RUNTIME)
public @interface Task {
  String[] value();
}
