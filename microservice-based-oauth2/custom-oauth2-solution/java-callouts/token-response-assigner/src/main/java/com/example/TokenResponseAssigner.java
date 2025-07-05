package com.example;

import com.apigee.flow.execution.*;
import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.message.MessageContext;
import com.apigee.flow.execution.spi.Execution;

public class TokenResponseAssigner implements Execution {
  public ExecutionResult execute(MessageContext msgCtx, ExecutionContext execCtx) {
    try {
      // This callout simply passes through, as AssignMessage policy handles response formatting
      return ExecutionResult.SUCCESS;
    } catch (Exception ex) {
      msgCtx.setVariable("token.error", ex.getMessage());
      return ExecutionResult.ABORT;
    }
  }
}
