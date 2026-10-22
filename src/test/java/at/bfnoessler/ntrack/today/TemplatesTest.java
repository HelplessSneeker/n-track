package at.bfnoessler.ntrack.today;

import java.time.DayOfWeek;
import java.util.List;

import at.bfnoessler.ntrack.schedule.Item;
import at.bfnoessler.ntrack.schedule.TimeOfDay;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TemplatesTest {

	private final Templates templates = new Templates();

	@Test
	void buildsAGymDayFromTheMorningPlusGym() {
		List<Item> items = templates.forDay(DayOfWeek.WEDNESDAY);

		assertThat(items).hasSize(13);
		assertThat(items.get(0).title()).isEqualTo("Aufstehen: wiegen, LSF");
		assertThat(items.get(0).isOpen()).isTrue();
		assertThat(items.get(2).duration()).isEqualTo(1);
		assertThat(items.get(4).title()).startsWith("Heimweg");
		assertThat(items.get(5).duration()).isEqualTo(75);
		assertThat(items).isEqualTo(templates.forDay(DayOfWeek.MONDAY))
				.isEqualTo(templates.forDay(DayOfWeek.FRIDAY));
	}

	@Test
	void buildsARunningDayFromTheMorningPlusRunning() {
		List<Item> items = templates.forDay(DayOfWeek.TUESDAY);

		assertThat(items).hasSize(15);
		assertThat(items.get(4).title()).isEqualTo("Laufen nach der Arbeit");
		assertThat(items).isEqualTo(templates.forDay(DayOfWeek.THURSDAY));
	}

	@Test
	void keepsTheWeekendShort() {
		assertThat(templates.forDay(DayOfWeek.SATURDAY)).hasSize(3);
		assertThat(templates.forDay(DayOfWeek.SUNDAY)).hasSize(2);
	}

	@Test
	void readsFixedTimesAsTimesAndNotAsNumbers() {
		List<Item> items = templates.forDay(DayOfWeek.WEDNESDAY);

		assertThat(items.get(10).title()).isEqualTo("Feierabend");
		assertThat(items.get(10).fixedAt()).isEqualTo(TimeOfDay.of("20:15"));
		assertThat(items.get(12).title()).isEqualTo("Im Bett");
		assertThat(items.get(12).fixedAt()).isEqualTo(TimeOfDay.of("24:00"));
	}

	@Test
	void readsFixedTimeAndDurationTogether() {
		List<Item> items = templates.forDay(DayOfWeek.WEDNESDAY);

		assertThat(items.get(11).title()).isEqualTo("Hautpflege");
		assertThat(items.get(11).fixedAt()).isEqualTo(TimeOfDay.of("23:50"));
		assertThat(items.get(11).duration()).isEqualTo(10);
	}

	@Test
	void leavesTemplateItemsUndone() {
		assertThat(templates.forDay(DayOfWeek.WEDNESDAY))
				.allSatisfy(item -> assertThat(item.doneAt()).isNull());
	}

}
