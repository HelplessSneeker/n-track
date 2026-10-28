# Die Jetzt-Ansicht: offene Stellen

Diese Ansicht entsteht als Übung von Hand. Das Gerüst steht — Projekt, Proxy, PWA, Typen, Layout,
die Komponenten mit ihren Metadaten und der statischen Template-Struktur, dazu die drei
Spec-Dateien. `npx ng build` und `npx ng test --watch=false` laufen grün. Was fehlt, ist jedes Mal
mit `TODO(human)` markiert; **das volle Briefing steht als Kommentar direkt an der Stelle**, hier
nur die Reihenfolge und der Überblick.

Für Claude gilt: an einer `TODO(human)`-Stelle **keinen Code schreiben**, auch nicht auf Nachfrage.
Wenn ich hänge, will ich einen Hinweis auf die nächste Überlegung, nicht die Lösung.

## Reihenfolge

| # | Datei | Was fehlt |
|---|---|---|
| 1 | `src/app/today/today-store.ts` | der Plan als `httpResource` auf `/api/today` |
| 2 | `src/app/today/today-store.ts` | die abgeleiteten Signale: `warnings`, `current`, `upcoming`, `done`, `lastDone` |
| 3 | `src/app/today/today-store.ts` | `markDone`, `undoDone`, `busy`, `error` — und nach einem `409` neu laden |
| 4 | `src/app/today/today-page.ts` + `.html` | Store injizieren, Inputs und Outputs verdrahten |
| 5 | `src/app/today/today-page.ts` | einmal pro Minute nachladen |
| 6 | `src/app/today/warning-list/warning-list.ts` + `.html` | `warnings`-Input, `@for` |
| 7 | `src/app/today/current-item/current-item.ts` + `.html` | `item`- und `busy`-Input, `next`-Output, Uhrzeit von–bis |
| 7b | `src/app/today/current-item/current-item.ts` **oder** `today-page.*` | „ab 23:50" statt „Jetzt", wenn der Punkt erst später beginnt |
| 8 | `src/app/today/upcoming-list/upcoming-list.ts` + `.html` | `items`-Input, `@for`, Uhrzeit nur wo vorhanden |
| 9 | `src/app/today/done-list/done-list.ts` + `.html` | `items`- und `busy`-Input, `undo`-Output, eingeklappt |
| 10a | `src/app/today/today-store.spec.ts` | die Tests zum Vertrag — siehe unten |
| 10b | `src/app/today/today-page.spec.ts` | die Tests zum Container |
| 10c | `src/app/today/current-item/current-item.spec.ts` | die Tests zur Uhrzeile und zum Knopf |

1 bis 3 zuerst: ohne den Store hat der Container nichts zu binden. Danach 4, dann die
Kindkomponenten 6 bis 9 in beliebiger Reihenfolge, dann 5 und 7b, dann die Tests.

**7b ist die Stelle mit der Entscheidung darin:** Der Vergleich braucht `plan.now`, das hat nur
`today-page`; den Punkt hat `current-item`. Ob die Überschrift oben bleibt oder `now` nach unten
geht, legt fest, welche Komponente die Uhr kennen darf.

