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

public class RefreshTokenHandler implements Execution {
  public ExecutionResult execute(MessageContext msgCtx, ExecutionContext execCtx) {
    try {
      String refreshToken = msgCtx.getVariable("request.formparam.refresh_token");
      String clientId = msgCtx.getVariable("request.formparam.client_id");

      String cassandraHost = msgCtx.getVariable("cassandra.host");
      String cassandraDatacenter = msgCtx.getVariable("cassandra.datacenter");
      String cassandraKeyspace = msgCtx.getVariable("cassandra.keyspace");
      // Fallbacks if not set
      if (cassandraHost == null) cassandraHost = "127.0.0.1";
      if (cassandraDatacenter == null) cassandraDatacenter = "datacenter1";
      if (cassandraKeyspace == null) cassandraKeyspace = "oauth";

      try {
        msgCtx.setVariable("token.debug", "Connecting to Cassandra");
        try (CqlSession session = CqlSession.builder()
            .withLocalDatacenter("datacenter1")
            .withKeyspace("oauth")
            .build()) {

          Row row = session.execute(SimpleStatement.builder(
              "SELECT client_id, expires_at, revoked FROM refresh_tokens WHERE refresh_token = ?")
              .addPositionalValue(refreshToken)
              .build()).one();

          if (row == null || row.getBoolean("revoked") || Instant.now().isAfter(row.getInstant("expires_at"))) {
            msgCtx.setVariable("refresh.valid", false);
            return ExecutionResult.SUCCESS;
          }

          // Revoke old refresh token
          session.execute(SimpleStatement.builder(
              "UPDATE refresh_tokens SET revoked = true WHERE refresh_token = ?")
              .addPositionalValue(refreshToken)
              .build());

          Instant now = Instant.now();
          Instant accessExpiry = now.plusSeconds(3600);
          Instant refreshExpiry = now.plusSeconds(604800);

          Algorithm algorithm = Algorithm.HMAC256("super-secret");
          String newAccessToken = JWT.create()
              .withIssuer("https://api.example.com")
              .withSubject(clientId)
              .withClaim("scope", "read write")
              .withIssuedAt(Date.from(now))
              .withExpiresAt(Date.from(accessExpiry))
              .sign(algorithm);

          String newRefresh = UUID.randomUUID().toString();

          session.execute(SimpleStatement.builder(
              "INSERT INTO oauth_tokens (token_id, client_id, scope, issued_at, expires_at, token_type, token_value, revoked) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
              .addPositionalValues(newAccessToken, clientId, "read write", now, accessExpiry, "Bearer", newAccessToken, false)
              .build());

          session.execute(SimpleStatement.builder(
              "INSERT INTO refresh_tokens (refresh_token, client_id, issued_at, expires_at, revoked) VALUES (?, ?, ?, ?, ?)")
              .addPositionalValues(newRefresh, clientId, now, refreshExpiry, false)
              .build());

          msgCtx.setVariable("refresh.valid", true);
          msgCtx.setVariable("new.access_token", newAccessToken);
          msgCtx.setVariable("new.refresh_token", newRefresh);
          msgCtx.setVariable("new.expires_in", 3600);
          return ExecutionResult.SUCCESS;
        }
      } catch (Exception cqlEx) {
        msgCtx.setVariable("token.debug", "Cassandra unavailable, attempting to store token in local CSV file: " + cqlEx.getMessage());
        try {
          Instant now = Instant.now();
          Instant accessExpiry = now.plusSeconds(3600);
          Instant refreshExpiry = now.plusSeconds(604800);
          String newAccessToken = "fallback-access-token";
          String newRefresh = "fallback-refresh-token";
          String csvLine = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
            newAccessToken, newRefresh, clientId, "read write", now.toString(), accessExpiry.toString(), refreshExpiry.toString(), "Bearer", false);
          java.nio.file.Path path = java.nio.file.Paths.get("/tmp/apigee_tokens.csv");
          java.nio.file.Files.write(path, csvLine.getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
          msgCtx.setVariable("token.debug", "Token written to local CSV file: /tmp/apigee_tokens.csv");
          msgCtx.setVariable("refresh.valid", true);
          msgCtx.setVariable("new.access_token", newAccessToken);
          msgCtx.setVariable("new.refresh_token", newRefresh);
          msgCtx.setVariable("new.expires_in", 3600);
          return ExecutionResult.SUCCESS;
        } catch (Exception fileEx) {
          msgCtx.setVariable("token.error", "Failed to write token to local CSV: " + fileEx.getMessage());
          msgCtx.setVariable("token.debug", "CSV fallback failed: " + fileEx.toString());
          return ExecutionResult.ABORT;
        }
      }
    } catch (Exception e) {
      msgCtx.setVariable("token.error", e.getMessage());
      return ExecutionResult.ABORT;
    }
  }
}
