package com.example;

import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.message.MessageContext;
import com.apigee.flow.execution.ExecutionContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class TokenGeneratorTest {
    @Test
    void testExecute() {
        TokenGenerator generator = new TokenGenerator();
        MessageContext msgCtx = Mockito.mock(MessageContext.class);
        ExecutionContext execCtx = Mockito.mock(ExecutionContext.class);

        Mockito.when(msgCtx.getVariable("request.queryparam.client_id")).thenReturn("test-client");
        Mockito.when(msgCtx.getVariable("request.queryparam.scope")).thenReturn("read write");

        ExecutionResult result = generator.execute(msgCtx, execCtx);

        assertEquals(ExecutionResult.SUCCESS, result);
        Mockito.verify(msgCtx).setVariable(Mockito.eq("generated.token"), Mockito.anyString());
        Mockito.verify(msgCtx).setVariable(Mockito.eq("generated.refresh_token"), Mockito.anyString());
        Mockito.verify(msgCtx).setVariable("generated.expires_in", 3600);
    }
}
