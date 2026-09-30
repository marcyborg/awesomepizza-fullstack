# AwesomePizza | Ordini pizza fullstack

Un unico progetto per creare ordini pizza, consultarne lo stato e gestire la
coda del pizzaiolo. Il [backend Spring Boot](backend/README.md) e il
[frontend Angular](frontend/README.md) sono due componenti della stessa
applicazione: l'interfaccia chiama le API REST `/api/orders`.

| Componente | Cartella | Tecnologie |
| --- | --- | --- |
| API e logica ordini | [`backend/`](backend/README.md) | Java 21, Spring Boot 3.5.7, JPA, PostgreSQL, Flyway, Swagger |
| Interfaccia cliente e pizzaiolo | [`frontend/`](frontend/README.md) | Angular 21, TypeScript, HttpClient, signals |
| Avvio integrato | [`compose.yaml`](compose.yaml) | Docker Compose, Nginx e proxy API |

## Flusso dell'applicazione

Il cliente crea un ordine e riceve un codice `ORD-...` con cui consultarne lo
stato. Il pizzaiolo carica la coda cronologica e seleziona un ordine:
`PENDING` → `IN_PROGRESS` → `READY` → `COMPLETED`. Le transizioni acquisiscono
un lock sulla postazione condivisa nel database per mantenere un solo ordine
attivo (`IN_PROGRESS` o `READY`) anche in caso di assegnazioni simultanee.

La selezione Customer / Pizza Chef è una scelta dell'interfaccia, non
un'autenticazione. Le API non hanno controllo degli accessi: questa è una demo,
non un sistema da esporre a Internet senza ulteriori protezioni. Il database
predefinito è PostgreSQL persistente, con schema gestito da Flyway.

## Avvio locale

Prerequisiti: JDK 21, Node.js 22.12+ (serie 22) e npm. È disponibile il
Maven Wrapper; in alternativa usa Maven 3.9+ installato. Dalla radice del
repository prepara PostgreSQL e le variabili `DB_URL`, `DB_USER` e
`DB_PASSWORD`, come descritto nella [guida database](docs/POSTGRESQL.md),
poi apri due terminali:

```bash
cd backend
./mvnw spring-boot:run
```

Su PowerShell, usa `.\mvnw.cmd spring-boot:run`.

Per una demo temporanea senza PostgreSQL usa invece
`./mvnw spring-boot:run -Dspring-boot.run.profiles=h2`: solo questo profilo
perde gli ordini quando il processo termina. I test ordinari scelgono H2
esplicitamente e non richiedono credenziali.

```bash
cd frontend
npm ci
npm start
```

Apri `http://localhost:4200/`. Il proxy Angular inoltra `/api/**` al backend
su `http://localhost:8080`; Swagger è disponibile su
`http://localhost:8080/swagger-ui/index.html`. Non serve installare Angular CLI
globalmente: gli script npm usano la versione del progetto.

## Avvio con Docker

Dalla radice copia `.env.example` in `.env` e imposta una password privata
non vuota in `DB_PASSWORD`; `.env` non deve essere committato. Poi esegui:

```bash
docker compose up --build
```

I due Dockerfile compilano i sorgenti durante la costruzione delle immagini.
Il frontend è servito da Nginx su `http://localhost:4200/`; le richieste
`/api/` sono inoltrate al servizio `backend:8080`. Nessun indirizzo interno
Docker viene usato direttamente dal browser.

PostgreSQL 16 usa il volume nominato `postgres_data`. Per fermare i servizi
esegui `docker compose down`: gli ordini restano disponibili al successivo
avvio. **Non usare `docker compose down -v` se vuoi conservare i dati**:
rimuove anche il volume. La persistenza non sostituisce un backup.

Le porte 4200, 8080 e 5432 devono essere libere; puoi cambiare la porta host
del database con `DB_PORT` in `.env`. Fermare prima altri progetti che
usano le stesse porte, come l'applicazione degli eventi cittadini.

## Test e compilazione

Le correzioni e i test di regressione sono descritti in
[`docs/CORREZIONI-PR.md`](docs/CORREZIONI-PR.md).

```bash
cd backend
./mvnw test
./mvnw package
cd ../frontend
npm ci
npm test -- --watch=false
npm run build
```

Gli esiti e i limiti della verifica locale sono in
[`docs/VERIFICA.md`](docs/VERIFICA.md). I test Angular usano Vitest e non richiedono Chrome. La CI GitHub verifica
separatamente backend e frontend per push e pull request. La build frontend
mantiene la configurazione Angular originale con prerender della pagina e
bundle server; Docker serve la parte browser con Nginx.

La CI comprende inoltre un job PostgreSQL 16 con migrazioni, test API,
persistenza dopo il riavvio dell'applicazione, concorrenza fra due istanze e
verifica dopo il riavvio del container database. Per eseguire questi test
localmente consulta [PostgreSQL e Flyway](docs/POSTGRESQL.md).

## Organizzazione Git

- **Sorgenti**: Java e TypeScript, test, file di configurazione e
  `package-lock.json` sono versionati.
- **Output esclusi**: `target/`, `dist/`, `node_modules/`, cache, database,
  log e JavaScript compilato non sono versionati.
- **Dati riservati**: `.env` e chiavi private sono esclusi; non inserire
  credenziali nei sorgenti.
- **Progetto unico**: backend e frontend sono sviluppati, verificati e
  rilasciati insieme in questo repository.

## Copia sul PC

Quando la cartella locale GitHub sarà nuovamente scrivibile:

```powershell
cd C:\Users\Francesco\Documents\GitHub
git clone https://github.com/marcyborg/awesomepizza-fullstack.git
```

Entrambi i componenti sono nello stesso clone. Usa questo repository per
sviluppare e aggiornare l'intera applicazione.
