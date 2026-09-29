# AwesomePizza | Correzioni prioritarie e test di regressione

Questa modifica affronta i difetti funzionali riprodotti nella revisione
del 30 settembre 2026. Non aggiunge autenticazione, un database persistente,
migrazioni o il proxy mancante nel percorso SSR standalone.

## Backend

- **Assegnazione concorrente**: tutte le transizioni usano una transazione e
  un lock pessimista sulla stessa riga `chef_station`. Il controllo degli
  ordini attivi avviene dopo il lock e la riga resta bloccata fino al commit.
- **Input**: `@Valid`, `@NotBlank`, `@Size(max=255)`, controllo anche nel
  servizio e normalizzazione degli spazi.
- **Errori**: Problem Details con 400 per input/JSON non valido, 404 per
  codice inesistente, 409 per conflitti di stato o accesso concorrente.
- **Codici**: UUID completo con prefisso `ORD-` e vincolo univoco. I codici
  esistenti non vengono modificati; HTTP 200 alla creazione è mantenuto.

Il lock è nel database, non in un monitor Java locale. La verifica
automatizzata usa richieste HTTP su H2 con una sola istanza applicativa:
non costituisce una prova multi-istanza o su PostgreSQL. Prima di introdurre
un database persistente, servono migrazioni e controllo dei dati esistenti.

## Frontend

- **Selezione**: i pulsanti controllano che il codice selezionato coincida
  con l'ordine caricato; modificarlo invalida il dettaglio.
- **Ricerche**: dettagli vecchi cancellati all'avvio/in caso d'errore,
  risposte tardive di ricerche superate ignorate.
- **Azioni**: blocco degli invii duplicati, stato di caricamento e gestione
  dei conflitti senza cancellare il messaggio durante il refresh della coda.
- **Dipendenze**: aggiornamento del lockfile entro gli intervalli esistenti;
  Angular risolto a 21.2.24 e controllo audit nella CI.
- **Host SSR**: allowlist esplicita dei due host locali dopo l'aggiornamento
  Angular, senza disabilitare la validazione degli host o fidarsi di header
  forwarded arbitrari. Questo non aggiunge il proxy API SSR mancante.

## Verifica automatizzata locale

| Verifica | Esito |
| --- | --- |
| `./mvnw --batch-mode verify` | 11 test passati e JAR prodotto |
| Concorrenza HTTP su due ordini distinti | 5 ripetizioni: una risposta 200, una 409, un solo ordine attivo |
| Input nullo, mancante, vuoto e troppo lungo | 400 con Problem Details |
| Codice inesistente su lettura e transizioni | 404 |
| JSON malformato | 400 |
| Flusso completo e transizioni non consentite | Verificati |
| Stato READY mantiene occupata la postazione | Verificato |
| `npm test -- --watch=false` | 13 test passati |
| Ricerca fallita e risposte invertite | Dettaglio obsoleto non mostrato |
| Cambio codice e conflitto con refresh coda | Azioni coerenti e messaggio preservato |
| Invio duplicato e input pizza vuoto | Verificati |
| `npm run build` | Build browser/server e prerender completati |
| Browser desktop/mobile | Flusso fino a COMPLETED, azioni disabilitate dopo cambio codice e nessun dettaglio obsoleto dopo 404 |
| `npm audit --omit=dev` | 0 segnalazioni al momento della verifica |
| `npm audit` completo | 0 segnalazioni al momento della verifica |

Gli esiti dell'audit dipendono dal database degli advisory al momento
dell'esecuzione e non equivalgono a una certificazione di sicurezza.
La CI remota deve confermare questi esiti sul commit della pull request.
Docker non è disponibile nell'ambiente di verifica locale.
