/**
 * Der Vertrag zum Backend, abgeschrieben von `TodayResponse`, `ItemResponse` und
 * `WarningResponse`. Alle drei Endpunkte antworten mit demselben `Plan`:
 *
 * - `GET /api/today` — der Tag, der jetzt gilt
 * - `POST /api/items/{id}/done` — diesen Punkt abhaken
 * - `DELETE /api/items/{id}/done` — das zurücknehmen
 *
 * `POST` und `DELETE` liefern also den kompletten neuen Plan zurück, und zwar den des Tages, zu
 * dem der Punkt gehört — nicht zwingend den heutigen.
 */

/**
 * Aktuell ist immer der erste nicht erledigte Punkt. Sind alle Punkte erledigt, hat **kein** Punkt
 * `CURRENT`.
 */
export type ItemStatus = 'DONE' | 'CURRENT' | 'UPCOMING';

export interface Item {
  /** Die Adresse zum Abhaken: damit sagt ein Client, welchen Punkt er meint, nicht „den aktuellen". */
  readonly id: number;

  readonly title: string;

  /**
   * ISO-8601 mit Wiener Offset, auf die Minute: `"2026-10-22T17:30:00+02:00"`.
   *
   * `null`, wo der Server keine Uhrzeit kennt — ein offener Punkt ohne Dauer reißt die Kette ab,
   * und die Punkte danach bekommen erst wieder eine Uhrzeit, wenn ein fester Punkt sie verankert.
   * Auf einem frischen Tag ist das der Normalfall, kein Ladezustand.
   */
  readonly start: string | null;

  /**
   * Wie `start`. Kann auf dem **Folgetag** liegen: „Im Bett" um `24:00`. Beim aktuellen Punkt ohne
   * Dauer ist `end === start === plan.now` — der Punkt läuft, bis man tippt.
   */
  readonly end: string | null;

  readonly status: ItemStatus;
}

export interface Warning {
  readonly title: string;

  /** Wanduhrzeit wie `"20:15"`, kein Zeitstempel; `"24:00"` ist möglich. */
  readonly fixedAt: string;

  readonly minutesLate: number;

  /** Fertiger Text: `"Feierabend 20:15: 63 min zu spät"`. Direkt anzeigen, nicht neu bauen. */
  readonly message: string;
}

export interface Plan {
  /**
   * Der Tag, der gilt, als `"2026-10-22"`. **Nicht** aus `now` ableiten: `ntrack.day-starts-at` ist
   * `04:00`, zwischen 00:00 und 03:59 gilt noch der Vortag.
   */
  readonly date: string;

  /** Jetzt, aus der Uhr des Servers. */
  readonly now: string;

  /**
   * Nach `position` sortiert; die erledigten Punkte sind das führende Stück der Liste. **Nicht
   * umsortieren** — nach Uhrzeit sortiert stünde „Im Bett" um `24:00` vorne, weil es auf dem
   * Folgetag liegt.
   */
  readonly items: readonly Item[];

  /** Leer, solange sich alles halten lässt. */
  readonly warnings: readonly Warning[];
}

/** Die Begründung, die der Server bei `409` und `404` in `problem+json` unter `detail` mitgibt. */
export interface ProblemDetail {
  readonly title?: string;
  readonly status?: number;
  readonly detail?: string;
}
