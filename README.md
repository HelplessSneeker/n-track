# n-track

Tagesplaner: Der Server kennt den Tagesplan, weiß, welcher Punkt gerade dran ist, und rechnet die
Uhrzeiten des restlichen Tages ab jetzt. Spring Boot 4.1.1, Java 21, PostgreSQL.

Angular, Login und Deploy kommen später; ab Schritt 1b plant ein Sprachmodell auf Zuruf um. Die
Regeln und alle Uhrzeiten rechnet immer der Server.

## Stand

Dieser Commit enthält nur die Rechenschicht: `at.bfnoessler.ntrack.schedule` ist eine reine Funktion
ohne Spring und ohne Datenbank. Vorlagen, Schema und die Endpunkte folgen im nächsten Commit.

## Bauen

```sh
./mvnw verify
```

Läuft in diesem Stand ohne Docker, weil noch kein Spring-Kontext startet.

## Die Rechenregeln in drei Sätzen

Aktuell ist der erste nicht erledigte Punkt; er beginnt, als der vorige erledigt wurde, und endet
nach seiner Dauer — aber nie vor jetzt, und ohne Dauer genau jetzt. Die kommenden Punkte reihen sich
daran an, wobei ein Punkt ohne Dauer und ohne feste Uhrzeit offen ist: Er hat einen Start, aber kein
Ende, und die Punkte danach bekommen erst wieder eine Uhrzeit, wenn ein fester Punkt die Kette
verankert. Ein fester Punkt beginnt immer zu seiner Uhrzeit oder später, wenn die Kette davor länger
läuft — dann gibt es eine Warnung mit den Minuten, etwa „Feierabend 20:15: 63 min zu spät".

Dazu zwei Feinheiten, die leicht schiefgehen: Feste Uhrzeiten sind Wanduhrzeiten (am Tag der
Zeitumstellung bleibt 20:15 auf 20:15), Dauern dagegen echte Minuten. Und „24:00" heißt Mitternacht
am Ende des Tages, liegt also auf dem Folgetag.
