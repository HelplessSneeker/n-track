import { Component } from '@angular/core';

/** „Danach": die kommenden Punkte mit ihren Uhrzeiten, soweit der Server welche kennt. */
@Component({
  selector: 'app-upcoming-list',
  imports: [],
  templateUrl: './upcoming-list.html',
  styleUrl: './upcoming-list.scss',
})
export class UpcomingList {
  // ───────────────────────────────────────────────────────────────────────────────────────────────
  // TODO(human) 8 — „Danach"
  //
  // Kontext: Die Liste der `UPCOMING`-Punkte, in genau der Reihenfolge, in der sie ankommt. Im
  //   Template wartet `<ol class="next">`.
  //
  // Aufgabe: Ein `items`-Input und das `@for` darüber, pro Punkt die Uhrzeit und der Titel.
  //
  // Achtung:
  //   - `track item.id` — die `id` ist stabil und eindeutig über alle Tage hinweg.
  //   - Auf einem frischen Tag haben die **meisten** Punkte `start: null`, weil ein offener Punkt
  //     ohne Dauer die Kette abreißt (heute: die Punkte 2 bis 11). Das ist der Normalfall, nicht
  //     ein Ladezustand. Zeig dann nur den Titel — kein „--:--" und kein „unbekannt", das sieht
  //     nach einem Fehler aus, wo keiner ist.
  //   - Nicht umsortieren und nicht filtern: „Im Bett" um `24:00` liegt auf dem Folgetag und stünde
  //     nach Uhrzeit sortiert vorne.
  //   - Formatier die Uhrzeit genauso wie in `current-item` (TODO 7) — wenn du dort die Zeichen
  //     11–16 nimmst, wird hier eine kleine gemeinsame Funktion fällig. Überleg, wo die hingehört.
  // ───────────────────────────────────────────────────────────────────────────────────────────────
}
