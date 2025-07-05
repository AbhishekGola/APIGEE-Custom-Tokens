package com.example;

import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.message.MessageContext;
import com.apigee.flow.execution.ExecutionContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class TokenValidatorTest {
    @Test
    void testExecute() {
        TokenValidator validator = new TokenValidator();
        MessageContext msgCtx = Mockito.mock(MessageContext.class);
        ExecutionContext execCtx = Mockito.mock(ExecutionContext.class);
        Mockito.when(msgCtx.getVariable("request.header.Authorization")).thenReturn("Bearer test-token");
        ExecutionResult result = validator.execute(msgCtx, execCtx);
        assertNotNull(result);
    }
}
