# Custom OAuth2 Solution

This repository contains a custom OAuth2 implementation with Java callouts, Apigee proxy configuration, Cassandra schema, and CI/CD workflows.
# Custom OAuth2 Proxy (Apigee OPDK + Cassandra)

## Features

- JWT access tokens with HMAC signing
- Refresh token support with rotation
- Custom Cassandra storage layer
- Java-based Apigee policy extensions
- Fully configurable via encrypted KVM

## Structure

- `java-callouts/` — Token generator, validator, refresher
- `cassandra/` — DB schema
- `apiproxy/` — Proxy artifacts
- `test/` — Postman collections
- `config/` — KVM template
- `ci/` & `cd/` — GitHub Actions pipelines

## Deployment

```bash
apigeetool deployproxy -n custom-oauth2-proxy -e test -o your-org -u your-user
