# AwesomePizza – Backend

REST backend for managing pizza orders in the AwesomePizza application.
It exposes APIs to create orders, check their status, and manage the pizza chef’s queue (with states PENDING, IN_PROGRESS, READY, COMPLETED).
## Technologies

- Java 21
- Spring Boot 3.x (Web, Data JPA)
- H2 Database (in-memory, dev)
- Maven
- Springdoc OpenAPI (Swagger UI)
- (Optional) Docker / Docker Compose

## Requirements

- JDK 21 installed

- Maven 3.6+ (mvn -v to verify)

- (Optional) Docker and Docker Compose

## Project Structure

- `com.awesome.awesomepizza`
    - `AwesomePizzaApplication.java`
    - `config/` – configurazione CORS
    - `controller/` – `OrderController`
    - `domain/` – `Order`, `OrderStatus`
    - `dto/` – `OrderRequest`, `OrderResponse`
    - `repository/` – `OrderRepository`
    - `service/` – `OrderService`

## Run Application (without Docker)

From the backend project root:

- `mvn clean spring-boot:run`


The application will be available at:

- API base: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Order Model

Entity `Order` (tabella `pizza_order`):

- `id: UUID`
- `pizzaType: String`
- `status: OrderStatus` (enum: `PENDING`, `IN_PROGRESS`, `READY`, `COMPLETED`)
- `orderCode: String` (es. `ORD-AB12CD34`)
- `createdAt: LocalDateTime`


## Exposed APIs

Base path: `/api/orders`

- **POST** `/api/orders`  
  Creates a new order.
  #### Request body:
        {"pizzaType": "Margherita"}

  #### Response:
        { "orderCode": "ORD-AB12CD34" }


- **GET** `/api/orders/{code}`  
  Returns the order details (including `status`).

- **GET** `/api/orders/queue`  
  Returns all orders for the pizza chef, ordered by `createdAt` (PENDING, IN_PROGRESS, READY, COMPLETED).

- **PUT** `/api/orders/{code}/assign`  
  Set the status to `IN_PROGRESS`.  
  Constraint: **only one order at a time** is allowed in `IN_PROGRESS/READY`.
  If there is already an active order, an `IllegalStateException` is thrown.

- **PUT** `/api/orders/{code}/ready`  
  Set the status to `READY`.  
  Constraint: the order must be `IN_PROGRESS`.

- **PUT** `/api/orders/{code}/complete`  
  Set the status to `COMPLETED`.  
  Constraint: the order must be `READY`.

## Order Flow

1. Customer creates an order → initial state `PENDING`.
2. Pizza chef sees the queue at `/queue`.
3. Pizza Chef:
- `assign` → `IN_PROGRESS`
- `ready` → `READY`
- `complete` → `COMPLETED`
4. The customer can check the status with `GET /api/orders/{code}`.

## Development Profile

- In the development environment, an in-memory H2 database is used (no extra DB configuration required).

- For real databases (PostgreSQL/MySQL), uncomment the driver and properties in `application.yml`.

## Test

Run test:

- `mvn test`

Controller and service tests are included using JUnit 5 and Mockito-based mocking.


---

## Optional: Docker run

The project includes a `Dockerfile` to build a container image for the backend.

### Build backend image

From the backend project root:
- `docker build -t awesomepizza-backend .`

### Run backend container

From the backend project root:
- `docker run -p 8080:8080 awesomepizza-backend`


The backend will be reachable at http://localhost:8080, just like in the traditional run.


##### docker-compose
You can run backend and frontend together using `docker-compose.yml`:
from the root folder that contains both projects, run:` docker-compose up` 






