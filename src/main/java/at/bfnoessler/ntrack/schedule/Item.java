package at.bfnoessler.ntrack.schedule;

import java.time.ZonedDateTime;

/**
 * Ein Punkt des Tagesplans. {@code duration} sind Minuten, {@code fixedAt} eine feste Uhrzeit,
 * {@code doneAt} der Zeitpunkt, an dem der Punkt abgehakt wurde — alle drei optional.
 */
public record Item(String title, Integer duration, TimeOfDay fixedAt, ZonedDateTime doneAt) {

	/** Ein Punkt ohne Dauer und ohne feste Uhrzeit ist offen: Er läuft, bis weitergetippt wird. */
	public boolean isOpen() {
		return duration == null && fixedAt == null;
	}

}
