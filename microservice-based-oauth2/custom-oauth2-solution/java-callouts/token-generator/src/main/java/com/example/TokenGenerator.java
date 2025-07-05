package com.example;

import com.apigee.flow.execution.*;
import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.MessageContext;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.datastax.oss.driver.api.core.*;
import com.datastax.oss.driver.api.core.cql.*;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class TokenGenerator implements Execution {
  public ExecutionResult execute(MessageContext msgCtx, ExecutionContext execCtx) {
    try {
      String clientId = msgCtx.getVariable("request.queryparam.client_id");
      String scope = msgCtx.getVariable("request.queryparam.scope");

      Instant now = Instant.now();
      Instant expiry = now.plusSeconds(3600);

      Algorithm algorithm = Algorithm.HMAC256("super-secret"); // Or pull from KVM later
      String jwt = JWT.create()
          .withIssuer("https://api.example.com")
          .withSubject(clientId)
          .withClaim("scope", scope)
          .withIssuedAt(Date.from(now))
          .withExpiresAt(Date.from(expiry))
          .sign(algorithm);

      String refreshToken = UUID.randomUUID().toString();
      Instant refreshExpiry = now.plusSeconds(604800);

      try (CqlSession session = CqlSession.builder()
          .withLocalDatacenter("datacenter1")
          .withKeyspace("oauth")
          .build()) {

        session.execute(SimpleStatement.builder(
            "INSERT INTO oauth_tokens (token_id, client_id, scope, issued_at, expires_at, token_type, token_value, revoked) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
            .addPositionalValues(jwt, clientId, scope, now, expiry, "Bearer", jwt, false)
            .build());

        session.execute(SimpleStatement.builder(
            "INSERT INTO refresh_tokens (refresh_token, client_id, issued_at, expires_at, revoked) VALUES (?, ?, ?, ?, ?)")
            .addPositionalValues(refreshToken, clientId, now, refreshExpiry, false)
            .build());
      } catch (Exception cqlEx) {
        // Cassandra unavailable, fallback to CSV
        try {
          String fileName = System.getProperty("java.io.tmpdir") + java.io.File.separator + "apigee_tokens.csv";
          java.nio.file.Path path = java.nio.file.Paths.get(fileName);
          if (!java.nio.file.Files.exists(path)) {
            java.nio.file.Files.createFile(path);
          }
          String csvLine = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
            jwt, refreshToken, clientId, scope, now.toString(), expiry.toString(), refreshExpiry.toString(), "Bearer", false);
          java.nio.file.Files.write(path, csvLine.getBytes(), java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception fileEx) {
          msgCtx.setVariable("token.error", "Failed to write token to local CSV: " + fileEx.getMessage());
          return ExecutionResult.ABORT;
        }
      }

      msgCtx.setVariable("generated.token", jwt);
      msgCtx.setVariable("generated.refresh_token", refreshToken);
      msgCtx.setVariable("generated.expires_in", 3600);
      return ExecutionResult.SUCCESS;

    } catch (Exception ex) {
      msgCtx.setVariable("token.error", ex.getMessage());
      return ExecutionResult.ABORT;
    }
  }
}
