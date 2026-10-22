package at.bfnoessler.ntrack.today;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Gegen das echte PostgreSQL aus {@code compose.yaml}, aber in einem eigenen Schema: Sonst wuerde
 * jedes {@code ./mvnw verify} den heutigen Tagesplan aus der Entwicklungs-Datenbank loeschen.
 * <p>
 * Aufgeraeumt wird per DELETE und nicht per Transaktions-Rollback: Sonst teilten Test und
 * Controller eine Hibernate-Session, und genau die Fehler, die ein Datenbanktest finden soll --
 * fehlender Flush, verletzte Unique-Bedingung, falsche Position -- blieben verborgen.
 */
@SpringBootTest(properties = { "spring.docker.compose.skip.in-tests=false",
		"spring.docker.compose.lifecycle-management=start_only",
		"spring.flyway.schemas=test",
		"spring.jpa.properties.hibernate.default_schema=test" })
@AutoConfigureMockMvc
class TodayApiTest {

	private static final ZoneId ZONE = ZoneId.of("Europe/Vienna");

	/** Mittwoch, 21.10.2026, 17:30 — der Tag aus den Rechenbeispielen. */
	private static final ZonedDateTime WEDNESDAY_EVENING = ZonedDateTime.of(2026, 10, 21, 17, 30, 0, 0,
			ZONE);

	/**
	 * Eine Uhr, die ein Test weiterstellen kann. So reicht ein Spring-Kontext für alle Fälle, und der
	 * Tageswechsel ist prüfbar, ohne den Kontext ein zweites Mal hochzufahren.
	 */
	static final class AdjustableClock extends Clock {

		private volatile Instant instant = WEDNESDAY_EVENING.toInstant();

		void set(ZonedDateTime time) {
			this.instant = time.toInstant();
		}

		@Override
		public Instant instant() {
			return this.instant;
		}

		@Override
		public ZoneId getZone() {
			return ZONE;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			throw new UnsupportedOperationException("Die Testuhr bleibt in " + ZONE);
		}

	}

	@TestConfiguration
	static class AdjustableClockConfig {

		@Bean
		@Primary
		AdjustableClock adjustableClock() {
			return new AdjustableClock();
		}

	}

	@Autowired
	private MockMvc mvc;

	@Autowired
	private AdjustableClock clock;

	@Autowired
	private DayRepository days;

	@Autowired
	private ItemRepository items;

	@BeforeEach
	void emptyTheDatabaseAndResetTheClock() {
		items.deleteAllInBatch();
		days.deleteAllInBatch();
		clock.set(WEDNESDAY_EVENING);
	}

