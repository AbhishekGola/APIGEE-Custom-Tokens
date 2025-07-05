package com.example;

import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.message.MessageContext;
import com.apigee.flow.execution.ExecutionContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class TokenRevokerTest {
    @Test
    void testExecute() {
        TokenRevoker revoker = new TokenRevoker();
        MessageContext msgCtx = Mockito.mock(MessageContext.class);
        ExecutionContext execCtx = Mockito.mock(ExecutionContext.class);
        Mockito.when(msgCtx.getVariable("request.formparam.token")).thenReturn("token-id");
        ExecutionResult result = revoker.execute(msgCtx, execCtx);
        assertNotNull(result);
    }
}
