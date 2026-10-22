package at.bfnoessler.ntrack.today;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DayClockTest {

	private static final ZoneId ZONE = ZoneId.of("Europe/Vienna");

	@Test
	void keepsYesterdayBeforeTheDayStarts() {
		// Do 22.10.2026 um 00:30: Die Hautpflege von Mittwoch ist noch offen.
		assertThat(at(2026, 10, 22, 0, 30).date()).isEqualTo(LocalDate.of(2026, 10, 21));
	}

	@Test
	void keepsYesterdayUntilTheLastMinuteBeforeTheBoundary() {
		assertThat(at(2026, 10, 22, 3, 59).date()).isEqualTo(LocalDate.of(2026, 10, 21));
	}

	@Test
	void startsTheNewDayOnTheBoundary() {
		assertThat(at(2026, 10, 22, 4, 0).date()).isEqualTo(LocalDate.of(2026, 10, 22));
	}

	@Test
	void givesTheCalendarDayForTheRestOfTheDay() {
		assertThat(at(2026, 10, 21, 17, 30).date()).isEqualTo(LocalDate.of(2026, 10, 21));
		assertThat(at(2026, 10, 21, 23, 59).date()).isEqualTo(LocalDate.of(2026, 10, 21));
	}

	@Test
	void goesByTheWallClockWhenTheClocksGoBack() {
		// So 25.10.2026 gibt es 02:30 zweimal. Beide Male ist es noch Samstag.
		LocalDate saturday = LocalDate.of(2026, 10, 24);
		ZonedDateTime beforeTheChange = ZonedDateTime.of(2026, 10, 25, 2, 30, 0, 0, ZONE);

		assertThat(dayClock(beforeTheChange).date()).isEqualTo(saturday);
		assertThat(dayClock(beforeTheChange.withLaterOffsetAtOverlap()).date()).isEqualTo(saturday);
	}

	private static DayClock at(int year, int month, int day, int hour, int minute) {
		return dayClock(ZonedDateTime.of(year, month, day, hour, minute, 0, 0, ZONE));
	}

	private static DayClock dayClock(ZonedDateTime now) {
		return new DayClock(Clock.fixed(now.toInstant(), ZONE), "04:00");
	}

}
