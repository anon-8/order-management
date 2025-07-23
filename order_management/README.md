# Order Management System

A Domain-Driven Design (DDD) implementation of an order management system with customer orders and manufacturing orders, built with Spring Boot 3.5 and Java 21.

## Architecture Overview

This application follows DDD principles with:
- **Customer Order Module**: Handles customer order lifecycle
- **Manufacturing Order Module**: Manages manufacturing processes
- **Shared Kernel**: Common domain concepts and events
- **Event-Driven Architecture**: Asynchronous communication between modules

## Order Lifecycle

1. **Customer places order** → Status: `PLACED`
2. **Order confirmed** → Status: `CONFIRMED` + Creates manufacturing order with `PENDING` status
3. **Manufacturing starts** → Manufacturing: `IN_PROGRESS`, Customer: `MANUFACTURING_IN_PROGRESS`
4. **Manufacturing completes** → Manufacturing: `COMPLETED`, Customer: `MANUFACTURING_COMPLETED`
5. **Order shipped & delivered** → Customer: `SHIPPED` → `DELIVERED`

## Prerequisites

- Docker and Docker Compose
- Java 21 (for local development)
- Maven 3.9+ (for local development)

## Quick Start

```bash
# Clone the repository
git clone <repository-url>
cd order_management

# Start the application
docker-compose up -d

# Check application status
curl http://localhost:8080/actuator/health
```

**Services:**
- Application: http://localhost:8080
- Database: PostgreSQL on port 5432
- Health Check: http://localhost:8080/actuator/health

### Local Development

```bash
# Start only PostgreSQL
docker-compose up postgres -d

# Run application locally
mvn clean package -DskipTests
java -jar application/target/order-management-application-0.0.1.jar
```

## API Usage

### Customer Orders

#### 1. Place a Customer Order

```bash
curl -X POST http://localhost:8080/api/customer-orders \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "123e4567-e89b-12d3-a456-426614174000",
    "customerId": "123e4567-e89b-12d3-a456-426614174001",
    "customerName": "John Doe",
    "customerEmail": "john.doe@example.com",
    "customerAddress": "123 Main St, City, State 12345",
    "items": [
      {
        "productCode": "PRODUCT-001",
        "description": "Premium Widget",
        "quantity": 2,
        "unitPrice": 29.99,
        "currency": "USD"
      }
    ]
  }'
```

#### 2. Confirm Order (Triggers Manufacturing)

```bash
curl -X POST http://localhost:8080/api/customer-orders/123e4567-e89b-12d3-a456-426614174000/confirm
```

#### 3. Get Order Status

```bash
curl http://localhost:8080/api/customer-orders/123e4567-e89b-12d3-a456-426614174000
```

#### 4. Update Order Status

```bash
curl -X PUT http://localhost:8080/api/customer-orders/123e4567-e89b-12d3-a456-426614174000/status \
  -H "Content-Type: application/json" \
  -d '{"newStatus": "SHIPPED"}'
```

#### 5. Cancel Order

```bash
curl -X POST http://localhost:8080/api/customer-orders/123e4567-e89b-12d3-a456-426614174000/cancel \
  -H "Content-Type: application/json" \
  -d '{"reason": "Customer requested cancellation"}'
```

### Manufacturing Orders

#### 1. Get Manufacturing Order

```bash
curl http://localhost:8080/api/manufacturing-orders/123e4567-e89b-12d3-a456-426614174000
```

#### 2. Start Manufacturing

```bash
curl -X POST http://localhost:8080/api/manufacturing-orders/123e4567-e89b-12d3-a456-426614174000/start
```

#### 3. Complete Manufacturing

```bash
curl -X POST http://localhost:8080/api/manufacturing-orders/123e4567-e89b-12d3-a456-426614174000/complete
```

#### 4. Change Manufacturing Status

```bash
curl -X PUT http://localhost:8080/api/manufacturing-orders/123e4567-e89b-12d3-a456-426614174000/status \
  -H "Content-Type: application/json" \
  -d '{"newStatus": "IN_PROGRESS"}'
```

## Complete Workflow Example

