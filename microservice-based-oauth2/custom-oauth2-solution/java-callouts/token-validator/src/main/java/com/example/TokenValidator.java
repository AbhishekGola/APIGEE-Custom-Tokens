package com.example;

import com.apigee.flow.execution.*;
import com.apigee.flow.message.MessageContext;
import com.apigee.flow.execution.ExecutionContext;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

public class TokenValidator implements Execution {
  public ExecutionResult execute(MessageContext msgCtx, ExecutionContext execCtx) {
    try {
      String authHeader = msgCtx.getVariable("request.header.Authorization");
      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        msgCtx.setVariable("token.valid", false);
        return ExecutionResult.SUCCESS;
      }

      String token = authHeader.replace("Bearer ", "").trim();
      Algorithm algorithm = Algorithm.HMAC256("super-secret"); // Pull from KVM if needed

      JWTVerifier verifier = JWT.require(algorithm)
          .withIssuer("https://api.example.com")
          .build();

      DecodedJWT jwt = verifier.verify(token);
      msgCtx.setVariable("token.valid", true);
      msgCtx.setVariable("token.scope", jwt.getClaim("scope").asString());
      msgCtx.setVariable("token.client_id", jwt.getSubject());
      return ExecutionResult.SUCCESS;

    } catch (Exception e) {
      msgCtx.setVariable("token.valid", false);
      msgCtx.setVariable("token.error", e.getMessage());
      return ExecutionResult.ABORT;
    }
  }
}
