package at.bfnoessler.ntrack.today;

import java.time.Clock;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

	@TestConfiguration
	static class FixedClockConfig {

		/** Mittwoch, 21.10.2026, 17:30 — der Tag aus den Rechenbeispielen. */
		@Bean
		@Primary
		Clock fixedClock() {
			return Clock.fixed(ZonedDateTime.of(2026, 10, 21, 17, 30, 0, 0, ZONE).toInstant(), ZONE);
		}

	}

	@Autowired
	private MockMvc mvc;

	@Autowired
	private DayRepository days;

	@Autowired
	private ItemRepository items;

	@BeforeEach
	void emptyTheDatabase() {
		items.deleteAllInBatch();
		days.deleteAllInBatch();
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
	void countsTheRestOfTheDayFromNowOnceWorkIsTheCurrentItem() throws Exception {
		mvc.perform(get("/api/today")).andExpect(status().isOk());
		next();
		next();
		next();

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
				.andExpect(jsonPath("$.items[12].start").value("2026-10-22T00:00:00+02:00"))
				.andExpect(jsonPath("$.warnings.length()").value(1))
				.andExpect(jsonPath("$.warnings[0].title").value("Feierabend"))
				.andExpect(jsonPath("$.warnings[0].fixedAt").value("20:15"))
				.andExpect(jsonPath("$.warnings[0].minutesLate").value(63))
				.andExpect(jsonPath("$.warnings[0].message")
						.value("Feierabend 20:15: 63 min zu spät"));
	}

	@Test
	void marksTheCurrentItemAsDoneAndMovesOn() throws Exception {
		mvc.perform(get("/api/today")).andExpect(status().isOk());

		mvc.perform(post("/api/today/next"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].status").value("DONE"))
				.andExpect(jsonPath("$.items[0].end").value("2026-10-21T17:30:00+02:00"))
				.andExpect(jsonPath("$.items[1].status").value("CURRENT"));
	}

	@Test
	void takesTheLastNextBack() throws Exception {
		mvc.perform(get("/api/today")).andExpect(status().isOk());
		next();
		next();

		mvc.perform(get("/api/today"))
				.andExpect(jsonPath("$.items[1].status").value("DONE"))
				.andExpect(jsonPath("$.items[2].status").value("CURRENT"));

		mvc.perform(post("/api/today/back"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].status").value("DONE"))
				.andExpect(jsonPath("$.items[1].status").value("CURRENT"))
				.andExpect(jsonPath("$.items[2].status").value("UPCOMING"));
	}

	@Test
	void refusesToGoOnWhenTheWholeDayIsDone() throws Exception {
		mvc.perform(get("/api/today")).andExpect(status().isOk());
		for (int i = 0; i < 13; i++) {
			next();
		}

		mvc.perform(post("/api/today/next"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value("Der Tag ist schon komplett erledigt."));
	}

	@Test
	void refusesToGoBackWhenNothingIsDoneYet() throws Exception {
		mvc.perform(get("/api/today")).andExpect(status().isOk());

		mvc.perform(post("/api/today/back"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value("Heute ist noch kein Punkt erledigt."));
	}

	private void next() throws Exception {
		mvc.perform(post("/api/today/next")).andExpect(status().isOk());
	}

}
