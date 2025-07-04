CREATE KEYSPACE IF NOT EXISTS oauth WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE oauth;

CREATE TABLE IF NOT EXISTS oauth_tokens (
  token_id TEXT PRIMARY KEY,
  client_id TEXT,
  scope TEXT,
  issued_at TIMESTAMP,
  expires_at TIMESTAMP,
  token_type TEXT,
  token_value TEXT,
  revoked BOOLEAN
) WITH default_time_to_live = 3600;

CREATE TABLE IF NOT EXISTS refresh_tokens (
  refresh_token TEXT PRIMARY KEY,
  client_id TEXT,
  issued_at TIMESTAMP,
  expires_at TIMESTAMP,
  revoked BOOLEAN
) WITH default_time_to_live = 604800;
