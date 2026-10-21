# n-track

Tagesplaner: zeigt, was jetzt dran ist und was danach kommt, und rechnet die Uhrzeiten des Tages ab jetzt.
Spring Boot 4.1, Java 21, PostgreSQL. Angular folgt, ab Schritt 1b plant ein Sprachmodell auf Zuruf um.

## Fallen — hier gilt Spring Boot 4, nicht 3

- **Jackson 3**: Klassen kommen aus `tools.jackson.*` (z. B. `tools.jackson.databind.ObjectMapper`),
  **nur** die Annotationen bleiben in `com.fasterxml.jackson.annotation` (`@JsonProperty` usw.).
  Jackson-3-Exceptions sind unchecked.
- **Web-Starter** heißt `spring-boot-starter-webmvc`, der Test-Starter `spring-boot-starter-webmvc-test`.
- **Flyway** kommt über `spring-boot-starter-flyway`, für PostgreSQL zusätzlich
  `org.flywaydb:flyway-database-postgresql`.
- **Test-Slices liegen pro Modul**, nicht mehr unter `org.springframework.boot.test.autoconfigure.*`:
  `@WebMvcTest` aus `org.springframework.boot.webmvc.test.autoconfigure`.
- **`@MockBean` gibt es nicht mehr**, stattdessen `@MockitoBean` aus
  `org.springframework.test.context.bean.override.mockito`.
- **problem+json** auch für Springs eigene Fehler (400, 404, 415) nur mit
  `spring.mvc.problemdetails.enabled=true`, in Boot 4.1 ist das per Default aus.
- **RestClient** (ab 1b) kommt aus `spring-boot-starter-restclient`. `RestClient.Builder` injizieren,
  nicht selbst bauen. `@RestClientTest` bringt keinen `Validator` mit. Timeouts heißen
  `spring.http.clients.*`, nicht mehr `spring.http.client.*`.

## Zeit

- Alles rechnet in `Europe/Vienna`. Die Zeit kommt aus einer `Clock`-Bean, nie aus `LocalDate.now()`
  oder `LocalDateTime.now()` ohne Clock: Der Server läuft in UTC, und Tests brauchen eine feste Uhr.
- Feste Uhrzeiten sind Wanduhrzeiten: `datum.atTime(um).atZone(zone)`. „24:00“ ist
  `datum.plusDays(1).atStartOfDay(zone)`. Nie Minuten auf den Tagesbeginn addieren, am Tag der
  Zeitumstellung (So 25.10.2026) landet 20:15 sonst auf 19:15.
- Dauern sind echte Minuten: `start.plusMinutes(dauer)` auf `ZonedDateTime`.

## Konventionen

- Wenig Code: Controller, Records, Entities, eine Klasse für die Berechnung. Keine Service-Schicht
  „auf Vorrat“, kein Spring AI.
- Die Zeitberechnung ist eine reine Funktion ohne Spring und ohne Datenbank und hat Unit-Tests.
- Regeln und Uhrzeiten rechnet der Server. Ein Sprachmodell (ab 1b) schlägt nur Änderungen vor.
- Schema nur über Flyway-Migrationen, `spring.jpa.hibernate.ddl-auto=validate`.
- Konfiguration über Properties, Geheimnisse aus `.env` via
  `spring.config.import=optional:file:.env[.properties]`, nie eingecheckt.
- Tests rufen nie ein Modell live auf. Für `./mvnw verify` muss Docker laufen.
