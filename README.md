# AwesomePizza | Ordini pizza fullstack

Un unico progetto per creare ordini pizza, consultarne lo stato e gestire la
coda del pizzaiolo. Il [backend Spring Boot](backend/README.md) e il
[frontend Angular](frontend/README.md) sono due componenti della stessa
applicazione: l'interfaccia chiama le API REST `/api/orders`.

| Componente | Cartella | Tecnologie |
| --- | --- | --- |
| API e logica ordini | [`backend/`](backend/README.md) | Java 21, Spring Boot 3.5.7, JPA, H2, Swagger |
| Interfaccia cliente e pizzaiolo | [`frontend/`](frontend/README.md) | Angular 21, TypeScript, HttpClient, signals |
| Avvio integrato | [`compose.yaml`](compose.yaml) | Docker Compose, Nginx e proxy API |

## Flusso dell'applicazione

Il cliente crea un ordine e riceve un codice `ORD-...` con cui consultarne lo
stato. Il pizzaiolo carica la coda cronologica e seleziona un ordine:
`PENDING` → `IN_PROGRESS` → `READY` → `COMPLETED`. La logica originale consente
un solo ordine attivo (`IN_PROGRESS` o `READY`) alla volta.

La selezione Customer / Pizza Chef è una scelta dell'interfaccia, non
un'autenticazione. Le API non hanno controllo degli accessi: questa è una demo,
non un sistema da esporre a Internet senza ulteriori protezioni. Il database
H2 predefinito è in memoria: riavviando il backend si perdono gli ordini.

## Avvio locale

Prerequisiti: JDK 21, Node.js 22.12+ (serie 22) e npm. È disponibile il
Maven Wrapper; in alternativa usa Maven 3.9+ installato. Dalla radice del
repository apri due terminali:

```bash
cd backend
./mvnw spring-boot:run
```

Su PowerShell, usa `.\mvnw.cmd spring-boot:run`.

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

Dalla radice esegui:

```bash
docker compose up --build
```

I due Dockerfile compilano i sorgenti durante la costruzione delle immagini.
Il frontend è servito da Nginx su `http://localhost:4200/`; le richieste
`/api/` sono inoltrate al servizio `backend:8080`. Nessun indirizzo interno
Docker viene usato direttamente dal browser.

Per fermare i servizi esegui `docker compose down`. Anche in Docker il
database è in memoria: gli ordini non persistono al riavvio del backend.
Le porte 4200 e 8080 devono essere libere; fermare prima altri progetti che
usano le stesse porte, come l'applicazione degli eventi cittadini.

## Test e compilazione

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

## Organizzazione Git

- **Sorgenti**: Java e TypeScript, test, file di configurazione e
  `package-lock.json` sono versionati.
- **Output esclusi**: `target/`, `dist/`, `node_modules/`, cache, database,
  log e JavaScript compilato non sono versionati.
- **Dati riservati**: `.env` e chiavi private sono esclusi; non inserire
  credenziali nei sorgenti.
- **Cronologia**: i due repository originali sono stati importati come
  subtree senza squash, mantenendo i commit raggiungibili; poi il backend
  è stato spostato dalla sottocartella `AwesomePizza/` a `backend/`.

Il monorepository deriva da
[`marcyborg/awesomepizza-backend`](https://github.com/marcyborg/awesomepizza-backend)
e [`marcyborg/awesomepizza-frontend`](https://github.com/marcyborg/awesomepizza-frontend).
Questi repository non sono stati eliminati, archiviati o modificati e non
vengono sincronizzati automaticamente con il nuovo progetto.

## Copia sul PC

Quando la cartella locale GitHub sarà nuovamente scrivibile:

```powershell
cd C:\Users\Francesco\Documents\GitHub
git clone https://github.com/marcyborg/awesomepizza-fullstack.git
```

Entrambi i componenti sono nello stesso clone. Per lavorare sul nuovo progetto
usa questo repository, anziché modificare separatamente i due originali.
