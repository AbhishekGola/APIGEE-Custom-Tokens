package com.example;

import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.message.MessageContext;
import com.apigee.flow.execution.ExecutionContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class TokenResponseAssignerTest {
    @Test
    void testExecute() {
        TokenResponseAssigner assigner = new TokenResponseAssigner();
        MessageContext msgCtx = Mockito.mock(MessageContext.class);
        ExecutionContext execCtx = Mockito.mock(ExecutionContext.class);
        ExecutionResult result = assigner.execute(msgCtx, execCtx);
        assertEquals(ExecutionResult.SUCCESS, result);
    }
}
