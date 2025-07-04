# OAuth2 Microservice

A microservice-based OAuth2 implementation using Spring Boot.
# OAuth2 Token Microservice (Java + Cassandra)

## Features
- JWT-based access tokens
- UUID refresh tokens
- Token storage in Cassandra
- Token validation, refresh, revocation
- Ready for deployment behind Apigee

## Tech Stack
- Spring Boot 3
- Java 17+
- Cassandra 4.x
- Java JWT (Auth0)

## Endpoints
| Method | Path             | Description              |
|--------|------------------|--------------------------|
| POST   | `/token/generate`| Issue new tokens         |
| POST   | `/token/validate`| Verify access token      |
| POST   | `/token/refresh` | Refresh access token     |
| POST   | `/token/revoke`  | Revoke an access token   |

## Build & Run
```bash
mvn clean spring-boot:run
