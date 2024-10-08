package org.camunda.bpm.extension.test.report.tags;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.tngtech.jgiven.annotation.IsTag;

@IsTag(name = "Tasks", description = "Error / Issue", color = "rgb(255,64,64)")
@Retention(RetentionPolicy.RUNTIME)
public @interface Issue {
  String[] value();
}
