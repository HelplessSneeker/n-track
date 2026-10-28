# n-track

Tagesplaner: zeigt, was jetzt dran ist und was danach kommt, und rechnet die Uhrzeiten des Tages ab
jetzt. Ab Schritt 1b plant ein Sprachmodell auf Zuruf um.

## Aufbau

- `backend/` — Spring Boot 4.1, Java 21, PostgreSQL. Dort liegen `pom.xml`, `mvnw` und
  `compose.yaml`; gebaut wird mit `cd backend && ./mvnw verify`, dafür muss Docker laufen. Die
  Fallen von Spring Boot 4, die Zeitrechnung und die Konventionen stehen in `backend/CLAUDE.md`.
- `frontend/` — die Jetzt-Ansicht in Angular 22, als PWA. Gebaut wird mit
  `cd frontend && npx ng build`, getestet mit `npx ng test --watch=false`. Siehe
  `frontend/CLAUDE.md` und `frontend/AUFGABEN.md`.

## Für beide

- Regeln und Uhrzeiten rechnet der Server. Ein Sprachmodell (ab 1b) schlägt nur Änderungen vor; das
  Frontend zeigt an, was zurückkommt, und rechnet nichts nach.
- Die Jetzt-Ansicht schreibt Benjamin als Übung selbst. An einer `TODO(human)`-Stelle gilt der
  Abschnitt „So hilfst du mir bei den TODO(human)" ganz oben in `frontend/CLAUDE.md` — lies ihn,
  bevor du dort etwas tust.