	@Test
	void createsTodayFromTheWeekdayTemplate() throws Exception {
		mvc.perform(get("/api/today"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.date").value("2026-10-21"))
				.andExpect(jsonPath("$.now").value("2026-10-21T17:30:00+02:00"))
				.andExpect(jsonPath("$.items.length()").value(13))
				.andExpect(jsonPath("$.items[0].title").value("Aufstehen: wiegen, LSF"))
				.andExpect(jsonPath("$.items[0].status").value("CURRENT"))
				.andExpect(jsonPath("$.items[12].title").value("Im Bett"));

		assertThat(days.count()).isOne();
		assertThat(items.count()).isEqualTo(13);
	}

	@Test
	void doesNotCreateASecondDayOnTheSameDate() throws Exception {
		mvc.perform(get("/api/today")).andExpect(status().isOk());
		mvc.perform(get("/api/today")).andExpect(status().isOk());

		assertThat(days.count()).isOne();
		assertThat(items.count()).isEqualTo(13);
	}

	@Test
	void keepsYesterdayUntilTheDayStartsAtFour() throws Exception {
		mvc.perform(get("/api/today")).andExpect(status().isOk());

		clock.set(ZonedDateTime.of(2026, 10, 22, 0, 30, 0, 0, ZONE));

		mvc.perform(get("/api/today"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.date").value("2026-10-21"))
				.andExpect(jsonPath("$.now").value("2026-10-22T00:30:00+02:00"));

		assertThat(days.count()).isOne();
		assertThat(items.count()).isEqualTo(13);
	}

	@Test
	void startsTheNewDayAtFour() throws Exception {
		mvc.perform(get("/api/today")).andExpect(status().isOk());

		clock.set(ZonedDateTime.of(2026, 10, 22, 4, 0, 0, 0, ZONE));

		mvc.perform(get("/api/today"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.date").value("2026-10-22"))
				// Donnerstag ist werktag_morgen + laufen, also 4 + 11 Punkte.
				.andExpect(jsonPath("$.items.length()").value(15));

		assertThat(days.count()).isEqualTo(2);
	}

	@Test
	void countsTheRestOfTheDayFromNowOnceWorkIsTheCurrentItem() throws Exception {
		mvc.perform(get("/api/today")).andExpect(status().isOk());
		done(0);
		done(1);
		done(2);

		mvc.perform(get("/api/today"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[3].title").value("Arbeit, mittags Anki 10 min"))
				.andExpect(jsonPath("$.items[3].status").value("CURRENT"))
				.andExpect(jsonPath("$.items[3].end").value("2026-10-21T17:30:00+02:00"))
				.andExpect(jsonPath("$.items[4].start").value("2026-10-21T17:30:00+02:00"))
				.andExpect(jsonPath("$.items[4].end").value("2026-10-21T18:00:00+02:00"))
				.andExpect(jsonPath("$.items[5].end").value("2026-10-21T19:15:00+02:00"))
				.andExpect(jsonPath("$.items[6].end").value("2026-10-21T20:15:00+02:00"))
				.andExpect(jsonPath("$.items[7].end").value("2026-10-21T20:18:00+02:00"))
				.andExpect(jsonPath("$.items[8].end").value("2026-10-21T21:18:00+02:00"))
				.andExpect(jsonPath("$.items[9].start").value("2026-10-21T21:18:00+02:00"))
				.andExpect(jsonPath("$.items[9].end").doesNotExist())
				// Die Hautpflege haengt nicht am Feierabend, sie liegt vor dem Schlafengehen.
				.andExpect(jsonPath("$.items[11].start").value("2026-10-21T23:50:00+02:00"))
				.andExpect(jsonPath("$.items[11].end").value("2026-10-22T00:00:00+02:00"))
				.andExpect(jsonPath("$.items[12].start").value("2026-10-22T00:00:00+02:00"))
				.andExpect(jsonPath("$.warnings.length()").value(1))
				.andExpect(jsonPath("$.warnings[0].title").value("Feierabend"))
				.andExpect(jsonPath("$.warnings[0].fixedAt").value("20:15"))
				.andExpect(jsonPath("$.warnings[0].minutesLate").value(63))
				.andExpect(jsonPath("$.warnings[0].message")
						.value("Feierabend 20:15: 63 min zu spät"));
	}

	@Test
	void givesEveryItemItsId() throws Exception {
		mvc.perform(get("/api/today")).andExpect(status().isOk());

		mvc.perform(get("/api/today"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].id").value(idAt(0)))
				.andExpect(jsonPath("$.items[12].id").value(idAt(12)));
	}

	@Test
	void marksTheNamedItemAsDoneAndMovesOn() throws Exception {
		mvc.perform(get("/api/today")).andExpect(status().isOk());

		mvc.perform(post("/api/items/{id}/done", idAt(0)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].status").value("DONE"))
				.andExpect(jsonPath("$.items[0].end").value("2026-10-21T17:30:00+02:00"))
				.andExpect(jsonPath("$.items[1].status").value("CURRENT"));
	}

	@Test
	void marksAnItemOnlyOnceHoweverOftenItIsTapped() throws Exception {
		mvc.perform(get("/api/today")).andExpect(status().isOk());

		done(0);
		mvc.perform(post("/api/items/{id}/done", idAt(0)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].status").value("DONE"))
				.andExpect(jsonPath("$.items[1].status").value("CURRENT"));
	}

	@Test
	void refusesToMarkAnItemThatIsNotDueYet() throws Exception {
		mvc.perform(get("/api/today")).andExpect(status().isOk());

		mvc.perform(post("/api/items/{id}/done", idAt(3)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail")
						.value("Dieser Punkt ist noch nicht dran, offen ist: Aufstehen: wiegen, LSF"));
	}

	@Test
	void takesTheLastDoneItemBack() throws Exception {
		mvc.perform(get("/api/today")).andExpect(status().isOk());
		done(0);
		done(1);

		mvc.perform(delete("/api/items/{id}/done", idAt(1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].status").value("DONE"))
				.andExpect(jsonPath("$.items[1].status").value("CURRENT"))
				.andExpect(jsonPath("$.items[2].status").value("UPCOMING"));
	}

	@Test
	void refusesToTakeBackAnEarlierDoneItem() throws Exception {
		mvc.perform(get("/api/today")).andExpect(status().isOk());
		done(0);
		done(1);

		mvc.perform(delete("/api/items/{id}/done", idAt(0)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value("Zurücknehmen lässt sich nur der letzte "
						+ "erledigte Punkt: Arbeitsweg: Lesen 20 min"));
	}

	@Test
	void leavesAnItemThatIsNotDoneAloneWhenItIsTakenBack() throws Exception {
		mvc.perform(get("/api/today")).andExpect(status().isOk());

		mvc.perform(delete("/api/items/{id}/done", idAt(0)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].status").value("CURRENT"));
	}

	@Test
	void answersWithProblemJsonForAnUnknownItem() throws Exception {
		mvc.perform(post("/api/items/{id}/done", 999999))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.detail").value("Diesen Punkt gibt es nicht: 999999"));

		mvc.perform(delete("/api/items/{id}/done", 999999))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.detail").value("Diesen Punkt gibt es nicht: 999999"));
	}

	/** Der Fall, der die Ids motiviert: eine seit gestern offene Ansicht nach Mitternacht. */
	@Test
	void marksAnItemInItsOwnDayAfterMidnight() throws Exception {
		mvc.perform(get("/api/today")).andExpect(status().isOk());
		Integer id = idAt(0);

		clock.set(ZonedDateTime.of(2026, 10, 22, 0, 30, 0, 0, ZONE));

		mvc.perform(post("/api/items/{id}/done", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.date").value("2026-10-21"))
				.andExpect(jsonPath("$.items[0].status").value("DONE"))
				.andExpect(jsonPath("$.items[0].end").value("2026-10-22T00:30:00+02:00"));

		assertThat(days.count()).isOne();
	}

	private void done(int position) throws Exception {
		mvc.perform(post("/api/items/{id}/done", idAt(position))).andExpect(status().isOk());
	}

	/** Die Id des Punktes an dieser Stelle des Mittwochs-Plans. */
	private Integer idAt(int position) {
		DayEntity day = days.findByDate(WEDNESDAY_EVENING.toLocalDate()).orElseThrow();
		return items.findByDayIdOrderByPositionAsc(day.getId()).get(position).getId();
	}

}
