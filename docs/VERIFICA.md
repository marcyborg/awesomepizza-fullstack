# AwesomePizza | Verifica del monorepository

Verifica locale eseguita il 30 settembre 2026, fuso Europe/Rome. I controlli
coprono l'importazione dei repository, la compilazione e il collegamento fra
interfaccia Angular e API Spring Boot; non costituiscono un audit completo
per l'impiego in produzione.

## Test e build

| Controllo | Esito |
| --- | --- |
| Backend, `./mvnw --batch-mode verify` con JDK 21 | 2 test superati, JAR Spring Boot prodotto |
| Frontend, `npm ci` | Installazione completata dal lockfile |
| Frontend, `npm test -- --watch=false` | 8 test superati: 2 del componente, 6 del contratto HTTP |
| Frontend, `npm run build` | Bundle browser/server e pagina prerender generati |
| Browser, creazione ordine e consultazione | Codice ordine visibile e stato `PENDING` |
| Browser, presa in carico e completamento | `PENDING` → `IN_PROGRESS` → `READY` → `COMPLETED` |
| Browser, codice vuoto | Messaggio `Please enter an order code` |
| Browser, codice inesistente | Messaggio `Order not found` |
| Browser, desktop 1280×900 e mobile 375×812 | Schermate controllate; nessuna eccezione JavaScript osservata, nessun overflow orizzontale mobile |

Le porte 8080 e 4200 erano occupate da un altro progetto nel sandbox:
per questa prova sono state usate 8082 e 4202, con un proxy esterno al
repository che conserva l'origine locale 4200 consentita dal CORS originale.
Le istruzioni e il proxy versionati usano invece 8080 e 4200.

## Git e limiti della verifica

I repository originali sono importati mediante `git subtree add` senza
squash; i loro commit restano raggiungibili. Il backend è normalizzato sotto
`backend/`, il frontend sotto `frontend/`; gli originali remoti restano intatti.

Sono esclusi build Java/JavaScript, dipendenze, cache, dati runtime e segreti.
Docker non è disponibile nell'ambiente di verifica: i Dockerfile e il
Compose sono stati corretti, ma le immagini e l'avvio dei container non sono
stati eseguiti. L'esito della CI remota va verificato nella scheda Actions.

Questa verifica descrive lo stato precedente all'introduzione di PostgreSQL
e Flyway. Per la nuova configurazione persistente e i relativi test consulta
[PostgreSQL e Flyway](POSTGRESQL.md).

Restavano i limiti dell'applicazione originale: H2 in memoria, assenza di
autenticazione/autorizzazione, validazioni ed errori strutturati da completare
e controllo di concorrenza da rafforzare nella presa in carico degli ordini.
