
# AwesomePizza – Frontend

Angular 17 standalone frontend for the  **AwesomePizza** project.

It provides two views:
-  **Customer**: creates an order and monitors its status using the order code.

-  **Pizza Chef**: manages the order queue (PENDING, IN_PROGRESS, READY, COMPLETED).

## Technologies

- **Angular 17** (standalone components, signals, new @if/@for syntax)
- **TypeScript**
- **Angular HttpClient** to call the Spring Boot backend

## Requirements

- **Node.js 18+**

- **npm 9+**

- **Angular CLI 17** 
 
## Installation and run

From inside `awesome-pizza-ui`:

- npm install

- ng serve
 
Application will be available at:  `http://localhost:4200`

## HTTP Services

-  `POST /api/orders` → Create Order

-  `GET /api/orders/{code}` → Get Order Status

-  `GET /api/orders/queue` → Pizza Chef Queue

-  `PUT /api/orders/{code}/assign` → IN_PROGRESS

-  `PUT /api/orders/{code}/ready` → READY

-  `PUT /api/orders/{code}/complete` → COMPLETED
 
### Customer View

Tabs:
-  **New Order**
-  **Status Order**

### Pizza Chef View
Tab:
-  **Pizza Chef Queue**

## Full E2E Integration  

To run the system end-to-end:  

1. Start the Spring Boot backend: 
    - `mvn clean spring-boot:run`

2. Start Angular frontend: 
    - `cd awesome-pizza-ui`

    - `npm install`

    - `ng serve`

3. Open `http://localhost:4200`:

- Select **Customer** to create an order and monitor its status.

- Select **Pizza Chef** to manage the queue, take one order at a time, and progress it to `COMPLETED`.


## Optional: run with Docker

The project includes a `Dockerfile` to build a container image for the frontend with **Nginx**.

### Run frontend image

From the root of `awesome-pizza-ui`:

`docker build -t awesomepizza-frontend .`

### Run frontend container

`docker run -p 4200:80 awesomepizza-frontend`

The app will be available at`http://localhost:4200` (same as `ng serve`).

*Note*: in this configuration, the frontend still calls the backend at http://localhost:8080 (CORS enabled in the backend). 
If you want to use **Nginx** as a proxy to the backend (e.g. /api proxied to http://backend:8080), you can:

>configure `nginx.conf` with *proxy_pass*,

>set *apiBaseUrl*: '' in `environment.prod.ts` and use relative calls (/api/...),

>use docker-compose` to connect services on the same network.

## Full stack with Docker Compose (optional)

In a folder containing **both** backend and frontend, you can start everything with a single command, as described in the backend README: `docker-compose up`
