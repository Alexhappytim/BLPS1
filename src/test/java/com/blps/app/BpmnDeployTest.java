package com.blps.app;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Collection;

public class BpmnDeployTest {

    @Test
    public void testVariableSerialization() {
        for (java.lang.reflect.Method m : org.camunda.bpm.engine.variable.Variables.class.getMethods()) {
            if (m.getName().toLowerCase().contains("json") || m.getName().toLowerCase().contains("object")) {
                System.out.println("Variables method: " + m.getName() + " -> " + m.getReturnType());
            }
        }
    }

    @Test
    public void testBpmnModel() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/processes/skillbox.bpmn")) {
            Assertions.assertNotNull(is, "skillbox.bpmn must be present in classpath");
            BpmnModelInstance model = Bpmn.readModelFromStream(is);
            Bpmn.validateModel(model);
            System.out.println(">>> BPMN MODEL VALIDATION SUCCESSFUL! <<<");
        }
    }

    @Test
    public void testAllUserTasksHaveFormsAndCandidateGroups() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/processes/skillbox.bpmn")) {
            Assertions.assertNotNull(is, "skillbox.bpmn must be present in classpath");
            BpmnModelInstance model = Bpmn.readModelFromStream(is);
            Collection<UserTask> userTasks = model.getModelElementsByType(UserTask.class);

            System.out.println("Found " + userTasks.size() + " user tasks in model:");
            for (UserTask task : userTasks) {
                String taskId = task.getId();
                String taskName = task.getName();
                String formRef = task.getCamundaFormRef();
                String candidateGroups = task.getCamundaCandidateGroups();

                System.out.println("Task [" + taskId + "] '" + taskName + "' -> formRef: " + formRef + ", groups: " + candidateGroups);

                // Verify login/register are not in BPMN
                Assertions.assertNotEquals("Activity_AuthChoice", taskId, "Activity_AuthChoice must not be in BPMN");
                Assertions.assertNotEquals("Activity_Register", taskId, "Activity_Register must not be in BPMN");
                Assertions.assertNotEquals("Activity_Login", taskId, "Activity_Login must not be in BPMN");

                Assertions.assertNotNull(formRef, "UserTask " + taskId + " (" + taskName + ") must have a camunda:formRef!");
                Assertions.assertFalse(formRef.isBlank(), "UserTask " + taskId + " formRef cannot be blank!");

                // Check form file exists
                String formPath = "/processes/" + formRef + ".form";
                try (InputStream formIs = getClass().getResourceAsStream(formPath)) {
                    Assertions.assertNotNull(formIs, "Form file " + formPath + " must exist on classpath for task " + taskId);
                }

                Assertions.assertNotNull(candidateGroups, "UserTask " + taskId + " must have camunda:candidateGroups!");
                Assertions.assertFalse(candidateGroups.isBlank(), "UserTask " + taskId + " candidateGroups cannot be blank!");
            }
        }
    }
}
