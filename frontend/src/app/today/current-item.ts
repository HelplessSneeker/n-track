import { Component } from '@angular/core';

/** „Jetzt": der aktuelle Punkt und der große Knopf, der ihn abhakt. Das Herz der Ansicht. */
@Component({
  selector: 'app-current-item',
  imports: [],
  templateUrl: './current-item.html',
  styleUrl: './current-item.scss',
})
export class CurrentItem {
  // ───────────────────────────────────────────────────────────────────────────────────────────────
  // TODO(human) 7 — „Jetzt" und der Knopf „Weiter"
  //
  // Kontext: Diese Komponente bekommt den einen Punkt, der gerade dran ist, und meldet nach oben,
  //   dass er erledigt ist. Sie ruft selbst nichts beim Server ab. Im Template warten die
  //   Uhrzeile, der Titel und der Knopf.
  //
  // Aufgabe: Die Inputs `item` und `busy`, der Output `next`, und im Template die Bindings: Titel,
  //   Uhrzeit von–bis, `(click)` und `[disabled]`.
  //
  // Achtung:
  //   - Der Knopf muss **genau diesen** Punkt meinen: gib die `id` mit nach oben, hak nicht „den
  //     aktuellen" ab. Genau dafür hat das Backend überhaupt `id`s — ein zweiter Versuch bei
  //     schlechtem Netz trifft dann denselben Punkt.
  //   - `[disabled]` an `busy` hängen, solange die Anfrage läuft. Ein Doppeltipp ist zwar harmlos
  //     (das Backend ist idempotent), aber der Knopf soll zeigen, dass etwas passiert.
  //   - `item` ist genau einer, nie eine Liste, und nie `undefined` — ob es einen aktuellen Punkt
  //     überhaupt gibt, entscheidet `today-page` mit einem `@if` (TODO 4). Dein Input darf also
  //     `required` sein. Überleg, warum das die bessere Grenze ist, als hier auf `undefined` zu
  //     prüfen.
  //   - Zur Uhrzeit: `start` ist `"2026-10-22T17:30:00+02:00"` — der Server hat die Wiener
  //     Wanduhrzeit also *schon hingeschrieben*. Du kannst sie mit `DatePipe` formatieren (dann
  //     `DatePipe` in `imports` aufnehmen und an die Zeitzone denken) oder die Zeichen 11–16 der
  //     Zeichenkette nehmen. Letzteres rechnet garantiert nichts um — und „Uhrzeiten rechnet nur
  //     der Server" ist die Hausregel dieses Projekts. Entscheide und begründe es.
  //   - `start === end` ist normal: ein Punkt ohne Dauer endet „jetzt" und läuft, bis du tippst.
  //     „17:30–17:30" liest sich schlecht. Und `start` kann `null` sein.
  //
  // ───────────────────────────────────────────────────────────────────────────────────────────────
  // TODO(human) 7b — „ab 23:50" statt „Jetzt"
  //
  // Kontext: Die Überschrift des Abschnitts steht in `today-page.html` fest auf „Jetzt". Das ist
  //   falsch, sobald der aktuelle Punkt erst später beginnt: Die Hautpflege hängt fest auf `23:50`,
  //   und um 23:00 ist sie schon `CURRENT` — aktuell ist beim Server der erste nicht erledigte
  //   Punkt, und seinen Start zieht er auf die feste Uhrzeit hoch. Dann soll „ab 23:50" dastehen.
  //
  // Aufgabe: Entscheide, **welche Komponente das Label trägt**, und bau es.
  //
  // Achtung — das ist eine Schnittstellenfrage, keine Formatierung:
  //   - Für den Vergleich brauchst du zwei Dinge: `item.start` (hast du) und `plan.now` (hat nur
  //     `today-page`). Also entweder die Überschrift bleibt oben und `today-page` rechnet sie in
  //     einem `computed()`, oder diese Komponente bekommt `now` als zweiten Input und trägt das
  //     Label selbst. Frag dich, welche Komponente die Uhr kennen *darf* — und ob „Jetzt" eine
  //     Abschnittsüberschrift ist (dann gehört sie nach oben) oder eine Aussage über den Punkt.
  //   - Vergleich auf dem **vollen Zeitstempel**, nicht auf `"23:50"` als Zeichenkette. „Im Bett"
  //     um `24:00` kommt als `"2026-10-22T00:00:00+02:00"` — als Text verglichen wäre das kleiner
  //     als jedes `"…T23:…"` desselben Tages, und die Überschrift stünde auf „Jetzt", obwohl der
  //     Punkt erst in einer Stunde anfängt.
  //   - `start` kann hier nicht `null` sein (der Server verankert den aktuellen Punkt immer am Ende
  //     des vorigen oder an `now`) — aber der Typ sagt `string | null`, du musst den Fall also
  //     behandeln. Überleg, was „kein Start" für die Überschrift heißen soll.
  // ───────────────────────────────────────────────────────────────────────────────────────────────
}
