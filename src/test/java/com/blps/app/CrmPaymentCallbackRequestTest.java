package com.blps.app;

import com.blps.app.web.dto.CrmPaymentCallbackRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CrmPaymentCallbackRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testDeserializeWithBooleanSuccess() throws Exception {
        String json = "{\"invoiceId\":\"inv-100\",\"success\":true}";
        CrmPaymentCallbackRequest req = objectMapper.readValue(json, CrmPaymentCallbackRequest.class);
        Assertions.assertEquals("inv-100", req.invoiceId());
        Assertions.assertTrue(req.success());
    }

    @Test
    void testDeserializeWithStringSuccessTrue() throws Exception {
        // 1C sends string "true"
        String json = "{\"invoiceId\":\"inv-200\",\"success\":\"true\"}";
        CrmPaymentCallbackRequest req = objectMapper.readValue(json, CrmPaymentCallbackRequest.class);
        Assertions.assertEquals("inv-200", req.invoiceId());
        Assertions.assertTrue(req.success());
    }

    @Test
    void testDeserializeWithStringSuccessFalse() throws Exception {
        // 1C sends string "false"
        String json = "{\"invoiceId\":\"inv-300\",\"success\":\"false\"}";
        CrmPaymentCallbackRequest req = objectMapper.readValue(json, CrmPaymentCallbackRequest.class);
        Assertions.assertEquals("inv-300", req.invoiceId());
        Assertions.assertFalse(req.success());
    }
}
