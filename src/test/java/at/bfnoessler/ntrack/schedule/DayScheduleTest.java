package at.bfnoessler.ntrack.schedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DayScheduleTest {

	private static final ZoneId ZONE = ZoneId.of("Europe/Vienna");

	/** Mittwoch, 21.10.2026 — die Vorlage werktag_morgen + gym. */
	private static final LocalDate WEDNESDAY = LocalDate.of(2026, 10, 21);

	// --- Fall 1: Arbeit ist aktuell -------------------------------------------------------------

	@Test
	void chainsUpcomingItemsBehindTheCurrentOne() {
		DaySchedule plan = DaySchedule.calculate(wednesdayDoneUntil("06:40", "08:00", "08:01"),
				WEDNESDAY, at(17, 30));

		assertThat(item(plan, "Arbeit,").start()).isEqualTo(at(8, 1));
		assertThat(item(plan, "Arbeit,").end()).isEqualTo(at(17, 30));
		assertTimes(plan, "Heimweg", at(17, 30), at(18, 0));
		assertTimes(plan, "Gym", at(18, 0), at(19, 15));
		assertTimes(plan, "Kochen", at(19, 15), at(20, 15));
		assertTimes(plan, "Meditieren", at(20, 15), at(20, 18));
		assertTimes(plan, "Deep Work", at(20, 18), at(21, 18));
	}

	@Test
	void warnsAboutTheFixedItemTheChainOverruns() {
		DaySchedule plan = DaySchedule.calculate(wednesdayDoneUntil("06:40", "08:00", "08:01"),
				WEDNESDAY, at(17, 30));

		assertThat(plan.warnings()).singleElement().satisfies(warning -> {
			assertThat(warning.title()).isEqualTo("Feierabend");
			assertThat(warning.minutesLate()).isEqualTo(63);
			assertThat(warning.message()).isEqualTo("Feierabend 20:15: 63 min zu spät");
		});
	}

	@Test
	void givesTheOpenUpcomingItemAStartButNoEnd() {
		DaySchedule plan = DaySchedule.calculate(wednesdayDoneUntil("06:40", "08:00", "08:01"),
				WEDNESDAY, at(17, 30));

		assertThat(item(plan, "Aufgaben").start()).isEqualTo(at(21, 18));
		assertThat(item(plan, "Aufgaben").end()).isNull();
	}

	@Test
	void marksEveryItemWithItsStatus() {
		DaySchedule plan = DaySchedule.calculate(wednesdayDoneUntil("06:40", "08:00", "08:01"),
				WEDNESDAY, at(17, 30));

		assertThat(item(plan, "Aufstehen").status()).isEqualTo(Status.DONE);
		assertThat(item(plan, "In der Daily").status()).isEqualTo(Status.DONE);
		assertThat(item(plan, "Arbeit,").status()).isEqualTo(Status.CURRENT);
		assertThat(item(plan, "Heimweg").status()).isEqualTo(Status.UPCOMING);
		assertThat(item(plan, "Im Bett").status()).isEqualTo(Status.UPCOMING);
	}

	// --- Fall 2: die Grenzminute ----------------------------------------------------------------

	@Test
	void staysSilentWhileTheFixedItemIsStillExactlyReachable() {
		DaySchedule plan = DaySchedule.calculate(wednesdayDoneUntil("06:40", "08:00", "08:01"),
				WEDNESDAY, at(16, 27));

		assertThat(item(plan, "Deep Work").end()).isEqualTo(at(20, 15));
		assertThat(plan.warnings()).isEmpty();
	}

	@Test
	void warnsFromTheFirstMinuteOfDelay() {
		DaySchedule plan = DaySchedule.calculate(wednesdayDoneUntil("06:40", "08:00", "08:01"),
				WEDNESDAY, at(16, 28));

		assertThat(plan.warnings()).singleElement()
				.satisfies(warning -> assertThat(warning.minutesLate()).isEqualTo(1));
	}

	// --- Fall 3: der aktuelle Punkt laeuft ueber ------------------------------------------------

	@Test
	void endsTheCurrentItemNowWhenItRunsLongerThanPlanned() {
		DaySchedule plan = DaySchedule.calculate(
				wednesdayDoneUntil("06:40", "08:00", "08:01", "17:00", "18:00"), WEDNESDAY, at(19, 30));

		assertThat(item(plan, "Gym").status()).isEqualTo(Status.CURRENT);
		assertTimes(plan, "Gym", at(18, 0), at(19, 30));
		assertTimes(plan, "Kochen", at(19, 30), at(20, 30));
	}

	// --- Fall 4: noch nichts erledigt ----------------------------------------------------------

	@Test
	void leavesItemsBehindAnOpenOneWithoutTimesUntilAFixedItemAnchorsTheChain() {
		DaySchedule plan = DaySchedule.calculate(wednesday(), WEDNESDAY, at(7, 0));

		assertThat(item(plan, "Aufstehen").status()).isEqualTo(Status.CURRENT);
		assertTimes(plan, "Aufstehen", at(7, 0), at(7, 0));

		assertThat(item(plan, "Arbeitsweg").start()).isEqualTo(at(7, 0));
		assertThat(item(plan, "Arbeitsweg").end()).isNull();

		for (String ohneUhrzeit : List.of("In der Daily", "Arbeit,", "Heimweg", "Gym", "Kochen",
				"Meditieren", "Deep Work", "Aufgaben")) {
			assertThat(item(plan, ohneUhrzeit).start()).as(ohneUhrzeit).isNull();
			assertThat(item(plan, ohneUhrzeit).end()).as(ohneUhrzeit).isNull();
		}

		assertTimes(plan, "Feierabend", at(20, 15), at(20, 15));
		assertTimes(plan, "Hautpflege", at(20, 15), at(20, 25));
		assertThat(plan.warnings()).isEmpty();
	}

	// --- Fall 5: 24:00 ---------------------------------------------------------------------------

	@Test
	void putsMidnightOnTheNextDay() {
		DaySchedule plan = DaySchedule.calculate(wednesdayDoneUntil("06:40", "08:00", "08:01"),
				WEDNESDAY, at(17, 30));

		ZonedDateTime imBett = item(plan, "Im Bett").start();

		assertThat(imBett).isEqualTo(
				ZonedDateTime.of(LocalDate.of(2026, 10, 22), LocalTime.MIDNIGHT, ZONE));
		assertThat(imBett.getOffset()).isEqualTo(ZoneOffset.ofHours(2));
	}

	// --- Fall 6: Zeitumstellung ------------------------------------------------------------------

	@Test
	void keepsAFixedItemOnItsWallClockTimeWhenTheClocksGoBack() {
		LocalDate sunday = LocalDate.of(2026, 10, 25);
		List<Item> items = List.of(open("Vormittags: wiegen, LSF, Lesen 20 min, Spanisch"),
				fixed("Feierabend", "20:15"));

		DaySchedule plan = DaySchedule.calculate(items, sunday,
				ZonedDateTime.of(sunday, LocalTime.of(10, 0), ZONE));

		ZonedDateTime feierabend = item(plan, "Feierabend").start();

		assertThat(feierabend.toLocalTime()).isEqualTo(LocalTime.of(20, 15));
		assertThat(feierabend.getOffset()).isEqualTo(ZoneOffset.ofHours(1));
	}

	// --- Festlegungen zu Stellen, die die Beschreibung offen laesst ------------------------------

	@Test
	void leavesTheStartOfTheFirstDoneItemOpen() {
		DaySchedule plan = DaySchedule.calculate(wednesdayDoneUntil("06:40"), WEDNESDAY, at(7, 0));

		assertThat(item(plan, "Aufstehen").start()).isNull();
		assertThat(item(plan, "Aufstehen").end()).isEqualTo(at(6, 40));
		assertThat(item(plan, "Arbeitsweg").start()).isEqualTo(at(6, 40));
	}

	@Test
	void treatsADoneItemAsARecordAndIgnoresItsFixedTime() {
		List<Item> items = List.of(new Item("Feierabend", null, TimeOfDay.of("20:15"), at(19, 30)),
				lasting("Hautpflege", 10));

		DaySchedule plan = DaySchedule.calculate(items, WEDNESDAY, at(19, 35));

		assertThat(item(plan, "Feierabend").start()).isNull();
		assertThat(item(plan, "Feierabend").end()).isEqualTo(at(19, 30));
		assertTimes(plan, "Hautpflege", at(19, 30), at(19, 40));
	}

	@Test
	void doesNotStartACurrentFixedItemBeforeItsTime() {
		DaySchedule plan = DaySchedule.calculate(List.of(fixed("Aufstehen", "06:30")), WEDNESDAY,
				at(5, 0));

		assertTimes(plan, "Aufstehen", at(6, 30), at(6, 30));
	}

	@Test
	void handlesADayWithoutItems() {
		DaySchedule plan = DaySchedule.calculate(List.of(), WEDNESDAY, at(17, 30));

		assertThat(plan.items()).isEmpty();
		assertThat(plan.warnings()).isEmpty();
	}

	@Test
	void hasNoCurrentItemLeftWhenEverythingIsDone() {
		DaySchedule plan = DaySchedule.calculate(
				List.of(new Item("Aufstehen", null, null, at(6, 40))), WEDNESDAY, at(17, 30));

		assertThat(item(plan, "Aufstehen").status()).isEqualTo(Status.DONE);
	}

	// --- Helfer ----------------------------------------------------------------------------------

	private static ZonedDateTime at(int hour, int minute) {
		return ZonedDateTime.of(WEDNESDAY, LocalTime.of(hour, minute), ZONE);
	}

	private static Item open(String title) {
		return new Item(title, null, null, null);
	}

	private static Item lasting(String title, int minutes) {
		return new Item(title, minutes, null, null);
	}

	private static Item fixed(String title, String time) {
		return new Item(title, null, TimeOfDay.of(time), null);
	}

	private static List<Item> wednesday() {
		return List.of(open("Aufstehen: wiegen, LSF"),
				open("Arbeitsweg: Lesen 20 min"),
				lasting("In der Daily: abhaken, Gewicht eintragen, Tagesplan ansehen", 1),
				open("Arbeit, mittags Anki 10 min"),
				lasting("Heimweg: Language Transfer 10 min", 30),
				lasting("Gym", 75),
				lasting("Kochen, Essen", 60),
				lasting("Meditieren 3 min, danach Handy in einen anderen Raum", 3),
				lasting("Deep Work", 60),
				open("Aufgaben aus der Daily, wenn noch Zeit ist"),
				fixed("Feierabend", "20:15"),
				lasting("Hautpflege", 10),
				fixed("Im Bett", "24:00"));
	}

	/** Die Mittwoch-Vorlage, bei der die ersten Punkte zu diesen Uhrzeiten erledigt wurden. */
	private static List<Item> wednesdayDoneUntil(String... doneTimes) {
		List<Item> items = new ArrayList<>(wednesday());
		for (int i = 0; i < doneTimes.length; i++) {
			Item item = items.get(i);
			TimeOfDay time = TimeOfDay.of(doneTimes[i]);
			items.set(i, new Item(item.title(), item.duration(), item.fixedAt(),
					time.atDate(WEDNESDAY, ZONE)));
		}
		return List.copyOf(items);
	}

	private static ScheduledItem item(DaySchedule plan, String titlePrefix) {
		return plan.items().stream()
				.filter(scheduled -> scheduled.item().title().startsWith(titlePrefix))
				.findFirst()
				.orElseThrow(() -> new AssertionError("Kein Punkt beginnt mit " + titlePrefix));
	}

	private static void assertTimes(DaySchedule plan, String titlePrefix, ZonedDateTime start,
			ZonedDateTime end) {
		assertThat(item(plan, titlePrefix).start()).as(titlePrefix + " start").isEqualTo(start);
		assertThat(item(plan, titlePrefix).end()).as(titlePrefix + " end").isEqualTo(end);
	}

}
