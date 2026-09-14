package com.blps.app;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

public class BpmnDeployTest {

    @Test
    public void testBpmnModel() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/processes/skillbox.bpmn")) {
            BpmnModelInstance model = Bpmn.readModelFromStream(is);
            Bpmn.validateModel(model);
            System.out.println(">>> BPMN MODEL VALIDATION SUCCESSFUL! <<<");
            System.out.println("СТАРЫЙ БОГ");
        }
    }
}
