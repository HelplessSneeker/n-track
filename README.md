# n-track

Tagesplaner: Der Server kennt den Tagesplan, weiß, welcher Punkt gerade dran ist, und rechnet die
Uhrzeiten des restlichen Tages ab jetzt. Spring Boot 4.1.1, Java 21, PostgreSQL.

Jeder Tag entsteht aus einer Vorlage für seinen Wochentag. Wer weitertippt, hakt den aktuellen Punkt
ab; alles dahinter rückt nach. Angular, Login und Deploy kommen später; ab Schritt 1b plant ein
Sprachmodell auf Zuruf um, die Regeln und Uhrzeiten rechnet aber immer der Server.

## Starten

Docker muss laufen — die Datenbank kommt aus `compose.yaml` und wird beim Start mitgestartet.

```sh
./mvnw spring-boot:run
curl localhost:8080/api/today
```

Bauen und testen:

```sh
./mvnw verify
```

Braucht ebenfalls Docker. Der Integrationstest läuft im Schema `test` derselben Datenbank, damit er
den echten Tagesplan im Schema `public` nicht anfasst.

## Die Rechenregeln in drei Sätzen

Aktuell ist der erste nicht erledigte Punkt; er beginnt, als der vorige erledigt wurde, und endet
nach seiner Dauer — aber nie vor jetzt, und ohne Dauer genau jetzt. Die kommenden Punkte reihen sich
daran an, wobei ein Punkt ohne Dauer und ohne feste Uhrzeit offen ist: Er hat einen Start, aber kein
Ende, und die Punkte danach bekommen erst wieder eine Uhrzeit, wenn ein fester Punkt die Kette
verankert. Ein fester Punkt beginnt immer zu seiner Uhrzeit oder später, wenn die Kette davor länger
läuft — dann gibt es eine Warnung mit den Minuten, etwa „Feierabend 20:15: 63 min zu spät".

Dazu drei Feinheiten, die leicht schiefgehen: Feste Uhrzeiten sind Wanduhrzeiten (am Tag der
Zeitumstellung bleibt 20:15 auf 20:15), Dauern dagegen echte Minuten. „24:00" heißt Mitternacht am
Ende des Tages und liegt damit auf dem Folgetag — ein Client darf also nicht nach Uhrzeit sortieren,
sondern nach dem vollen Zeitstempel. Und erledigte Punkte sind Protokoll, nicht Rechnung: Ihr Ende
ist der gemessene Zeitpunkt, eine feste Uhrzeit bleibt bei ihnen außen vor.

## Endpunkte

Zeiten sind ISO-8601 mit Offset, auf die Minute genau. `start` und `end` sind `null`, wo es keine
Uhrzeit gibt.

| | |
|---|---|
| `GET /api/today` | Der heutige Tag. Legt ihn aus der Wochentags-Vorlage an, falls es ihn noch nicht gibt — ein schreibender GET, bewusst so, und beim zweiten Aufruf ohne weitere Wirkung. |
| `POST /api/today/next` | Der aktuelle Punkt ist jetzt erledigt. `409`, wenn der Tag schon komplett erledigt ist. |
| `POST /api/today/back` | Nimmt das letzte Weiter zurück, für Fehltipps. `409`, wenn heute noch kein Punkt erledigt ist. |

Alle drei antworten gleich:

```json
{
  "date": "2026-10-21",
  "now": "2026-10-21T17:30:00+02:00",
  "items": [
    { "title": "Heimweg: Language Transfer 10 min", "start": "2026-10-21T17:30:00+02:00",
      "end": "2026-10-21T18:00:00+02:00", "status": "UPCOMING" },
    { "title": "Aufgaben aus der Daily, wenn noch Zeit ist", "start": "2026-10-21T21:18:00+02:00",
      "end": null, "status": "UPCOMING" }
  ],
  "warnings": [
    { "title": "Feierabend", "fixedAt": "20:15", "minutesLate": 63,
      "message": "Feierabend 20:15: 63 min zu spät" }
  ]
}
```

`status` ist `DONE`, `CURRENT` oder `UPCOMING`. Aktuell ist immer der erste nicht erledigte Punkt.

## Vorlagen

`src/main/resources/vorlagen.yaml`, von Hand gepflegt. Ein Punkt hat `titel` und optional `dauer`
(Minuten) oder `um` (feste Uhrzeit); ohne beides ist er offen. Die Anführungszeichen um die
Uhrzeiten sind tragend — ohne sie liest YAML `20:15` als Zahl.

| Wochentag | Vorlagen |
|---|---|
| Mo, Mi, Fr | `werktag_morgen` + `gym` |
| Di, Do | `werktag_morgen` + `laufen` |
| Sa | `samstag` |
| So | `sonntag` |

Die Zuordnung steht in `Templates.java`.

## Aufbau

```
src/main/java/at/bfnoessler/ntrack/
  NTrackApplication.java      Start
  TimeConfig.java             die Clock-Bean, auf die Minute genau, Europe/Vienna
  schedule/                   die Rechenschicht: rein, ohne Spring, ohne Datenbank
    DaySchedule.java            calculate(punkte, datum, jetzt) -- die ganze Logik
    TimeOfDay.java              feste Uhrzeit als Minuten nach Mitternacht, 1440 = "24:00"
    Item.java                   ein Punkt: titel, dauer?, feste Uhrzeit?, erledigt?
    ScheduledItem.java          ein Punkt mit Start, Ende und Status
    Warning.java                ein fester Punkt, der nicht mehr zu halten ist
    Status.java                 DONE, CURRENT, UPCOMING
  today/                      Web und Datenbank
    TodayController.java        die drei Endpunkte, Transaktionsgrenze je Request
    Templates.java              vorlagen.yaml, beim Start eingelesen
    DayEntity.java  ItemEntity.java  DayRepository.java  ItemRepository.java
    TodayResponse.java  ItemResponse.java  WarningResponse.java
src/main/resources/
  vorlagen.yaml               die Tagesvorlagen
  db/migration/               Flyway; das Schema gehört Flyway, Hibernate prüft nur
```

## Nicht vergessen

- `spring-boot-starter-validation` ist noch ohne Abnehmer: Kein Request hat einen Body. Die
  Abhängigkeit steht für Schritt 1b bereit.
- Zwei gleichzeitige `GET /api/today` könnten beide einen Tag anlegen und auf `day.date`
  kollidieren. Bei einem Nutzer vernachlässigbar, deshalb bewusst keine Sperre.
