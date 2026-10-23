# n-track

Tagesplaner: Der Server kennt den Tagesplan, weiß, welcher Punkt gerade dran ist, und rechnet die
Uhrzeiten des restlichen Tages ab jetzt. Spring Boot 4.1.1, Java 21, PostgreSQL, Angular 22.

Jeder Tag entsteht aus einer Vorlage für seinen Wochentag. Abgehakt wird ein benannter Punkt, nicht
„der aktuelle"; alles dahinter rückt nach. Login und Deploy kommen später; ab Schritt 1b plant ein
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

## Frontend

Die Jetzt-Ansicht in `frontend/`: Angular 22, standalone, zoneless, als PWA fürs Handy. Backend
starten, dann in einem zweiten Terminal:

```sh
cd frontend
npm start
```

Das ist `ng serve` auf <http://localhost:4200>. Der Dev-Server proxyt `/api` nach `localhost:8080`
(`frontend/proxy.conf.json`) — das Backend hat kein CORS, ohne den Proxy geht es also nicht.

```sh
npx ng build
npx ng test --watch=false
```

Getestet wird mit Vitest, dem Default von Angular 22; `--watch=false` ist nötig, im Terminal läuft
`ng test` sonst im Watch-Mode. Der Service Worker ist im Dev abgeschaltet (`enabled: !isDevMode()`)
und greift nur im Produktionsbuild.

Die Ansicht entsteht als Angular-Übung von Hand. Die offenen Stellen sind mit `TODO(human)`
markiert und in `frontend/AUFGABEN.md` aufgelistet.

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

## Der Tag wechselt um 04:00

`ntrack.day-starts-at=04:00`: Vor dieser Uhrzeit gilt noch der Vortag. Um Mitternacht ist die
Hautpflege ja noch offen, und würde der Tag dort wechseln, entstünde aus der Vorlage ein frisches
Morgen, in dem „Aufstehen" um 00:05 erledigt wäre.

Gerechnet wird der Vortag deshalb mit seinem eigenen Datum weiter — der Plan zeigt um 00:10 also
folgerichtig „Im Bett 24:00: 10 min zu spät", statt auf morgen zu springen.

## Endpunkte

Zeiten sind ISO-8601 mit Offset, auf die Minute genau. `start` und `end` sind `null`, wo es keine
Uhrzeit gibt.

| | |
|---|---|
| `GET /api/today` | Der Tag, der jetzt gilt. Legt ihn aus der Wochentags-Vorlage an, falls es ihn noch nicht gibt — ein schreibender GET, bewusst so, und beim zweiten Aufruf ohne weitere Wirkung. |
| `POST /api/items/{id}/done` | Dieser Punkt ist jetzt erledigt. Ist er es schon, ändert sich nichts (`200`) — ein Doppeltipp darf nicht zwei Punkte abhaken. `409`, wenn vor ihm noch ein Punkt offen ist. |
| `DELETE /api/items/{id}/done` | Nimmt das zurück, für Fehltipps, aber nur beim letzten erledigten Punkt seines Tages; sonst `409`. Ist der Punkt nicht erledigt, ändert sich nichts (`200`). |

Unbekannte `id`: `404`. Fehler kommen als `problem+json`, die Begründung steht in `detail`.

Alle drei antworten gleich. Die beiden Punkt-Endpunkte antworten mit dem Plan **des Tages, zu dem der
Punkt gehört**, nicht mit dem heutigen: Eine Ansicht, die um 00:10 noch auf dem Vortag steht, bekommt
den Vortag zurück.

```json
{
  "date": "2026-10-21",
  "now": "2026-10-21T17:30:00+02:00",
  "items": [
    { "id": 5, "title": "Heimweg: Language Transfer 10 min", "start": "2026-10-21T17:30:00+02:00",
      "end": "2026-10-21T18:00:00+02:00", "status": "UPCOMING" },
    { "id": 10, "title": "Aufgaben aus der Daily, wenn noch Zeit ist",
      "start": "2026-10-21T21:18:00+02:00", "end": null, "status": "UPCOMING" }
  ],
  "warnings": [
    { "title": "Feierabend", "fixedAt": "20:15", "minutesLate": 63,
      "message": "Feierabend 20:15: 63 min zu spät" }
  ]
}
```

`status` ist `DONE`, `CURRENT` oder `UPCOMING`. Aktuell ist immer der erste nicht erledigte Punkt.
Die `id` ist die Adresse zum Abhaken — ein Client sagt damit, welchen Punkt er meint, und ein
wiederholter Versuch bei schlechtem Netz trifft denselben.

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
    DayClock.java               welcher Tagesplan jetzt gilt (ntrack.day-starts-at)
    Templates.java              vorlagen.yaml, beim Start eingelesen
    DayEntity.java  ItemEntity.java  DayRepository.java  ItemRepository.java
    TodayResponse.java  ItemResponse.java  WarningResponse.java
src/main/resources/
  vorlagen.yaml               die Tagesvorlagen
  db/migration/               Flyway; das Schema gehört Flyway, Hibernate prüft nur
frontend/
  AUFGABEN.md                 die offenen TODO(human)-Stellen der Jetzt-Ansicht
  proxy.conf.json             /api nach localhost:8080, weil das Backend kein CORS hat
  src/app/today/
    plan.ts                     der Backend-Vertrag als Typen
    today-store.ts              der Datenzugriff: httpResource, abhaken, zurücknehmen
    today-page.*                der Container: Warnungen, Jetzt, Danach, Erledigt
    warning-list.*  current-item.*  upcoming-list.*  done-list.*
```

## Nicht vergessen

- `spring-boot-starter-validation` ist noch ohne Abnehmer: Kein Request hat einen Body. Die
  Abhängigkeit steht für Schritt 1b bereit.
- Zwei gleichzeitige `GET /api/today` könnten beide einen Tag anlegen und auf `day.date`
  kollidieren. Bei einem Nutzer vernachlässigbar, deshalb bewusst keine Sperre.
