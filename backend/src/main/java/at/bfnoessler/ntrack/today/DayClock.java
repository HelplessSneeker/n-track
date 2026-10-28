package at.bfnoessler.ntrack.today;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Welcher Tagesplan jetzt gilt. Eigene Klasse und nicht eine Methode im Controller, damit die Frage
 * ohne Spring und ohne Datenbank zu testen ist.
 */
@Component
class DayClock {

	private final Clock clock;

	private final LocalTime dayStartsAt;

	DayClock(Clock clock, @Value("${ntrack.day-starts-at}") String dayStartsAt) {
		this.clock = clock;
		this.dayStartsAt = LocalTime.parse(dayStartsAt);
	}

	/**
	 * Das Datum des Tagesplans, der jetzt gilt: vor {@code ntrack.day-starts-at} noch der Vortag. Um
	 * 00:30 ist also noch gestern, und ein Abhaken landet nicht in einem frisch angelegten Morgen.
	 * <p>
	 * Verglichen wird die Wanduhrzeit und nicht ein Abstand zum Tagesbeginn: "vor 04:00" ist eine
	 * Aussage über die Uhr an der Wand, und an den Umstellungstagen (So 25.10.2026, So 29.03.2026)
	 * hat der Tag nicht 24 Stunden.
	 */
	LocalDate date() {
		ZonedDateTime now = ZonedDateTime.now(this.clock);
		return now.toLocalTime().isBefore(this.dayStartsAt) ? now.toLocalDate().minusDays(1)
				: now.toLocalDate();
	}

}
