# AwesomePizza | PostgreSQL persistente e migrazioni

Backend e frontend mantengono le stesse API. Cambia il database predefinito:
PostgreSQL sostituisce H2 in memoria, mentre Flyway gestisce lo schema prima
che Hibernate lo validi.

## Avvio completo con Docker Compose

Dalla radice copia `.env.example` in `.env` e assegna a `DB_PASSWORD` una
password privata non vuota. La copia si esegue con `cp .env.example .env`
in Bash oppure `Copy-Item .env.example .env` in PowerShell.

```bash
docker compose up --build
```

Il frontend risponde sulla porta 4200, il backend sulla 8080 e PostgreSQL 16
sulla porta host 5432, pubblicata solo su `127.0.0.1`. Se 5432 è già occupata,
imposta `DB_PORT=5433` in `.env`; il backend nel container continua a usare
`db:5432`. Il backend attende il controllo di disponibilità del database.

Il volume nominato `postgres_data` conserva i dati con `docker compose down`,
`docker compose up` e i riavvii. Non usare `docker compose down -v` né
rimuovere manualmente il volume se vuoi conservarli. Il nome effettivo del
volume è prefissato dal progetto Compose: cambiando cartella o nome progetto
si può selezionare un volume diverso.

Le variabili PostgreSQL inizializzano solo un volume nuovo. Cambiare
`DB_USER` o `DB_PASSWORD` in `.env` non modifica automaticamente un utente
già presente: coordinare eventuali rotazioni con il database, senza eliminare
il volume come scorciatoia.

## Backend locale e profili

Per eseguire Java sul PC con il solo database in Docker:

```bash
docker compose up -d db
```

Configura nel terminale del backend `DB_URL`, `DB_USER` e `DB_PASSWORD`.
L'URL predefinito è `jdbc:postgresql://localhost:5432/awesomepizza`, l'utente
predefinito `awesomepizza`; la password è obbligatoria e non ha fallback.
Se hai cambiato `DB_PORT`, aggiorna anche l'URL del backend locale.

Esempio PowerShell, senza scrivere la password nella cronologia:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/awesomepizza"
$env:DB_USER = "awesomepizza"
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new(
  "", (Read-Host "Password PostgreSQL" -AsSecureString)
).Password
cd backend
.\mvnw.cmd spring-boot:run
```

Il file `.env` è caricato da Compose, non da Spring Boot o Maven. Evita di
incollare credenziali in sorgenti, documentazione, screenshot o log condivisi.

Per una demo senza database esterno:

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

Il profilo `h2` usa Flyway ma è temporaneo: perde i dati quando termina il
processo. Non attivare contemporaneamente i profili `h2` e `postgres`.

## Migrazioni e vincoli

- **V1**: crea `pizza_order`, `chef_station`, indici cronologici e per stato,
  unicità del codice ordine, tipo pizza non nullo/non vuoto e stati ammessi.
- **V2**: inserisce la postazione con ID 1, condivisa dai lock transazionali.
- **Hibernate**: verifica la compatibilità delle entità con lo schema;
  non usa `create`, `update` o `create-drop`.
- **Flyway**: registra versioni e checksum; non esegue baseline automatica
  e non permette `clean`.

Non riscrivere V1 o V2 dopo l'applicazione. Per evoluzioni successive aggiungi
nuove migrazioni versionate e verifica una copia dei dati prima del rilascio.
Un errore di migrazione impedisce l'avvio, invece di ignorare schema o vincoli.

## Database preesistenti

Questa PR inizializza un database vuoto; non copia automaticamente ordini da
H2 o da un PostgreSQL già popolato. Se trova uno schema non vuoto privo della
cronologia Flyway, rifiuta di adottarlo automaticamente.

Prima di un'adozione manuale occorrono un backup verificato, il confronto
dello schema con V1 e il controllo di codici duplicati, tipi pizza nulli o
vuoti, stati non ammessi e più ordini attivi. Va verificata anche la singola
postazione condivisa. La procedura deve essere provata su una copia separata:
non attivare `baseline-on-migrate`, `clean` o la rimozione del volume alla cieca.
Un database incompatibile richiede una migrazione dei dati progettata a parte.

## Backup e ripristino

Esempio dalla radice, con i servizi già configurati. Il dump viene scritto
dentro il container e poi copiato, evitando la redirezione binaria problematica
di alcune versioni di PowerShell:

```bash
mkdir backups
docker compose exec db sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc -f /tmp/awesomepizza.dump'
docker compose cp db:/tmp/awesomepizza.dump ./backups/awesomepizza.dump
docker compose exec db rm /tmp/awesomepizza.dump
```

Il dump comprende anche la cronologia Flyway. Conservalo in un luogo protetto
fuori da Git e prova il ripristino; un volume persistente non protegge da
cancellazioni accidentali o perdita del disco.

Per verificare il backup in un database nuovo, distinto da quello applicativo:

```bash
docker compose cp ./backups/awesomepizza.dump db:/tmp/awesomepizza.dump
docker compose exec db sh -c 'createdb -U "$POSTGRES_USER" awesomepizza_restore_test'
docker compose exec db sh -c 'pg_restore -U "$POSTGRES_USER" -d awesomepizza_restore_test --exit-on-error /tmp/awesomepizza.dump'
docker compose exec db rm /tmp/awesomepizza.dump
```

Il database di verifica deve essere nuovo: se esiste già, fermati e scegli
un nome diverso. Non ripristinare sopra i dati applicativi senza una procedura
di manutenzione approvata; questi comandi non usano `--clean`.

## Test e CI

I test ordinari usano H2 esplicitamente:

```bash
cd backend
./mvnw --batch-mode verify
```

Per le integrazioni serve un database separato chiamato esattamente
`awesomepizza_test`, con un utente dedicato e privilegi per creare tabelle:

```bash
export POSTGRES_IT_URL=jdbc:postgresql://localhost:5432/awesomepizza_test
export POSTGRES_IT_USER=testuser
# Imposta POSTGRES_IT_PASSWORD nel terminale senza versionarla.
./mvnw --batch-mode -Ppostgres-it verify
```

In PowerShell usa `$env:POSTGRES_IT_URL`, `$env:POSTGRES_IT_USER` e
`$env:POSTGRES_IT_PASSWORD`, con `.\mvnw.cmd`. I test rifiutano URL che non
selezionano il nome dedicato; cancellano gli ordini del database di test.
Non usarlo per dati reali e non riutilizzare credenziali di produzione.

La suite comprende 14 test ordinari e 11 test PostgreSQL: API, vincoli,
migrazioni non ripetute, persistenza fra riavvii applicativi e assegnazioni
concorrenti da due istanze con pool separati. La CI usa PostgreSQL 16,
valida Compose e verifica un ordine dopo il riavvio del container database;
controlla inoltre i 13 test Angular e le build.

La prova locale è stata eseguita anche su PostgreSQL 18.6: i test passano,
ma Flyway 11.7.2 segnala che questa versione supera quelle ufficialmente
supportate. Il riferimento per questa configurazione resta PostgreSQL 16
in Compose e nella CI, non PostgreSQL 18.

## Perimetro Git e applicativo

Credenziali `.env`, backup, directory dati PostgreSQL, `target/`, `dist/`
e `node_modules/` sono esclusi da Git. `.env.example` contiene una password
vuota, che deve essere sostituita prima dell'avvio Compose.

Un commit separato normalizza i fine riga di sorgenti preesistenti secondo
`.gitattributes`; non modifica il comportamento del frontend. La PR non
introduce autenticazione, autorizzazioni o modifiche al proxy SSR: questi
restano interventi distinti.