**Die Spec-Dateien stehen schon da**, mit dem Briefing und einer `it.todo`-Liste statt Tests. Vitest
meldet sie als offen und bleibt grün; mach aus jeder Zeile ein `it(…)`, wenn du sie schreibst. (Eine
Spec-Datei ganz ohne Suite wäre rot: „No test suite found in file".)

## Der Backend-Vertrag in Kurzform

Die Typen stehen vollständig und kommentiert in `src/app/today/plan.ts` — das ist die
verbindliche Fassung. Die vier Punkte, die die Ansicht am meisten prägen:

1. **Alle drei Endpunkte antworten mit demselben `Plan`.** `POST /api/items/{id}/done` und
   `DELETE /api/items/{id}/done` liefern den **kompletten neuen Plan** zurück, nicht nur ein OK —
   nachladen ist danach optional.
2. **Ein flaches `items`-Array**, nach `position` sortiert, unterschieden durch
   `status: "DONE" | "CURRENT" | "UPCOMING"`. Erledigte Punkte sind das führende Stück. Sind alle
   erledigt, hat **kein** Punkt `CURRENT`. Nicht umsortieren.
3. **`start` und `end` sind oft `null`** — auf einem frischen Tag bei den meisten Punkten. Ein
   offener Punkt ohne Dauer reißt die Kette ab; Uhrzeiten kommen erst beim nächsten festen Punkt
   wieder. Normalfall, kein Ladezustand.
4. **`warnings[].message`** ist fertiger deutscher Text. Direkt anzeigen.

Fehler sind `problem+json`, die Begründung steht in **`detail`**:

| | |
|---|---|
| `409` beim Abhaken | „Dieser Punkt ist noch nicht dran, offen ist: …" |
| `409` beim Zurücknehmen | „Zurücknehmen lässt sich nur der letzte erledigte Punkt: …" |
| `404` | „Diesen Punkt gibt es nicht: …" |

Ein `409` bringt **keinen** Plan mit. Nachladen ist nach einem Erfolg optional (Punkt 1), nach einem
`409` nicht: Der Fehler beweist gerade, dass der angezeigte Stand veraltet ist.

Das Backend ist idempotent: ein zweiter `POST` auf denselben Punkt ändert nichts und antwortet mit
`200`. Ein Doppeltipp hakt also nie zwei Punkte ab. Ein schnelles „Weiter, Zurück" dagegen schon:
die zweite Anfrage geht noch vom alten Stand aus und bekommt `409`. Deshalb sperrt `busy` beide
Knöpfe, nicht nur den, der gerade gedrückt wurde.

Der Tag wechselt um **04:00**, nicht um Mitternacht: zwischen 00:00 und 03:59 gilt noch der Vortag.
Deshalb den angezeigten Tag aus `plan.date` nehmen, nie aus `plan.now`.

## TODO 10 — die Tests

Das Briefing pro Datei steht in der Datei; hier nur, was für alle drei gilt.

- **Zoneless im Test** heißt `await fixture.whenStable()` statt `detectChanges()`. Die generierte
  `app.spec.ts` zeigt es — sie hat die HTTP-Provider schon, weil `App` über `TodayPage` den Store
  injiziert.
- **Für den Store** `provideHttpClient()` + `provideHttpClientTesting()` und
  `HttpTestingController`.
- Eine **`httpResource` lädt lazy**: solange niemand `value()` liest, fliegt keine Anfrage. Also
  `TestBed.tick()` oder `whenStable()` dazwischen, bevor du `expectOne` erwartest.
  (`flushEffects()` ist deprecated.)
- Spies sind **`vi.fn()`** — Vitest, nicht Jasmine.
- Lohnende Fälle: Punkte ohne Uhrzeit, kein `CURRENT` mehr, `409` mit `detail` **und** dem Nachladen
  danach, beide gesperrten Knöpfe während der Anfrage, dass „Weiter" die **mitgegebene** `id`
  schickt, der Tag aus `date` bei einem `now` auf dem Folgetag, und ein `start` um `24:00`.
- `vi.useFakeTimers()` für den Minutentakt, in `afterEach` `vi.useRealTimers()`. Räumt TODO 5 den
  Timer nicht über `DestroyRef` auf, beendet sich Vitest nicht.
- Als Vorlage für echte Daten taugt `TodayApiTest.java` im Backend — die Mittwoch-Zahlen dort
  (13 Punkte, `"Feierabend 21:00: 18 min zu spät"`, `minutesLate: 18`) sind geprüft.

## Nur aktuelle APIs: wo man leicht eine alte schreibt

Nachgeprüft an `@angular/core@22.2.3` und `@angular/common@22.2.3`. Die offizielle Idiomliste steht
in `CLAUDE.md` daneben.

| alt | aktuell in v22 |
|---|---|
| `standalone: true` im Decorator | Default, weglassen |
| `changeDetection: ChangeDetectionStrategy.OnPush` | Default, weglassen |
| `provideZonelessChangeDetection()` | ab v21 Default, weglassen |
| `provideHttpClient(withFetch())` | `provideHttpClient()` — `withFetch` ist deprecated, `FetchBackend` ist Default |
| `@Injectable({ providedIn: 'root' })` | `@Service()` |
| `@Input()` / `@Output()` | `input()` / `output()`; bei `[(x)]` `model()` |
| `*ngIf` / `*ngFor` | `@if` / `@for` mit `track` |
| `ngClass` / `ngStyle` | `[class.x]` / `[style.x]` |
| `constructor(private http: HttpClient)` | `inject(HttpClient)` |
| `CommonModule` importieren | nur die gebrauchte Pipe, z. B. `DatePipe` |
| `@HostListener` / `@HostBinding` | `host: { … }` im Decorator |
| `flushEffects()` im Test | `TestBed.tick()` |

`strict` und `strictTemplates` stehen nicht in den tsconfigs, sind aber beide **an** — TypeScript 6
hat die Defaults gedreht, deshalb schreibt `ng new` sie nicht mehr hin. Ein falscher Input-Typ im
Template fällt also beim Bauen auf.
