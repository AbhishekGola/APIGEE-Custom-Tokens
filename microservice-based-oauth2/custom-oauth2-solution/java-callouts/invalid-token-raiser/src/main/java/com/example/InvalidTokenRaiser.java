package com.example;

import com.apigee.flow.execution.*;
import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.message.MessageContext;
import com.apigee.flow.execution.spi.Execution;

public class InvalidTokenRaiser implements Execution {
  public ExecutionResult execute(MessageContext msgCtx, ExecutionContext execCtx) {
    msgCtx.setVariable("token.valid", false);
    msgCtx.setVariable("token.error", "The access token is invalid or expired.");
    return ExecutionResult.SUCCESS;
  }
}
