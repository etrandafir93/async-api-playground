# Async API Playground

A microservices project demonstrating event-driven architecture with Spring Boot 4, Java 25, and Kafka.

## Services

### Order Service (Port 8081)
- Listens to `stock-unavailable` topic
- Action: Prints "Canceling the order" when stock is unavailable

### Inventory Service (Port 8082)
- Listens to `order-created` topic
- Action: Prints "Decreasing stock for product x" when an order is created

## Technology Stack

- Spring Boot 4.0.0
- Java 25
- Spring Cloud Stream with Kafka Binder
- Apache Kafka

## Getting Started

### Prerequisites
- Java 25
- Maven
- Docker and Docker Compose (for Kafka)

### Running Kafka

Start Kafka and Zookeeper using Docker Compose:

```bash
docker-compose up -d
```

### Running the Services

**Order Service:**
```bash
cd order-service
mvn spring-boot:run
```

**Inventory Service:**
```bash
cd inventory-service
mvn spring-boot:run
```

## Testing the Setup

You can test the services by publishing messages to Kafka topics using kafka-console-producer or any Kafka client.

**Test order-created topic (received by inventory-service):**
```bash
docker exec -it <kafka-container-id> kafka-console-producer --broker-list localhost:9092 --topic order-created
# Then type: {"orderId": "123", "productId": "product-x"}
```

**Test stock-unavailable topic (received by order-service):**
```bash
docker exec -it <kafka-container-id> kafka-console-producer --broker-list localhost:9092 --topic stock-unavailable
# Then type: {"orderId": "123", "reason": "Out of stock"}
```

## Project Structure

```
async-api-playground/
├── order-service/
│   ├── src/main/java/com/example/orderservice/
│   │   ├── OrderServiceApplication.java
│   │   └── listener/StockUnavailableListener.java
│   ├── src/main/resources/application.yml
│   └── pom.xml
├── inventory-service/
│   ├── src/main/java/com/example/inventoryservice/
│   │   ├── InventoryServiceApplication.java
│   │   └── listener/OrderCreatedListener.java
│   ├── src/main/resources/application.yml
│   └── pom.xml
└── docker-compose.yml
```