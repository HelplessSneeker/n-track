import { HttpClient, httpResource } from '@angular/common/http';
import { Service, inject } from '@angular/core';

// Für die TODOs unten schon hereingeholt — noch benutzt sie keiner.
import { Item, Plan, Warning } from './plan';

/**
 * Der Datenzugriff auf den Tagesplan: eine Quelle der Wahrheit für die ganze Ansicht.
 *
 * `@Service()` statt `@Injectable({ providedIn: 'root' })` — so schreibt man Singletons ab v22.
 */
@Service()
export class TodayStore {
  private readonly http = inject(HttpClient);

  readonly plan = httpResource<Plan>(() => '/api/today');

  // ───────────────────────────────────────────────────────────────────────────────────────────────
  // TODO(human) 2 — die abgeleiteten Signale
  //
  // Kontext: Das Backend liefert *ein* flaches `items`-Array, unterschieden durch `status`. Die
  //   Ansicht braucht daraus vier Schnitte, und der „Zurück"-Knopf braucht einen fünften.
  //
  // Aufgabe: `computed()`-Felder für `warnings`, `current`, `upcoming`, `done` und `lastDone`.
  //
  // Achtung:
  //   - `current` kann es **nicht geben**: sind alle Punkte erledigt, hat keiner `CURRENT`.
  //     `undefined` ist hier ein gültiger Zustand, kein Fehler.
  //   - `lastDone` ist der *letzte* `DONE`-Punkt in Array-Reihenfolge, nicht der mit dem jüngsten
  //     Zeitstempel. So bestimmt ihn der Server auch (`findFirst…OrderByPositionDesc`), und nur
  //     diesen einen nimmt er zurück — bei jedem anderen antwortet er mit `409`.
  //   - **Nicht umsortieren.** Die Reihenfolge aus dem Array ist die Wahrheit; nach Uhrzeit
  //     sortiert stünde „Im Bett" um 24:00 vorne, weil es auf dem Folgetag liegt. (`items` ist
  //     deshalb `readonly Item[]` — `.sort()` erwischt dich schon beim Kompilieren.)
  //   - Denk an den Fall, dass der Plan noch nicht da ist.
  // ───────────────────────────────────────────────────────────────────────────────────────────────

  // ───────────────────────────────────────────────────────────────────────────────────────────────
  // TODO(human) 3 — abhaken, zurücknehmen, busy, error
  //
  // Kontext: `POST /api/items/{id}/done` und `DELETE /api/items/{id}/done` liefern beide **den
  //   ganzen neuen Plan** als Antwort, nicht nur ein OK.
  //
  // Aufgabe: `markDone(id)` und `undoDone(id)`, dazu ein `busy`-Signal für den gesperrten Knopf und
  //   ein `error`-Signal für die Begründung des Servers.
  //
  // Achtung — hier liegt die interessanteste Entscheidung des Features:
  //   - Was tust du mit der Antwort? `this.plan.set(antwort)` übernimmt sie direkt: eine Runde,
  //     sofort sichtbar. `reload()` holt sie neu: zwei Runden, aber ein Weg weniger im Code.
  //     Beides ist vertretbar — entscheide und begründe es im Kommentar.
  //   - **Auch nach einem Fehler muss der Plan neu geladen werden.** Im Erfolgsfall hast du die
  //     Wahl, hier nicht: ein `409` bringt keinen Plan mit, und er beweist gerade, dass dein Stand
  //     veraltet ist („Dieser Punkt ist noch nicht dran, offen ist: …" heißt, der Server sieht
  //     einen anderen aktuellen Punkt als du). Ein `reload()` *zusätzlich* zum `error`-Signal, nicht
  //     statt ihm — der Nutzer soll die Begründung lesen *und* den richtigen Stand sehen.
  //     Überleg, ob das in den `catch` gehört oder in den `finally`: entscheidest du dich oben für
  //     `reload()`, liegen beide Fälle auf demselben Weg.
  //   - `plan.isLoading()` deckt nur die GET-Anfrage der Resource ab, **nicht** dein POST. Für den
  //     gesperrten Knopf brauchst du deshalb ein eigenes `busy`.
  //   - Setz `busy` im `finally` zurück, sonst bleibt der Knopf nach einem Fehler für immer
  //     gesperrt.
  //   - Der Fehlertext steht in `error.error.detail` der `HttpErrorResponse` (siehe
  //     `ProblemDetail` in `./plan`), nicht in `error.message`. `409` beim Abhaken heißt „Dieser
  //     Punkt ist noch nicht dran, offen ist: …", `409` beim Zurücknehmen „Zurücknehmen lässt sich
  //     nur der letzte erledigte Punkt: …", `404` „Diesen Punkt gibt es nicht: …".
  //   - Das Backend ist idempotent: ein zweiter `POST` auf denselben Punkt ändert nichts und
  //     antwortet mit `200`. Ein Doppeltipp ist also harmlos — du musst ihn nicht abfangen, nur
  //     nicht künstlich zu einem Fehler machen.
  // ───────────────────────────────────────────────────────────────────────────────────────────────
}
