
# Messenger Backend (Spring Boot 3, Java 21)

### Quick start
1. `docker compose up -d` (PostgreSQL)
2. `./mvnw spring-boot:run` (or `mvn spring-boot:run`)

OpenAPI: http://localhost:8080/swagger-ui/index.html

### Auth
```
POST /api/v1/auth/register {"username":"alice","displayName":"Alice","password":"secret"}
POST /api/v1/auth/login {"username":"alice","password":"secret"}
```

Use the `accessToken` as `Authorization: Bearer <token>`.

### Features & Implementation Details

#### Tech Stack
- **Framework:** Spring Boot 3.4+
- **Language:** Java 24
- **Database:** PostgreSQL (with Spring Data JPA)
- **Security:** Spring Security with JWT (Stateless)
- **Real-time:** Spring WebSocket (STOMP) for instant message delivery.
- **API Style:** REST with `ProblemDetail` (RFC 7807) for error handling.

#### Domain Model
- **User:** Handles authentication and profiles.
- **Conversation:** Represents a chat between members.
- **Message:** Linked to conversations and users, delivered via WebSockets.
- **CallSession:** Tracks the state of voice/video calls (`INITIATED`, `RINGING`, `ACTIVE`, `ENDED`).

#### Project Structure
- `com.example.messenger.api`: REST Controllers and global error handling.
- `com.example.messenger.domain`: JPA Entities representing the core business data.
- `com.example.messenger.security`: JWT generation, filtering, and authentication logic.
- `com.example.messenger.ws`: WebSocket configuration for STOMP messaging.

#### Real-time Messaging
Messages sent via `POST /api/v1/conversations/{id}/messages` are automatically broadcasted to the WebSocket topic:
`/topic/chat.{conversationId}`
