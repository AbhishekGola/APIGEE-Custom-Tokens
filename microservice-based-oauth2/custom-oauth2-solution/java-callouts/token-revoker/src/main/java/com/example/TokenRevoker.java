package com.example;

import com.apigee.flow.execution.*;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.message.MessageContext;
import com.datastax.oss.driver.api.core.*;
import com.datastax.oss.driver.api.core.cql.*;
import java.time.Instant;

public class TokenRevoker implements Execution {
  public ExecutionResult execute(MessageContext msgCtx, ExecutionContext execCtx) {
    try {
      String token = msgCtx.getVariable("request.formparam.token");
      String cassandraHost = msgCtx.getVariable("cassandra.host");
      String cassandraDatacenter = msgCtx.getVariable("cassandra.datacenter");
      String cassandraKeyspace = msgCtx.getVariable("cassandra.keyspace");
      if (cassandraHost == null) cassandraHost = "127.0.0.1";
      if (cassandraDatacenter == null) cassandraDatacenter = "datacenter1";
      if (cassandraKeyspace == null) cassandraKeyspace = "oauth";

      try {
        msgCtx.setVariable("token.debug", "Connecting to Cassandra");
        try (CqlSession session = CqlSession.builder()
            .withLocalDatacenter("datacenter1")
            .withKeyspace("oauth")
            .build()) {

          session.execute(SimpleStatement.builder(
              "UPDATE oauth_tokens SET revoked = true WHERE token_id = ?")
              .addPositionalValue(token)
              .build());
        }
      } catch (Exception cqlEx) {
        msgCtx.setVariable("token.debug", "Cassandra unavailable, attempting to store token in local CSV file: " + cqlEx.getMessage());
        try {
          String csvLine = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
            token, "", "", "", Instant.now().toString(), "", "", "revoked", true);
          java.nio.file.Path path = java.nio.file.Paths.get("/tmp/apigee_tokens.csv");
          java.nio.file.Files.write(path, csvLine.getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
          msgCtx.setVariable("token.debug", "Token written to local CSV file: /tmp/apigee_tokens.csv");
        } catch (Exception fileEx) {
          msgCtx.setVariable("token.error", "Failed to write token to local CSV: " + fileEx.getMessage());
          msgCtx.setVariable("token.debug", "CSV fallback failed: " + fileEx.toString());
          return ExecutionResult.ABORT;
        }
      }
      msgCtx.setVariable("revoke.success", true);
      return ExecutionResult.SUCCESS;
    } catch (Exception ex) {
      msgCtx.setVariable("revoke.success", false);
      msgCtx.setVariable("revoke.error", ex.getMessage());
      return ExecutionResult.ABORT;
    }
  }
}
