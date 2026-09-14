package com.blps.app.application.bpmn.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("generateCertificateDelegate")
public class GenerateCertificateDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(GenerateCertificateDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String login = (String) execution.getVariable("login");
        Object courseId = execution.getVariable("courseId");

        String certNumber = "CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Generated course completion certificate: {} for user: {}, course: {}", certNumber, login, courseId);

        execution.setVariable("certificateNumber", certNumber);
        execution.setVariable("certificateReady", true);
    }
}
