# AwesomePizza | Frontend Angular

Questa interfaccia è collegata al
[backend Spring Boot](../backend/README.md) dello stesso monorepository.
Il [README principale](../README.md) descrive l'avvio locale e integrato.

Le dipendenze reali sono Angular 21, TypeScript e Vitest; la precedente
documentazione Angular 17 non corrispondeva ai sorgenti importati.
L'interfaccia offre le viste Customer e Pizza Chef per creare ordini,
consultare lo stato e gestire la coda.

## Avvio, test e build

Usa Node.js 22.12+ (serie 22). Avvia prima il backend sulla porta 8080:

```bash
npm ci
npm start
```

Apri `http://localhost:4200/`. Per verificare il progetto:

```bash
npm test -- --watch=false
npm run build
```

I test usano Vitest. Il lockfile è versionato per rendere riproducibili le
installazioni; `node_modules/`, `dist/` e cache Angular sono esclusi da Git.

## Comunicazione con Spring Boot

`OrderService` chiama `/api/orders` mediante URL relativi. In sviluppo
`proxy.conf.json` inoltra `/api/**` a `http://localhost:8080`; in Docker,
`nginx.conf` inoltra `/api/` al servizio `backend:8080`. Non occorre cambiare
l'URL fra locale e Docker.

Il Dockerfile si trova nella radice `frontend/` e usa `npm ci` e la build
Angular, quindi copia il bundle browser in Nginx. La configurazione originale
SSR/prerender resta disponibile nel progetto Angular; il container Nginx
non esegue il bundle Node SSR.
