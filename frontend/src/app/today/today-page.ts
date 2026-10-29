import { Component, inject } from '@angular/core';

import { CurrentItem } from './current-item/current-item';
import { DoneList } from './done-list/done-list';
import { UpcomingList } from './upcoming-list/upcoming-list';
import { WarningList } from './warning-list/warning-list';
import { TodayStore } from './today-store';

/**
 * Die Jetzt-Ansicht. Der einzige Ort, der den Store kennt — die vier Kindkomponenten bekommen
 * alles über Inputs und melden sich über Outputs zurück.
 */
@Component({
  selector: 'app-today-page',
  imports: [WarningList, CurrentItem, UpcomingList, DoneList],
  templateUrl: './today-page.html',
  styleUrl: './today-page.scss',
})
export class TodayPage {
  // ───────────────────────────────────────────────────────────────────────────────────────────────
  // TODO(human) 4 — zusammenstecken
  //
  // Kontext: Das Gate auf `plan.value()` steht, die Abschnitte stehen in der richtigen Reihenfolge
  //   — Warnungen, Jetzt, Danach, Erledigt — und an jeder Binding-Stelle steht ein Kommentar. Es
  //   fehlt die Verdrahtung der Kinder.
  //
  // Aufgabe: Die Signale des Stores an die Kind-Inputs binden und die Outputs auf
  //   `markDone`/`undoDone` legen.
  //
  // Achtung:
  //   - Das `error`-Signal aus TODO 3 gehört auch hierher: wenn der Server einen Tipp mit `409`
  //     ablehnt, soll der Nutzer die Begründung lesen. Erst mit den Knöpfen aus TODO 7 und 9 kann
  //     es überhaupt gesetzt werden — deshalb hier zurückgestellt, nicht vergessen.
  //

  protected readonly today = inject(TodayStore);

  // ───────────────────────────────────────────────────────────────────────────────────────────────
  // TODO(human) 5 — einmal pro Minute nachladen
  //
  // Kontext: Die Uhrzeiten rechnet nur der Server, und sie verschieben sich mit jeder Minute, die
  //   vergeht. Nach „Weiter" und „Zurück" ist der Plan schon aktuell (TODO 3) — es fehlt der Takt
  //   für die Zeit, die einfach nur vergeht.
  //
  // Aufgabe: Ein Takt, der minütlich ein `reload()` auslöst.
  //
  // Achtung:
  //   - Zoneless heißt: `setInterval` löst von sich aus **keine** Change Detection aus. Das macht
  //     erst das Signal, das `reload()` schreibt. Genau so soll es sein — und genau das ist der
  //     Unterschied zu Angular mit zone.js, wo jeder Timer eine Prüfung der ganzen App anstößt.
  //   - Räum den Timer über `inject(DestroyRef).onDestroy(…)` auf. Sonst läuft er im Test weiter,
  //     und Vitest beendet sich nicht.
  //   - `effect()` ist hier das falsche Werkzeug: du reagierst nicht auf eine Zustandsänderung, du
  //     erzeugst Zeit. (Dass `effect()` dafür oft missbraucht wird, ist eine beliebte Frage.)
  //   - Eine Minute ist grob genug, dass du nichts entprellen musst — aber überleg, ob du den Takt
  //     anhalten willst, wenn die Seite im Hintergrund liegt (`document.visibilityState`). Für den
  //     Abschluss von Schritt 1a ist das nicht nötig.
  // ───────────────────────────────────────────────────────────────────────────────────────────────
}
