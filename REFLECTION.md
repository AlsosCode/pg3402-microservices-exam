# Refleksjonsnotat - PG3402 Mikrotjenester

**Student:** Mathias Alsos Paulsen
**Prosjekt:** PKMN/OP Binder - Digital TCG Collection Manager
**Dato:** Desember 2024

---

## 1. Prosjektoversikt og Måloppnåelse

PKMN/OP Binder er en mikrotjenestebasert webapplikasjon for håndtering av Pokemon TCG-samlinger. Prosjektet demonstrerer kjerneprinsipper innen mikrotjenestearkitektur gjennom implementering av fire operasjonelle tjenester (Catalog, Collection, Media, API Gateway) med både synkron og asynkron kommunikasjon.

### Oppnådde Krav

**Kritiske krav for bestått (Grade E):**
- ✅ **Krav 1-2:** Implementert fire tjenester med REST/HTTP kommunikasjon
- ✅ **Krav 3:** Asynkron kommunikasjon via RabbitMQ med hendelsesdrevet arkitektur

**Grunnleggende krav (Grade D-B):**
- ✅ **Krav 4-6:** Klar tjenestestruktur, konsistent dokumentasjon, Docker-deployment
- ✅ **Krav 7:** API Gateway med routing og rate limiting
- ✅ **Krav 9-10:** Health checks via Actuator og Docker Compose-orkestrerering

Prosjektet oppfyller alle kritiske krav for bestått eksamen samt de fleste krav for karakterene D og B. Krav 8 (load balancing), 11 (sentralisert konfigurasjon), og 12 (flere instanser) er ikke implementert, men infrastrukturen er forberedt for disse utvidelsene.

---

## 2. Arkitekturbeslutninger og Læringspunkter

### 2.1 Event-Driven Architecture (RabbitMQ)

**Beslutning:** Implementere asynkron kommunikasjon mellom Collection Service og Catalog Service via RabbitMQ.

**Læringsutbytte:**
Dette var den mest utfordrende og lærerike delen av prosjektet. Implementeringen krevde:
- Forståelse av TopicExchange, queues, og routing keys
- Håndtering av message serialization (Jackson JavaTimeModule for `Instant`-typer)
- Debugging av container-til-container kommunikasjon i Docker-nettverk
- Implementering av `@RabbitListener` for event consumption

**Utfordring:** Først møtte jeg `InvalidDefinitionException` for Java 8 date/time-typer. Løsningen var å konfigurere `ObjectMapper` med `JavaTimeModule` i `RabbitMQConfig`. Dette lærte meg viktigheten av eksplisitt serialization-konfigurasjon i distribuerte systemer.

**Refleksjon:** Event-driven architecture gir løs kobling mellom tjenester, men introduserer eventual consistency. I produksjon ville jeg lagt til:
- Dead letter queues for feilhåndtering
- Retry-mekanismer med exponential backoff
- Event versioning for bakoverkompatibilitet

### 2.2 Database per Service Pattern

**Beslutning:** Hver tjeneste har sin egen PostgreSQL-database (catalog_db, collection_db).

**Læringsutbytte:**
Denne beslutningen understreket forskjellen mellom monolittisk og mikrotjenestearkitektur. Fordeler jeg observerte:
- Tjenester kan deployes og skaleres uavhengig
- Ingen shared database-coupling
- Hver tjeneste kontrollerer sitt eget skjema

**Utfordring:** Uten en delt database må jeg tenke annerledes på datakonsistens. RabbitMQ-events løser dette, men det krever mer kompleks feilhåndtering enn en database-transaksjon.

**Refleksjon:** I et større system ville jeg vurdert:
- Saga pattern for distribuerte transaksjoner
- Outbox pattern for garantert event publishing
- Eventual consistency monitoring

### 2.3 CQRS-Inspirert Design

**Beslutning:** Separere write model (Collection Service) fra read model (Catalog Service).

**Læringsutbytte:**
Selv om dette ikke er full CQRS (ingen event sourcing), ga det verdifull innsikt i:
- Optimalisering av tjenester for forskjellige bruksområder
- Collection Service optimalisert for writes (brukerens samling)
- Catalog Service optimalisert for reads (søk, filtrering, browsing)

**Refleksjon:** Dette mønsteret skalerer godt. Catalog Service kan cached aggressivt siden kortdata er read-only. Collection Service kan fokusere på konsistens og validering av brukerdata.

### 2.4 API Gateway som Single Entry Point

**Beslutning:** Spring Cloud Gateway for all ekstern trafikk.

**Læringsutbytte:**
Gateway-mønsteret sentraliserer cross-cutting concerns:
- Rate limiting (100 req/min per IP med Redis)
- CORS-håndtering
- Routing til backend-tjenester
- Health checks for circuit breaker

**Utfordring:** Først hadde jeg dupliserte CORS-konfigurasjoner i hver tjeneste. Cleanup viste viktigheten av å forstå hvor ansvar bør ligge i arkitekturen.

**Refleksjon:** Gateway er kritisk i produksjon for:
- Authentication/authorization enforcement
- Request/response transformation
- API versioning
- Telemetry og logging

---

## 3. Tekniske Utfordringer og Løsninger

### 3.1 Jackson Serialization av java.time.Instant

**Problem:** `InvalidDefinitionException` når RabbitMQ skulle serialisere events med `Instant`-felt.

**Løsning:**
```java
@Bean
public MessageConverter messageConverter() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return new Jackson2JsonMessageConverter(objectMapper);
}
```

