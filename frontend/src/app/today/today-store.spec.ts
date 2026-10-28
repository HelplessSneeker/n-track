// ─────────────────────────────────────────────────────────────────────────────────────────────────
// TODO(human) 10a — die Tests für den Store
//
// Kontext: `TodayStore` ist die einzige Stelle, die mit dem Server spricht. Hier prüfst du also den
//   Vertrag, nicht die Optik: welche Anfrage rausgeht, was aus der Antwort wird, und was bei einem
//   Fehler passiert.
//
// Aufgabe: Das Setup und die Fälle unten. Das Gerüst dieser Datei ist absichtlich leer — auch der
//   `describe`-Block gehört dir.
//
// Setup:
//   - `TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] })`,
//     dann `TestBed.inject(TodayStore)` und `TestBed.inject(HttpTestingController)`.
//   - `provideHttpClientTesting` kommt aus `@angular/common/http/testing`.
//   - `afterEach` mit `httpTesting.verify()`, sonst rutschen dir unerwartete Anfragen durch.
//
// Lohnende Fälle:
//   - `GET /api/today` geht genau einmal raus und `plan.value()` hält danach die Antwort.
//   - `markDone(id)` schickt die **mitgegebene** `id` an `POST /api/items/{id}/done` — nicht die des
//     aktuellen Punktes, die du aus dem Plan lesen könntest. Das ist der ganze Grund für die `id`s.
//   - `busy` ist `true`, solange die Anfrage offen ist, und danach wieder `false`. Prüf beides; nur
//     das Setzen zu prüfen fängt den Fehler nicht, der den Knopf für immer sperrt.
//   - Ein `409` mit `problem+json`: `error` hält hinterher den Text aus `detail`, und der Plan wird
//     **trotzdem** neu geladen (TODO 3). Zum Auslösen `req.flush(body, { status: 409, statusText: 'Conflict' })`.
//   - `undoDone` auf einen Punkt, der nicht der letzte erledigte ist — derselbe Weg, anderer Text.
//   - Ein Plan, in dem **kein** Punkt `CURRENT` ist: `current()` ist `undefined`, und nichts wirft.
//   - `lastDone()` ist der letzte `DONE` in Array-Reihenfolge. Bau dafür einen Plan, in dem die
//     Array-Reihenfolge und die Zeitstempel sich widersprechen — sonst prüft der Test nichts.
//
// Fallen:
//   - Eine `httpResource` lädt **lazy**: solange niemand `plan.value()` liest, fliegt keine Anfrage,
//     und `expectOne` schlägt fehl. Also erst lesen (oder `TestBed.tick()`), dann `expectOne`.
//     (`flushEffects()` ist deprecated.)
//   - Zoneless: `await fixture.whenStable()` bzw. `TestBed.tick()` statt `detectChanges()`.
//   - Spies sind `vi.fn()` — Vitest, nicht Jasmine. `vitest/globals` ist in `tsconfig.spec.json`
//     eingetragen, `describe`/`it`/`expect` brauchst du nicht zu importieren.
//
// Echte Daten: `backend/src/test/java/at/bfnoessler/ntrack/today/TodayApiTest.java`, Methode
//   `countsTheRestOfTheDayFromNowOnceWorkIsTheCurrentItem` — Mittwoch, 2026-10-21 17:30, 13 Punkte,
//   die ersten drei erledigt. Dort geprüft: Index 3 ist `CURRENT` mit `end` = `17:30` (läuft, bis
//   man tippt), Index 9 ist offen (`start` gesetzt, `end` fehlt), Index 10 ist „Feierabend", Index
//   11 beginnt `23:50`, Index 12 liegt mit `2026-10-22T00:00:00+02:00` auf dem Folgetag. Eine
//   Warnung: `minutesLate: 18`, `message: "Feierabend 21:00: 18 min zu spät"`.
//
// Die Fehlertexte, wörtlich aus demselben Test:
//   - 409 abhaken:      "Dieser Punkt ist noch nicht dran, offen ist: Aufstehen: wiegen, LSF"
//   - 409 zurücknehmen: "Zurücknehmen lässt sich nur der letzte erledigte Punkt: Arbeitsweg: Lesen 20 min"
//   - 404:              "Diesen Punkt gibt es nicht: 999999"
//
// Überleg beim Aufbauen der Testdaten, wo sie hingehören: drei Tests brauchen einen Plan. Eine
// gemeinsame Konstante in einer eigenen Datei, oder pro Test ein kleiner Plan mit genau den zwei
// Punkten, um die es geht? Das Zweite liest sich schlechter, zeigt aber im Test selbst, was der
// Fall ist.
//
// Die `it.todo`-Zeilen unten sind nur die Liste, kein Testcode: Vitest meldet sie als offen und
// bleibt grün. Mach aus jeder ein `it(…, async () => { … })`, wenn du sie schreibst.
// ─────────────────────────────────────────────────────────────────────────────────────────────────

describe('TodayStore', () => {
  it.todo('lädt /api/today einmal und hält die Antwort in plan.value()');
  it.todo('schickt bei markDone die mitgegebene id, nicht die des aktuellen Punktes');
  it.todo('setzt busy während der Anfrage und nimmt es danach zurück');
  it.todo('legt bei 409 den Text aus detail in error und lädt den Plan trotzdem neu');
  it.todo('gibt current() als undefined zurück, wenn kein Punkt CURRENT ist');
  it.todo('nimmt als lastDone() den letzten DONE in Array-Reihenfolge');
});
