package at.bfnoessler.ntrack.today;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import at.bfnoessler.ntrack.schedule.DaySchedule;
import at.bfnoessler.ntrack.schedule.Item;
import at.bfnoessler.ntrack.schedule.ScheduledItem;
import at.bfnoessler.ntrack.schedule.TimeOfDay;
import at.bfnoessler.ntrack.schedule.Warning;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Der heutige Tag. Keine Service-Schicht: Die Transaktionsgrenze ist der Request, deshalb sitzt
 * {@code @Transactional} hier — und deshalb müssen die drei Methoden {@code public} sein, denn auf
 * nicht-öffentlichen Methoden ignoriert Spring die Annotation stillschweigend.
 */
@RestController
@RequestMapping("/api/today")
class TodayController {

	private final Clock clock;

	private final DayClock dayClock;

	private final DayRepository days;

	private final ItemRepository items;

	private final Templates templates;

	TodayController(Clock clock, DayClock dayClock, DayRepository days, ItemRepository items,
			Templates templates) {
		this.clock = clock;
		this.dayClock = dayClock;
		this.days = days;
		this.items = items;
		this.templates = templates;
	}

	/** Legt den Tag aus der Vorlage für den Wochentag an, falls es ihn noch nicht gibt. */
	@GetMapping
	@Transactional
	public TodayResponse today() {
		return respond(today(dayClock.date()));
	}

	/** Der aktuelle Punkt ist jetzt erledigt. */
	@PostMapping("next")
	@Transactional
	public TodayResponse next() {
		DayEntity day = today(dayClock.date());
		ItemEntity current = items.findFirstByDayIdAndDoneAtIsNullOrderByPositionAsc(day.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
						"Der Tag ist schon komplett erledigt."));
		current.setDoneAt(OffsetDateTime.now(clock));
		return respond(day);
	}

	/** Nimmt das letzte Weiter zurück, für Fehltipps. */
	@PostMapping("back")
	@Transactional
	public TodayResponse back() {
		DayEntity day = today(dayClock.date());
		ItemEntity last = items.findFirstByDayIdAndDoneAtIsNotNullOrderByPositionDesc(day.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
						"Heute ist noch kein Punkt erledigt."));
		last.setDoneAt(null);
		return respond(day);
	}

	private DayEntity today(LocalDate date) {
		return days.findByDate(date).orElseGet(() -> fromTemplate(date));
	}

	private DayEntity fromTemplate(LocalDate date) {
		DayEntity day = days.save(new DayEntity(date));
		List<Item> template = templates.forDay(date.getDayOfWeek());
		for (int position = 0; position < template.size(); position++) {
			items.save(ItemEntity.fromTemplate(day.getId(), position, template.get(position)));
		}
		return day;
	}

	private TodayResponse respond(DayEntity day) {
		ZonedDateTime now = ZonedDateTime.now(clock);
		List<Item> plan = items.findByDayIdOrderByPositionAsc(day.getId()).stream()
				.map(row -> toItem(row, now.getZone()))
				.toList();
		DaySchedule schedule = DaySchedule.calculate(plan, day.getDate(), now);

		return new TodayResponse(day.getDate(), now.toOffsetDateTime(),
				schedule.items().stream().map(TodayController::toResponse).toList(),
				schedule.warnings().stream().map(TodayController::toResponse).toList());
	}

	/**
	 * PostgreSQL gibt {@code timestamptz} mit Offset {@code Z} zurück, nicht in unserer Zone — ohne
	 * das Umrechnen stünde im JSON 16:00Z statt 18:00+02:00.
	 */
	private static Item toItem(ItemEntity row, ZoneId zone) {
		return new Item(row.getTitle(), row.getDuration(),
				row.getFixedAt() == null ? null : new TimeOfDay(row.getFixedAt()),
				row.getDoneAt() == null ? null : row.getDoneAt().atZoneSameInstant(zone));
	}

	private static ItemResponse toResponse(ScheduledItem scheduled) {
		return new ItemResponse(scheduled.item().title(), at(scheduled.start()), at(scheduled.end()),
				scheduled.status());
	}

	private static WarningResponse toResponse(Warning warning) {
		return new WarningResponse(warning.title(), warning.fixedAt().toString(),
				warning.minutesLate(), warning.message());
	}

	private static OffsetDateTime at(ZonedDateTime time) {
		return time == null ? null : time.toOffsetDateTime();
	}

}
