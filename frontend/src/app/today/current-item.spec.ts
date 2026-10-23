// ─────────────────────────────────────────────────────────────────────────────────────────────────
// TODO(human) 10c — die Tests für „Jetzt"
//
// Kontext: `CurrentItem` ist die eine Kindkomponente, in der genug Entscheidungen stecken, dass ein
//   eigener Test lohnt: die Uhrzeile hat drei Fälle, und der Knopf muss die richtige `id` schicken.
//   Für `warning-list`, `upcoming-list` und `done-list` reicht, was `today-page.spec.ts` mitprüft —
//   es sei denn, du willst die Uhrzeit-Fälle von `upcoming-list` einzeln festhalten, dann bau dir
//   eine vierte Spec-Datei.
//
// Aufgabe: Das Setup und die Fälle unten.
//
// Setup:
//   - `TestBed.createComponent(CurrentItem)`, Inputs über `fixture.componentRef.setInput('item', …)`
//     setzen, dann `await fixture.whenStable()`.
//   - Die Signal-Inputs aus `input()` kannst du nicht direkt zuweisen — `setInput` ist der Weg, und
//     bei `input.required()` wirft ein Lesen vor dem ersten `setInput`. Genau dieses Verhalten ist
//     der Grund, `item` als `required` zu deklarieren (TODO 7).
//   - Text aus dem DOM über `fixture.nativeElement.querySelector('.now__time')?.textContent`.
//
// Lohnende Fälle — die Uhrzeile:
//   - `start` und `end` gesetzt und verschieden: „17:30–18:00" (oder wie auch immer du es
//     formatiert hast — der Test hält deine Entscheidung fest).
//   - `start === end`: ein Punkt ohne Dauer, der läuft, bis man tippt. „17:30–17:30" liest sich
//     schlecht; prüf, was du stattdessen zeigst.
//   - `start` gesetzt, `end` `null`: der offene Punkt.
//   - `start` `null`: die Zeile bleibt leer, es steht kein „--:--" da.
//   - Ein `start` auf dem Folgetag (`"2026-10-22T00:00:00+02:00"`) zeigt `00:00`, nicht `24:00` und
//     nicht irgendetwas Umgerechnetes. Dieser Test ist der eigentliche Grund, ihn zu schreiben: er
//     fällt um, sobald jemand doch anfängt zu rechnen.
//
// Der Knopf:
//   - `(click)` gibt die `id` **dieses** Punktes nach oben, nicht „den aktuellen". Hör den Output
//     mit `vi.fn()` ab: `component.next.subscribe(spy)` — oder, näher am Nutzer, über
//     `querySelector('.now__next').click()`.
//   - `busy` `true` → der Knopf ist `disabled`; `false` → nicht.
//
// Und TODO 7b, falls du das Label hier gebaut hast: `item.start` nach `now` zeigt „ab 23:50",
// davor „Jetzt". Hast du es in `today-page` gebaut, gehört der Fall nach `today-page.spec.ts` —
// nur verlieren darf er sich nicht.
//
// Fallen:
//   - Zoneless: `await fixture.whenStable()` statt `detectChanges()`, auch nach jedem `setInput`.
//   - Spies sind `vi.fn()` — Vitest, nicht Jasmine.
//
// Die `it.todo`-Zeilen unten sind nur die Liste, kein Testcode.
// ─────────────────────────────────────────────────────────────────────────────────────────────────

describe('CurrentItem', () => {
  it.todo('zeigt die Uhrzeit von–bis, wenn start und end verschieden sind');
  it.todo('zeigt bei start === end keine Spanne');
  it.todo('zeigt beim offenen Punkt nur den Start');
  it.todo('lässt die Uhrzeile leer, wenn start null ist');
  it.todo('zeigt einen Start auf dem Folgetag als 00:00');
  it.todo('gibt beim Tippen auf „Weiter" die id dieses Punktes nach oben');
  it.todo('sperrt „Weiter", solange busy gesetzt ist');
});
