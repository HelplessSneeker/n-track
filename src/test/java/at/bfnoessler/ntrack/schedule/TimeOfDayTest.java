package at.bfnoessler.ntrack.schedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class TimeOfDayTest {

	private static final ZoneId ZONE = ZoneId.of("Europe/Vienna");

	@Test
	void readsHoursAndMinutes() {
		assertThat(TimeOfDay.of("20:15").minutes()).isEqualTo(20 * 60 + 15);
		assertThat(TimeOfDay.of("8:00").minutes()).isEqualTo(8 * 60);
		assertThat(TimeOfDay.of("00:00").minutes()).isZero();
	}

	@Test
	void readsMidnightAtTheEndOfTheDay() {
		assertThat(TimeOfDay.of("24:00").minutes()).isEqualTo(24 * 60);
	}

	@Test
	void printsBackWhatItRead() {
		assertThat(TimeOfDay.of("20:15")).hasToString("20:15");
		assertThat(TimeOfDay.of("8:00")).hasToString("08:00");
		assertThat(TimeOfDay.of("24:00")).hasToString("24:00");
	}

	@Test
	void rejectsAnythingThatIsNotATimeOfDay() {
		assertThatIllegalArgumentException().isThrownBy(() -> TimeOfDay.of("24:01"));
		assertThatIllegalArgumentException().isThrownBy(() -> TimeOfDay.of("25:00"));
		assertThatIllegalArgumentException().isThrownBy(() -> TimeOfDay.of("20:60"));
		assertThatIllegalArgumentException().isThrownBy(() -> TimeOfDay.of("20:1"));
		assertThatIllegalArgumentException().isThrownBy(() -> TimeOfDay.of("2015"));
		assertThatIllegalArgumentException().isThrownBy(() -> TimeOfDay.of(""));
		assertThatIllegalArgumentException().isThrownBy(() -> TimeOfDay.of(null));
	}

	@Test
	void midnightFallsOnTheNextDay() {
		assertThat(TimeOfDay.of("24:00").atDate(LocalDate.of(2026, 10, 21), ZONE))
				.isEqualTo(ZonedDateTime.of(LocalDate.of(2026, 10, 22), LocalTime.MIDNIGHT, ZONE));
	}

	@Test
	void staysAWallClockTimeWhenTheClocksGoBack() {
		// So 25.10.2026 ist die Rueckstellung. Minuten auf den Tagesbeginn zu addieren ergaebe 19:15.
		ZonedDateTime abend = TimeOfDay.of("20:15").atDate(LocalDate.of(2026, 10, 25), ZONE);

		assertThat(abend.toLocalTime()).isEqualTo(LocalTime.of(20, 15));
		assertThat(abend.getOffset()).isEqualTo(ZoneOffset.ofHours(1));
	}

	@Test
	void stepsForwardOutOfTheSpringGap() {
		// So 29.03.2026 wird 02:00 auf 03:00 gestellt, 02:30 gibt es also nicht.
		ZonedDateTime fehlt = TimeOfDay.of("02:30").atDate(LocalDate.of(2026, 3, 29), ZONE);

		assertThat(fehlt.toLocalTime()).isEqualTo(LocalTime.of(3, 30));
		assertThat(fehlt.getOffset()).isEqualTo(ZoneOffset.ofHours(2));
	}

}