```bash
# 1. Place order
ORDER_ID="123e4567-e89b-12d3-a456-426614174000"
curl -X POST http://localhost:8080/api/customer-orders \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"customerId\": \"123e4567-e89b-12d3-a456-426614174001\",
    \"customerName\": \"John Doe\",
    \"customerEmail\": \"john.doe@example.com\",
    \"customerAddress\": \"123 Main St, City, State 12345\",
    \"items\": [{
      \"productCode\": \"PRODUCT-001\",
      \"description\": \"Premium Widget\",
      \"quantity\": 2,
      \"unitPrice\": 29.99,
      \"currency\": \"USD\"
    }]
  }"

# 2. Confirm order (creates manufacturing order)
curl -X POST http://localhost:8080/api/customer-orders/$ORDER_ID/confirm

# 3. Start manufacturing (updates customer order status)
curl -X POST http://localhost:8080/api/manufacturing-orders/$ORDER_ID/start

# 4. Complete manufacturing (updates customer order status)
curl -X POST http://localhost:8080/api/manufacturing-orders/$ORDER_ID/complete

# 5. Ship order
curl -X PUT http://localhost:8080/api/customer-orders/$ORDER_ID/status \
  -H "Content-Type: application/json" \
  -d '{"newStatus": "SHIPPED"}'

# 6. Mark as delivered
curl -X PUT http://localhost:8080/api/customer-orders/$ORDER_ID/status \
  -H "Content-Type: application/json" \
  -d '{"newStatus": "DELIVERED"}'

# Check final status
curl http://localhost:8080/api/customer-orders/$ORDER_ID
```

## Configuration

The application uses the following default settings:

- **Database**: PostgreSQL on port 5432
- **Application**: Spring Boot on port 8080
- **Management**: Actuator endpoints on port 8081
- **Profile**: `docker` (optimized for containers)

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `docker` |
| `DB_HOST` | Database host | `postgres` |
| `DB_PORT` | Database port | `5432` |
| `DB_NAME` | Database name | `order_management` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `postgres` |

## Monitoring

### Health Checks

```bash
# Application health
curl http://localhost:8080/actuator/health

# Detailed health info
curl http://localhost:8080/actuator/health/readiness
curl http://localhost:8080/actuator/health/liveness
```


### Logs

```bash
# View application logs
docker-compose logs -f app

# View database logs
docker-compose logs -f postgres
```

## Database Access

### Direct Database Connection

```bash
# Connect to PostgreSQL
docker-compose exec postgres psql -U postgres -d order_management

# List tables
\dt

# View customer orders
SELECT * FROM customer_orders;

# View manufacturing orders
SELECT * FROM manufacturing_orders;
```

## Development

### Local Development Setup

```bash
# Start database only
docker-compose up postgres -d

# Run tests
mvn test

# Run application locally
mvn spring-boot:run -pl application

# Or run specific module tests
mvn test -pl customer-order
mvn test -pl manufacturing-order
```

### Building

```bash
# Clean build
mvn clean package

# Skip tests
mvn clean package -DskipTests

# Build Docker image
docker build -t order-management .
```

## Troubleshooting

### Common Issues

**Application won't start:**
```bash
# Check if database is ready
docker-compose logs postgres

# Check application logs
docker-compose logs app

# Restart services
docker-compose restart
```

**Database connection issues:**
```bash
# Verify database is running
docker-compose ps postgres

# Check database health
docker-compose exec postgres pg_isready -U postgres
```

**Port conflicts:**
```bash
# Check what's using the port
netstat -tulpn | grep :8080

# Use different ports
APP_PORT=8090 docker-compose up
```

### Reset Database

```bash
# Stop services and remove volumes
docker-compose down -v

# Start fresh
docker-compose up -d
```

## Architecture Details

### Module Structure

```
order_management/
├── shared-kernel/          # Common domain concepts
├── customer-order/         # Customer order bounded context
├── manufacturing-order/    # Manufacturing bounded context
├── application/           # Application orchestration
└── docker-compose.yml     # Infrastructure setup
```

### Technology Stack

- **Framework**: Spring Boot 3.5
- **Language**: Java 21
- **Database**: PostgreSQL 17
- **Build**: Maven 3.9
- **Containerization**: Docker
- **Monitoring**: Spring Actuator

### Design Patterns

- **Domain-Driven Design (DDD)**
- **Command Query Responsibility Segregation (CQRS)**
- **Event-Driven Architecture**
- **Hexagonal Architecture (Ports & Adapters)**
- **Repository Pattern**
- **Aggregate Pattern**
