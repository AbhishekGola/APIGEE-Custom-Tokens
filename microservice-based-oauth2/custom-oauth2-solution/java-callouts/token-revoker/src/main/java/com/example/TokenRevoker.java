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

      try (CqlSession session = CqlSession.builder()
          .addContactPoint(new java.net.InetSocketAddress(cassandraHost, 9042))
          .withLocalDatacenter(cassandraDatacenter)
          .withKeyspace(cassandraKeyspace)
          .build()) {
        session.execute(SimpleStatement.builder(
            "UPDATE oauth_tokens SET revoked = true WHERE token_id = ?")
            .addPositionalValue(token)
            .build());
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
