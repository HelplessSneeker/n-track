import { Component } from '@angular/core';

/** Die Warnungen über fest terminierte Punkte, die nicht mehr zu halten sind. Ganz oben. */
@Component({
  selector: 'app-warning-list',
  imports: [],
  templateUrl: './warning-list.html',
  styleUrl: './warning-list.scss',
})
export class WarningList {
  // ───────────────────────────────────────────────────────────────────────────────────────────────
  // TODO(human) 6 — die Warnungen
  //
  // Kontext: `plan.warnings` ist eine Liste von `Warning` (siehe `../plan`). Diese Komponente zeigt
  //   sie nur an, sie entscheidet nichts. Im Template wartet `<ul class="warnings">`.
  //
  // Aufgabe: Ein `warnings`-Input, dazu im Template das `@for` darüber.
  //
  // Achtung:
  //   - `message` ist schon fertiger deutscher Text („Feierabend 20:15: 63 min zu spät"). Setz ihn
  //     **nicht** aus `title`, `fixedAt` und `minutesLate` neu zusammen — die Texte rechnet und
  //     formuliert der Server, das ist die Hausregel dieses Projekts.
  //   - `track` ist in `@for` Pflicht. Eine Warnung hat keine `id` — überleg dir, was hier eine
  //     stabile Identität ist, und warum `$index` die schlechtere Wahl wäre.
  //   - Ist die Liste leer, soll **nichts** erscheinen: keine leere Überschrift, kein leerer
  //     Kasten. Das SCSS blendet eine leere `<ul>` zwar aus, damit das Gerüst von Anfang an gut
  //     aussieht — aber stütz die Logik nicht auf dieses CSS-Detail, bau das `@if`.
  //   - Das Ganze ist eine Warnung für Nutzer, kein Fehler: `role="alert"` wäre zu laut, weil die
  //     Liste sich minütlich neu aufbaut. Überleg, ob `aria-live="polite"` hier passt.
  // ───────────────────────────────────────────────────────────────────────────────────────────────
}
