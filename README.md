# MyCalendar Backend API

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.3-6DB33F?style=flat&logo=spring-boot)
![Java](https://img.shields.io/badge/Java-21-007396?style=flat&logo=openjdk)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-4169E1?style=flat&logo=postgresql)

## 📋 Overview

Backend REST API สำหรับ MyCalendar Application ที่พัฒนาด้วย Spring Boot 3.3.3 และ Java 21

## 🛠️ Technology Stack

| Library | Version | Purpose |
|---------|---------|---------|
| Spring Boot | 3.3.3 | Main Framework |
| Spring Data JPA | 3.x | Database ORM |
| Spring Security | 6.x | Authentication |
| PostgreSQL | Latest | Database |
| JWT (jjwt) | 0.11.5 | Token Auth |
| Swagger/OpenAPI | 2.6.0 | API Docs |
| Lombok | Latest | Boilerplate Reduction |
| ModelMapper | 3.1.1 | DTO Mapping |
| Spring Mail | 3.x | Email Service |
| Google API Client | 2.7.2 | Google OAuth |

---

## 📁 Project Structure

```
src/main/java/com/mycalendar/dev/
│
├── DevApplication.java          # Main Application Entry
│
├── config/                       # Configuration Classes
│   ├── CorsConfig.java          # CORS Settings
│   ├── SecurityConfig.java      # Spring Security
│   ├── JwtConfig.java           # JWT Configuration
│   ├── SwaggerConfig.java       # OpenAPI/Swagger
│   └── ModelMapperConfig.java   # ModelMapper Bean
│
├── controller/v1/               # REST Controllers (API v1)
│   ├── AuthRestController.java      # /v1/auth/*
│   ├── EventRestController.java     # /api/v1/event/*
│   ├── GroupRestController.java     # /api/v1/group/*
│   ├── UserRestController.java      # /api/v1/user/*
│   ├── RoleRestController.java      # /api/v1/role/*
│   └── PermissionRestController.java # /api/v1/permission/*
│
├── entity/                       # JPA Entities
│   ├── BaseEntity.java          # Audit fields (created, updated)
│   ├── User.java                # ผู้ใช้งาน
│   ├── Event.java               # กิจกรรม
│   ├── Group.java               # กลุ่ม
│   ├── Role.java                # บทบาท
│   ├── Permission.java          # สิทธิ์
│   ├── UserGroup.java           # ความสัมพันธ์ User-Group
│   ├── UserGroupId.java         # Composite Key
│   └── UserSocialProvider.java  # OAuth Providers
│
├── enums/                        # Enum Types
│   ├── Priority.java            # LOW, MEDIUM, HIGH
│   ├── RepeatType.java          # NONE, DAILY, WEEKLY, MONTHLY
│   ├── NotificationType.java    # POPUP, EMAIL, PUSH
│   └── SocialProvider.java      # GOOGLE, FACEBOOK, etc.
│
├── exception/                    # Exception Handling
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   └── ... (5 more)
│
├── helper/                       # Helper Classes
│   └── DateTimeHelper.java
│
├── mapper/                       # Object Mappers
│   ├── EventMapper.java
│   └── UserMapper.java
│
├── payload/                      # DTOs (34 files)
│   ├── request/                 # Request DTOs
│   │   ├── LoginRequest.java
│   │   ├── CreateEventRequest.java
│   │   ├── CreateGroupRequest.java
│   │   └── ...
│   └── response/                # Response DTOs
│       ├── ApiResponse.java
│       ├── JwtResponse.java
│       ├── EventResponse.java
│       └── ...
│
├── projection/                   # Query Projections
│   ├── EventProjection.java
│   ├── UserProjection.java
│   └── GroupProjection.java
│
├── repository/                   # JPA Repositories
│   ├── UserRepository.java
│   ├── EventRepository.java
│   ├── GroupRepository.java
│   ├── RoleRepository.java
│   ├── PermissionRepository.java
│   └── ... (4 more)
│
├── security/                     # Security Components
│   ├── JwtTokenProvider.java    # JWT Generation/Validation
│   ├── JwtAuthenticationFilter.java
│   ├── CustomUserDetails.java
│   ├── CustomUserDetailsService.java
│   └── ... (2 more)
│
├── service/                      # Business Logic Layer
│   ├── IAuthService.java
│   ├── IEventService.java
│   ├── IGroupService.java
│   ├── IUserService.java
│   ├── IRoleService.java
│   ├── IPermissionService.java
│   ├── IGoogleAuth.java
│   └── implement/               # Implementations
│       ├── AuthServiceImpl.java
│       ├── EventServiceImpl.java
│       ├── GroupServiceImpl.java
│       ├── UserServiceImpl.java
│       └── ... (3 more)
│
└── util/                         # Utility Classes
    ├── DateUtils.java
    ├── StringUtils.java
    ├── ValidationUtils.java
    └── ... (4 more)
```

---

## 📊 Database Entities

### User
```java
@Entity @Table(name = "users")
public class User extends BaseEntity {
    Long userId;                    // PK
    String username;                // Unique
    String password;                // Encrypted
    String name;
    String email;                   // Unique
    Set<Role> roles;                // ManyToMany
    Set<UserGroup> userGroups;      // OneToMany
    Set<Permission> permissions;    // ManyToMany
    Set<UserSocialProvider> socialProviders; // OneToMany
    String activateCode;
    Date activatedDate;
    String resetPasswordToken;
}
```

### Event
```java
@Entity @Table(name = "events")
public class Event {
    Long eventId;                   // PK
    String title;                   // Required
    String description;             // @Lob for long text
    LocalDateTime startDate;
    LocalDateTime endDate;
    String location;
    Double latitude, longitude;     // GPS coordinates
    LocalDateTime notificationTime;
    String notificationType;        // POPUP, EMAIL, PUSH
    Integer remindBeforeMinutes;
    String repeatType;              // NONE, DAILY, WEEKLY, MONTHLY
    LocalDateTime repeatUntil;
    String color;
    String category;
    String priority;                // "1", "2", "3"
    Boolean pinned;
    String imageUrl;
    Long createById;
    Boolean allDay;
    Group group;                    // ManyToOne (Required)
    Set<User> users;                // ManyToMany
}
```

### Group
```java
@Entity @Table(name = "groups")
public class Group {
    Long groupId;                   // PK
    String groupName;               // Required
    String description;
    Set<UserGroup> userGroups;      // OneToMany
    Set<Event> events;              // OneToMany
    Set<Permission> permissions;    // ManyToMany
}
```

---

## 🔌 REST API Endpoints

### Authentication (`/v1/auth`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/login` | Email/Password Login |
| POST | `/register` | User Registration |
| POST | `/google-sign-in` | Google OAuth Login |
| POST | `/refresh-token` | Refresh JWT |
| POST | `/forgot-password` | Request Password Reset |
| POST | `/reset-password` | Reset Password |
| GET | `/activate` | Activate Account |

### Events (`/api/v1/event`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/all` | Get All Events (Paginated) |
| GET | `/{id}` | Get Event by ID |
| POST | `/create` | Create New Event |
| PUT | `/{id}` | Update Event |
| DELETE | `/{id}` | Delete Event |

### Groups (`/api/v1/group`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get All Groups |
| GET | `/{id}` | Get Group by ID |
| POST | `/` | Create Group |
| PUT | `/{id}` | Update Group |
| DELETE | `/{id}` | Delete Group |
| POST | `/{id}/members` | Add Members |
| DELETE | `/{id}/members/{userId}` | Remove Member |

### Users (`/api/v1/user`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get All Users |
| GET | `/{id}` | Get User by ID |
| GET | `/me` | Get Current User |
| PUT | `/{id}` | Update User |
| DELETE | `/{id}` | Delete User |

### Roles (`/api/v1/role`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get All Roles |
| GET | `/{id}` | Get Role by ID |
| POST | `/` | Create Role |
| PUT | `/{id}` | Update Role |
| DELETE | `/{id}` | Delete Role |

### Permissions (`/api/v1/permission`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get All Permissions |
| POST | `/` | Create Permission |

---

## 🔐 Security

### JWT Authentication
```
Authorization: Bearer <jwt_token>
```

### Token Structure
```json
{
  "sub": "user@example.com",
  "userId": 123,
  "roles": ["ROLE_USER"],
  "iat": 1699999999,
  "exp": 1700099999
}
```

### Google OAuth Flow
1. Frontend sends `idToken` to `/v1/auth/google-sign-in`
2. Backend validates token with Google API
3. Creates user if not exists
4. Returns JWT token

---

## 🚀 Quick Start

### Requirements
- Java 21
- PostgreSQL 14+
- Maven 3.9+

### Configuration
```properties
# src/main/resources/application.properties

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/mycalendar
spring.datasource.username=postgres
spring.datasource.password=password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.secret=your-secret-key
jwt.expiration=86400000

# Google OAuth
google.client-id=your-client-id

# Server
server.port=9001
```

### Build & Run
```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run

# Or with JAR
java -jar target/app.jar
```

### API Documentation
- **Swagger UI**: http://localhost:9001/swagger-ui.html
- **OpenAPI JSON**: http://localhost:9001/v3/api-docs

---

## 📋 Sample Requests

### Login
```bash
curl -X POST http://localhost:9001/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "password"}'
```

### Create Event
```bash
curl -X POST http://localhost:9001/api/v1/event/create \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Meeting",
    "startDate": "2024-01-15T10:00:00",
    "endDate": "2024-01-15T11:00:00",
    "groupId": 1
  }'
```

### Get Events (Paginated)
```bash
curl -X POST http://localhost:9001/api/v1/event/all \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "pageNumber": 1,
    "pageSize": 100,
    "sortBy": "startDate",
    "sortOrder": "DESC",
    "filter": {}
  }'
```

---

## 🏥 Health Check
```bash
curl http://localhost:9001/actuator/health
```

---

## 📄 License

Private Project