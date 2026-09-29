# AwesomePizza | Backend Spring Boot

Le API di questa cartella sono utilizzate dal
[frontend Angular collegato](../frontend/README.md). Per avviare l'intera
applicazione consulta il [README principale](../README.md).

Java 21 e Spring Boot 3.5.7, con JPA, H2 in memoria e Swagger. Il database
è temporaneo: gli ordini vengono persi a ogni riavvio del processo.

## Avvio e test

```bash
./mvnw test
./mvnw spring-boot:run
```

Su PowerShell usa `.\mvnw.cmd`; se hai Maven installato, puoi usare `mvn`.
Le API rispondono su `http://localhost:8080/api/orders` e Swagger su
`http://localhost:8080/swagger-ui/index.html`.

## API usate da Angular

| Metodo | Endpoint | Funzione |
| --- | --- | --- |
| POST | `/api/orders` | Crea ordine con body `{"pizzaType":"Margherita"}` e restituisce `{"orderCode":"ORD-..."}` |
| GET | `/api/orders/{code}` | Dettaglio e stato ordine |
| GET | `/api/orders/queue` | Coda ordinata per creazione, inclusi gli ordini completati |
| PUT | `/api/orders/{code}/assign` | Passa da `PENDING` a `IN_PROGRESS` se non ci sono altri ordini attivi |
| PUT | `/api/orders/{code}/ready` | Passa da `IN_PROGRESS` a `READY` |
| PUT | `/api/orders/{code}/complete` | Passa da `READY` a `COMPLETED` |

La logica degli ordini è stata mantenuta dai sorgenti originali. Non sono
implementati JWT o autorizzazioni: prima di un utilizzo in produzione servono
autenticazione, validazioni ed errori strutturati, persistenza e gestione
transazionale della concorrenza per la presa in carico.

## Collegamento al frontend

In sviluppo il proxy Angular inoltra `/api/**` alla porta 8080. In Docker
Nginx inoltra `/api/` al servizio `backend`. Il CORS originale consente
`http://localhost:4200`; il proxy rende le chiamate del browser same-origin.

Il Dockerfile usa uno stage Maven con JDK 21 per produrre il JAR e uno stage
JRE per eseguirlo. Docker Compose si trova solo nella radice del
monorepository, non in questa cartella.
