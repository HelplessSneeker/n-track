import { Component } from '@angular/core';

import { CurrentItem } from './current-item';
import { DoneList } from './done-list';
import { UpcomingList } from './upcoming-list';
import { WarningList } from './warning-list';

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
  // Kontext: Die vier Kindkomponenten stehen, das Template hat die Abschnitte in der richtigen
  //   Reihenfolge — Warnungen, Jetzt, Danach, Erledigt — und an jeder Binding-Stelle einen
  //   Kommentar. Es fehlt die Verdrahtung.
  //
  // Aufgabe: Den Store injizieren, seine Signale an die Kind-Inputs binden, die Outputs auf
  //   `markDone`/`undoDone` legen, und oben das Datum des Tages anzeigen.
  //
  // Achtung:
  //   - Für „Jetzt" brauchst du ein `@if` mit einem `@else`-Zweig („Alles erledigt") — es *kann*
  //     keinen aktuellen Punkt geben. Denk an `@if (store.current(); as item)`, dann musst du im
  //     Zweig kein `!` schreiben.
  //   - Den Tagestitel aus `plan().date` nehmen, **nie** aus `now`: `ntrack.day-starts-at` ist
  //     `04:00`, zwischen 00:00 und 03:59 gilt noch der Vortag. Genau das soll die Ansicht um
  //     00:10 auch zeigen.
  //   - Lade- und Fehlerzustand: beim allerersten Aufruf ist noch kein Plan da, und `GET
  //     /api/today` kann scheitern (Backend aus, Docker aus). Überleg, was die Ansicht dann zeigt —
  //     ein leerer Bildschirm ist die schlechteste Antwort.
  //   - Das `error`-Signal aus TODO 3 gehört auch hierher: wenn der Server einen Tipp mit `409`
  //     ablehnt, soll der Nutzer die Begründung lesen.
  //
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
