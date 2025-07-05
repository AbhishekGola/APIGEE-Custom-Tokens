package com.example;

import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.message.MessageContext;
import com.apigee.flow.execution.ExecutionContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenHandlerTest {
    @Test
    void testExecute() {
        RefreshTokenHandler handler = new RefreshTokenHandler();
        MessageContext msgCtx = Mockito.mock(MessageContext.class);
        ExecutionContext execCtx = Mockito.mock(ExecutionContext.class);
        Mockito.when(msgCtx.getVariable("request.formparam.refresh_token")).thenReturn("refresh-token");
        Mockito.when(msgCtx.getVariable("request.formparam.client_id")).thenReturn("client-id");
        ExecutionResult result = handler.execute(msgCtx, execCtx);
        assertNotNull(result);
    }
}
