# AwesomePizza | Backend Spring Boot

Le API di questa cartella sono utilizzate dal
[frontend Angular collegato](../frontend/README.md). Per avviare l'intera
applicazione consulta il [README principale](../README.md).

Java 21 e Spring Boot 3.5.7, con JPA, PostgreSQL, Flyway e Swagger.
PostgreSQL è il profilo predefinito; H2 in memoria è riservato ai test
ordinari e alla demo temporanea.

## Avvio e test

```bash
./mvnw test
./mvnw spring-boot:run
```

Prima dell'avvio configura `DB_URL`, `DB_USER` e `DB_PASSWORD` oppure usa
Docker Compose dalla radice. Il file `.env` viene letto da Compose, non
automaticamente da Maven. Per la demo temporanea senza PostgreSQL usa
`./mvnw spring-boot:run -Dspring-boot.run.profiles=h2`.

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

Il tipo pizza è obbligatorio, non vuoto e lungo al massimo 255 caratteri;
gli spazi iniziali/finali vengono rimossi prima del salvataggio. Input non
valido restituisce 400, ordine inesistente 404 e transizione non consentita
409, con body Problem Details. La creazione mantiene HTTP 200 per compatibilità.

Tutte le transizioni sono transazionali e acquisiscono un lock pessimista
sulla riga condivisa `chef_station`, creata una sola volta dalla migrazione V2; non si bloccano
solo i singoli ordini. I nuovi codici usano un UUID completo dopo `ORD-` (40
caratteri), con vincolo univoco nel database. I vecchi codici restano
consultabili; non vengono rinumerati.

Non sono implementati JWT o autorizzazioni: prima di un utilizzo in produzione
servono controllo degli accessi, gestione operativa e backup verificati.
Non è previsto un import automatico di database preesistenti.

## Schema e migrazioni

Flyway applica `db/migration/V1__create_order_schema.sql` e
`V2__initialize_chef_station.sql` prima dell'inizializzazione JPA. Hibernate usa
`ddl-auto: validate`: verifica lo schema senza modificarlo. Flyway valida i
checksum, non adotta automaticamente schemi non vuoti (`baseline-on-migrate:
false`) e impedisce `clean`.

La V1 crea tabelle, indici e vincoli; la V2 crea la singola postazione condivisa.
Le migrazioni applicate non vanno modificate: aggiungere nuove versioni V3,
V4 e successive. Configurazione, database di test dedicato, backup e adozione
di dati esistenti sono descritti in [PostgreSQL e Flyway](../docs/POSTGRESQL.md).

## Collegamento al frontend

In sviluppo il proxy Angular inoltra `/api/**` alla porta 8080. In Docker
Nginx inoltra `/api/` al servizio `backend`. Il CORS originale consente
`http://localhost:4200`; il proxy rende le chiamate del browser same-origin.

Il Dockerfile usa uno stage Maven con JDK 21 per produrre il JAR e uno stage
JRE per eseguirlo. Docker Compose si trova solo nella radice del
monorepository, non in questa cartella.
