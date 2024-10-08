package org.camunda.bpm.extension.test.report.tags;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.tngtech.jgiven.annotation.IsTag;

@IsTag(name = "Epics")
@Retention(RetentionPolicy.RUNTIME)
public @interface Epics {
}