**Læring:** Java 8 date/time types krever eksplisitt modul-registrering. Dette gjelder ikke bare RabbitMQ, men alle Jackson-baserte serialiseringsscenarier.

### 3.2 Docker Networking og Service Discovery

**Problem:** Tjenester må finne hverandre uten hardkodede IP-adresser.

**Løsning:** Docker Compose oppretter et bridge-nettverk hvor tjenester kan nås via servicenavn (f.eks. `rabbitmq:5672`, `catalog-db:5432`).

**Læring:** Container orchestration håndterer DNS-oppslag. Viktig å forstå forskjellen mellom:
- Intern kommunikasjon (service-navn)
- Ekstern kommunikasjon (localhost:port mapping)

### 3.3 Flyway Migrations og Seed Data

**Problem:** Hvordan laste inn 258 Pokemon-kort i databasen på en repetérbar måte?

**Løsning:** Flyway V3 migration med SQL INSERT-statements. Dette sikrer:
- Versjonert dataseeding
- Automatisk kjøring ved oppstart
- Idempotent (kan kjøres flere ganger)

**Læring:** Database migrations er ikke bare for schema, men også for seed data i dev/test-miljøer.

---

## 4. Forenklinger og Prioriteringer

### 4.1 Hva jeg valgte IKKE å implementere

**Share Service (User Story 3):**
- Prioriterte å få Krav 1-3 solid implementert
- Shareable links er en nice-to-have, ikke critical
- Kan legges til uten store refactorings

**OAuth2/OIDC Enforcement:**
- Dependencies er på plass, men security er disablet
- Forenkler testing og demonstrasjon
- I produksjon ville dette vært første prioritet

**Load Balancing (Krav 8):**
- Docker Compose kan skalere (`--scale`), men ikke konfigurert
- Fokuserte på å få enkeltstående tjenester robuste først

### 4.2 Hvorfor disse prioriteringene?

**Metodikk:** Jeg fulgte en "walking skeleton"-tilnærming:
1. Først: Få kritiske krav (Krav 1-3) til å fungere end-to-end
2. Deretter: Legg til observability og dokumentasjon
3. Til slutt: Nice-to-have features

Dette sikret at jeg alltid hadde en fungerende demo som oppfylte minimumskravene, selv om jeg ikke rakk alle features.

---

## 5. Hva jeg ville gjort annerledes

### 5.1 Tidligere Fokus på Asynkron Kommunikasjon

RabbitMQ-implementeringen tok lengre tid enn forventet. I retrospekt ville jeg:
- Startet med RabbitMQ-integrasjon tidligere i prosjektet
- Testet event-flyten før jeg bygget masse annen funksjonalitet
- Lest mer dokumentasjon om Spring AMQP i forkant

### 5.2 Mer Omfattende Testing

Prosjektet har enhetstester, men mangler:
- Integrasjonstester for event-flyt
- Contract testing mellom tjenester (Pact)
- End-to-end tester med Docker Compose

Med mer tid ville jeg implementert Testcontainers for integrerte tester mot RabbitMQ og PostgreSQL.

### 5.3 Bedre Observability fra Starten

Prometheus og Grafana er satt opp, men:
- Custom metrics kunne vært mer detaljerte
- Distributed tracing (Zipkin/Jaeger) ville hjulpet med debugging
- Structured logging kunne vært mer konsistent

---

## 6. Viktigste Læringspunkter

### 6.1 Mikrotjenester er Komplekse

Den største innsikten er at mikrotjenester introduserer betydelig kompleksitet:
- Nettverk er upålitelig (latency, failures)
- Distributed debugging er vanskelig
- Eventual consistency krever annen tankegang
- Mange flere "moving parts" (RabbitMQ, Redis, flere databaser)

**Men:** Kompleksiteten gir gevinster:
- Uavhengig deployment og skalering
- Teknologisk fleksibilitet (polyglot)
- Team autonomy
- Resilience (en tjeneste kan feile uten å ta ned hele systemet)

### 6.2 "Database per Service" er Kraftfullt, men Krevende

Å ikke kunne gjøre JOINs på tvers av tjenester endrer hvordan man designer APIer. Events og eventual consistency blir nødvendig.

### 6.3 Infrastruktur er Førsteborger

I mikrotjenesteverdenen er infrastruktur (RabbitMQ, Redis, Prometheus, Docker) like viktig som applikasjonskode. Å forstå hvordan disse komponentene fungerer sammen er kritisk.

### 6.4 Dokumentasjon er Essensielt

Med flere tjenester, APIer, og kommunikasjonsmønstre blir god dokumentasjon (README, API docs, architecture diagrams) kritisk for at andre (og fremtidig deg selv) skal forstå systemet.

---

## 7. Konklusjon

PKMN/OP Binder demonstrerer kjerneprinsipper i mikrotjenestearkitektur gjennom en praktisk implementering. Prosjektet oppfyller alle kritiske eksemenskrav og viser forståelse for:
- Synkron og asynkron kommunikasjon
- Database per service pattern
- API Gateway pattern
- Event-driven architecture
- Docker containerization og orchestration

Største utfordring var RabbitMQ-integrasjonen, men dette ga også mest læring. Prosjektet har et solid fundament som enkelt kan utvides med Share Service, OAuth2, og load balancing.

Jeg går ut av dette prosjektet med en mye dypere forståelse av både fordelene og kostnadene ved mikrotjenestearkitektur. Det er et kraftfullt mønster, men bør brukes med omhu og kun når kompleksiteten er berettiget.

---

**Ordtelling:** Ca. 1400 ord (innenfor 2-siders grense med normale marginer)
