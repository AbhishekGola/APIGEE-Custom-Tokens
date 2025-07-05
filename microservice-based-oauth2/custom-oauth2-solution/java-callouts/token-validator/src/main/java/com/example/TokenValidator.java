package com.example;

import com.apigee.flow.execution.*;
import com.apigee.flow.message.MessageContext;
import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.spi.Execution;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import com.datastax.oss.driver.api.core.CqlSession;

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

      // Token persistence logic
      try {
        msgCtx.setVariable("token.debug", "Connecting to Cassandra");
        try (CqlSession session = CqlSession.builder()
            .withLocalDatacenter("datacenter1")
            .withKeyspace("oauth")
            .build()) {

          String clientId = jwt.getSubject();
          String scope = jwt.getClaim("scope").asString();
          String jwtId = jwt.getId();
          String refreshToken = JWT.create()
              .withIssuer("https://api.example.com")
              .withSubject(clientId)
              .withClaim("scope", scope)
              .withIssuedAt(Date.from(Instant.now()))
              .withExpiresAt(Date.from(Instant.now().plus(30, ChronoUnit.DAYS)))
              .sign(algorithm);
          String query = "INSERT INTO tokens (id, jwt, refresh_token, client_id, scope, created_at, expires_at, refresh_expires_at, token_type, revoked) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
          session.execute(query, jwtId, jwt.getToken(), refreshToken, clientId, scope, new Date(), Date.from(jwt.getExpiresAt().toInstant()), Date.from(Instant.now().plus(30, ChronoUnit.DAYS)), "Bearer", false);
          msgCtx.setVariable("token.debug", "Token information persisted to Cassandra");
        }
      } catch (Exception cqlEx) {
        msgCtx.setVariable("token.debug", "Cassandra unavailable, attempting to store token in local CSV file: " + cqlEx.getMessage());
        try {
          String clientId = jwt.getSubject();
          String scope = jwt.getClaim("scope").asString();
          String jwtId = jwt.getId();
          String refreshToken = JWT.create()
              .withIssuer("https://api.example.com")
              .withSubject(clientId)
              .withClaim("scope", scope)
              .withIssuedAt(Date.from(Instant.now()))
              .withExpiresAt(Date.from(Instant.now().plus(30, ChronoUnit.DAYS)))
              .sign(algorithm);
          String nowStr = Instant.now().toString();
          String expiryStr = jwt.getExpiresAt().toString();
          String refreshExpiryStr = Instant.now().plus(30, ChronoUnit.DAYS).toString();
          String csvLine = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
            jwtId, jwt.getToken(), refreshToken, clientId, scope, nowStr, expiryStr, refreshExpiryStr, "Bearer");
          java.nio.file.Path path = java.nio.file.Paths.get("/tmp/apigee_tokens.csv");
          java.nio.file.Files.write(path, csvLine.getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
          msgCtx.setVariable("token.debug", "Token written to local CSV file: /tmp/apigee_tokens.csv");
        } catch (Exception fileEx) {
          msgCtx.setVariable("token.error", "Failed to write token to local CSV: " + fileEx.getMessage());
          msgCtx.setVariable("token.debug", "CSV fallback failed: " + fileEx.toString());
          return ExecutionResult.ABORT;
        }
      }
      return ExecutionResult.SUCCESS;

    } catch (Exception e) {
      msgCtx.setVariable("token.valid", false);
      msgCtx.setVariable("token.error", e.getMessage());
      return ExecutionResult.ABORT;
    }
  }
}
