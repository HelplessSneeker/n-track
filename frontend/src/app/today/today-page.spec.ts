// ─────────────────────────────────────────────────────────────────────────────────────────────────
// TODO(human) 10b — die Tests für den Container
//
// Kontext: `TodayPage` rechnet nichts. Sie steckt den Store an die vier Kindkomponenten und
//   entscheidet, was bei leerem, ladendem und kaputtem Zustand dasteht. Genau das prüfst du hier —
//   nicht nochmal den Store.
//
// Aufgabe: Das Setup und die Fälle unten.
//
// Setup — hier liegt die Entscheidung dieses Tests:
//   - Variante A: echter Store plus `provideHttpClient()` + `provideHttpClientTesting()`, Antworten
//     über den `HttpTestingController` einspielen. Nah an der Wirklichkeit, aber du testest den
//     Store gleich mit — und bei einem Fehler weißt du nicht, wer schuld ist.
//   - Variante B: ein Fake-Store über `providers: [{ provide: TodayStore, useValue: … }]`, dessen
//     Signale du im Test setzt. Dann prüft der Test nur den Container. Dafür muss der Fake die
//     Felder haben, die das Template liest — was dir zeigt, wie breit die Schnittstelle wirklich
//     ist. Wird sie unangenehm breit, ist das ein Befund über den Store, kein Testproblem.
//   Entscheide und begründe es. Im Gespräch ist genau das die Frage hinter „wie testest du
//   Komponenten".
//
// Lohnende Fälle:
//   - Das Datum kommt aus `plan().date`, **nicht** aus `now`. Der Fall, der das beweist: `date`
//     `"2026-10-21"` zusammen mit `now` `"2026-10-22T00:10:00+02:00"` — der Tag wechselt um 04:00,
//     um 00:10 gilt noch der Vortag. Ein Test mit `date` und `now` am selben Tag prüft das nicht.
//   - Erster Aufruf, noch kein Plan: die Ansicht zeigt etwas. Ein leerer Bildschirm wäre die
//     schlechteste Antwort, also halt das hier fest, was auch immer du gewählt hast.
//   - `GET /api/today` scheitert (Backend aus): die Ansicht sagt das, statt leer zu bleiben.
//   - Kein Punkt mit `CURRENT`: der `@else`-Zweig „Alles erledigt" steht da, und `app-current-item`
//     ist **nicht** im DOM.
//   - „Weiter" aus `app-current-item` landet mit der richtigen `id` bei `markDone`, „Zurück" aus
//     `app-done-list` bei `undoDone`. Spies sind `vi.fn()`.
//   - Ein abgelehnter Tipp: `error` ist gesetzt, der Text steht in der Ansicht.
//
// Fallen:
//   - Zoneless: `await fixture.whenStable()` statt `detectChanges()`. `src/app/app.spec.ts` zeigt
//     den Rahmen.
//   - Der Minutentakt aus TODO 5 läuft im Test weiter, wenn du ihn nicht aufräumst — dann beendet
//     sich Vitest nicht. Hast du `inject(DestroyRef).onDestroy(…)` gesetzt, reicht
//     `fixture.destroy()`. Ein Test, der das festhält, ist mehr wert als die Erinnerung daran:
//     prüf mit `vi.useFakeTimers()`, dass nach `destroy()` kein `reload()` mehr kommt.
//   - Willst du den Takt feuern sehen, `vi.useFakeTimers()` + `vi.advanceTimersByTime(60_000)` —
//     und in `afterEach` `vi.useRealTimers()`, sonst färbt es die anderen Tests.
//
// Echte Daten: siehe `today-store.spec.ts`.
//
// Die `it.todo`-Zeilen unten sind nur die Liste, kein Testcode.
// ─────────────────────────────────────────────────────────────────────────────────────────────────

describe('TodayPage', () => {
  it.todo('zeigt den Tag aus plan().date, auch wenn now schon auf dem Folgetag liegt');
  it.todo('zeigt beim ersten Aufruf einen Ladezustand statt eines leeren Bildschirms');
  it.todo('sagt es, wenn GET /api/today scheitert');
  it.todo('zeigt „Alles erledigt", wenn kein Punkt CURRENT ist');
  it.todo('gibt die id von „Weiter" an markDone und die von „Zurück" an undoDone weiter');
  it.todo('zeigt die Begründung des Servers, wenn ein Tipp abgelehnt wird');
  it.todo('stoppt den Minutentakt, wenn die Komponente zerstört wird');
});
