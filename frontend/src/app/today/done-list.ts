import { Component } from '@angular/core';

/** „Erledigt": Protokoll, eingeklappt. Dazu das kleine „Zurück" für Fehltipps. */
@Component({
  selector: 'app-done-list',
  imports: [],
  templateUrl: './done-list.html',
  styleUrl: './done-list.scss',
})
export class DoneList {
  // ───────────────────────────────────────────────────────────────────────────────────────────────
  // TODO(human) 9 — „Erledigt", eingeklappt, mit „Zurück"
  //
  // Kontext: Die erledigten Punkte sind Protokoll — sie sollen da sein, aber nicht im Weg stehen.
  //   Im Template warten `<details>` mit `<summary>Erledigt</summary>` und eine leere `<ol>`.
  //
  // Aufgabe: Die Inputs `items` und `busy`, der Output `undo`, das `@for` über die Liste und der „Zurück"-Knopf.
  //
  // Achtung:
  //   - `busy` sperrt „Zurück" genauso wie „Weiter" (TODO 7), und hier ist das **keine Optik,
  //     sondern Korrektheit**: „Zurück" nimmt den letzten erledigten Punkt zurück, und welcher das
  //     ist, ändert gerade die laufende Anfrage. Tippst du schnell „Weiter", dann „Zurück", geht
  //     die zweite Anfrage noch vom alten Stand aus und bekommt `409` („Zurücknehmen lässt sich nur
  //     der letzte erledigte Punkt: …") — ein Fehler für einen Tipp, der richtig gemeint war.
  //   - `<details>`/`<summary>` spart dir ein eigenes `open`-Signal und ist von sich aus
  //     tastaturbedienbar — genau die Art Entscheidung, die im Gespräch gern gefragt wird. Wenn du
  //     es stattdessen mit einem Signal und `[hidden]` baust: begründe, was du dafür gewinnst.
  //   - „Zurück" darf **nur beim letzten** erledigten Punkt erscheinen. Bei jedem anderen antwortet
  //     der Server mit `409` — „Zurücknehmen lässt sich nur der letzte erledigte Punkt". Du kannst
  //     dir den letzten hier selbst ausrechnen oder ihn als eigenen Input hereingeben; überleg, was
  //     die ehrlichere Schnittstelle ist.
  //   - Beim **ersten** erledigten Punkt des Tages ist `start` `null` — der Server weiß nicht, wann
  //     der Tag angefangen hat, nur wann der Punkt fertig war.
  //   - Wie in TODO 7: die `id` mitgeben, nicht „den letzten" abhaken lassen.
  //   - Ist noch nichts erledigt, soll das ganze `<details>` verschwinden.
  // ───────────────────────────────────────────────────────────────────────────────────────────────
}
