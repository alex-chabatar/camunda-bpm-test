package org.camunda.bpm.examples;

import org.camunda.bpm.extension.test.process.scenario.ProcessGiven;
import org.camunda.bpm.extension.test.process.scenario.ProcessThen;
import org.camunda.bpm.extension.test.process.scenario.ProcessWhen;
import org.camunda.bpm.extension.test.spring.AbstractSpringTest;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "org.camunda.bpm.examples" // add our namespace
})
public abstract class AbstractExampleTest extends AbstractSpringTest<ProcessGiven<?>, ProcessWhen<?>, ProcessThen<?>> {
}