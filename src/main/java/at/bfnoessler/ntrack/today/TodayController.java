package at.bfnoessler.ntrack.today;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.IntStream;

import at.bfnoessler.ntrack.schedule.DaySchedule;
import at.bfnoessler.ntrack.schedule.Item;
import at.bfnoessler.ntrack.schedule.ScheduledItem;
import at.bfnoessler.ntrack.schedule.TimeOfDay;
import at.bfnoessler.ntrack.schedule.Warning;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Der Tagesplan. Keine Service-Schicht: Die Transaktionsgrenze ist der Request, deshalb sitzt
 * {@code @Transactional} an den drei Endpunkten.
 * <p>
 * Die Falle dabei ist nicht die Sichtbarkeit — seit Spring 6.0 wirkt die Annotation bei
 * Klassen-Proxys auch auf package-private Methoden —, sondern der Aufruf aus derselben Klasse: Der
 * geht am Proxy vorbei und läuft ohne Transaktion. Hier kommt deshalb jeder Request von außen an
 * einer annotierten Methode an, und die Helfer darunter tragen keine eigene Annotation.
 * <p>
 * Kein Mapping an der Klasse, weil die Punkte unter {@code /api/items} liegen und der Tag unter
 * {@code /api/today}. Ein zweiter Controller dafür müsste sich {@link #respond} teilen, und genau
 * das wäre die Schicht, die hier nicht sein soll.
 */
@RestController
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
	@GetMapping("/api/today")
	@Transactional
	public TodayResponse today() {
		return respond(today(dayClock.date()));
	}

	/**
	 * Dieser Punkt ist jetzt erledigt. Ist er es schon, ändert sich nichts: Ein Doppeltipp am Handy
	 * und ein zweiter Versuch bei schlechtem Netz dürfen nicht zwei Punkte abhaken.
	 */
	@PostMapping("/api/items/{id}/done")
	@Transactional
	public TodayResponse done(@PathVariable Integer id) {
		ItemEntity item = item(id);
		if (item.getDoneAt() == null) {
			ItemEntity current = items.findFirstByDayIdAndDoneAtIsNullOrderByPositionAsc(item.getDayId())
					.orElseThrow(() -> new IllegalStateException(
							"Punkt " + id + " ist offen, sein Tag aber komplett erledigt."));
			if (!current.getId().equals(item.getId())) {
				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"Dieser Punkt ist noch nicht dran, offen ist: " + current.getTitle());
			}
			item.setDoneAt(OffsetDateTime.now(clock));
		}
		return respond(day(item));
	}

	/**
	 * Nimmt das Abhaken zurück, für Fehltipps — aber nur beim letzten erledigten Punkt seines Tages,
	 * denn erledigt ist immer ein Anfangsstück des Tages. Ist der Punkt nicht erledigt, ändert sich
	 * nichts.
	 */
	@DeleteMapping("/api/items/{id}/done")
	@Transactional
	public TodayResponse undone(@PathVariable Integer id) {
		ItemEntity item = item(id);
		if (item.getDoneAt() != null) {
			ItemEntity last = items.findFirstByDayIdAndDoneAtIsNotNullOrderByPositionDesc(item.getDayId())
					.orElseThrow(() -> new IllegalStateException(
							"Punkt " + id + " ist erledigt, sein Tag aber gar nicht angefangen."));
			if (!last.getId().equals(item.getId())) {
				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"Zurücknehmen lässt sich nur der letzte erledigte Punkt: " + last.getTitle());
			}
			item.setDoneAt(null);
		}
		return respond(day(item));
	}

	private ItemEntity item(Integer id) {
		return items.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
				"Diesen Punkt gibt es nicht: " + id));
	}

	/**
	 * Der Tag, zu dem der Punkt gehört, und nicht der heutige: Eine seit gestern offene Ansicht soll
	 * den Plan zurückbekommen, in dem sie gerade steht. Angelegt wird hier nichts — wenn es den Punkt
	 * gibt, gibt es seinen Tag.
	 */
	private DayEntity day(ItemEntity item) {
		return days.findById(item.getDayId())
				.orElseThrow(() -> new IllegalStateException(
						"Punkt " + item.getId() + " zeigt auf den Tag " + item.getDayId() + ", den es "
								+ "nicht gibt."));
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

	/**
	 * Die Ids kommen aus den Zeilen und nicht aus {@link Item}: Das Rechen-Paket soll von Datenbank
	 * nichts wissen. Beide Listen stehen in derselben Reihenfolge, denn {@code calculate} geht die
	 * Punkte der Reihe nach durch.
	 */
	private TodayResponse respond(DayEntity day) {
		ZonedDateTime now = ZonedDateTime.now(clock);
		List<ItemEntity> rows = items.findByDayIdOrderByPositionAsc(day.getId());
		List<Item> plan = rows.stream().map(row -> toItem(row, now.getZone())).toList();
		DaySchedule schedule = DaySchedule.calculate(plan, day.getDate(), now);

		return new TodayResponse(day.getDate(), now.toOffsetDateTime(),
				IntStream.range(0, rows.size())
						.mapToObj(i -> toResponse(rows.get(i).getId(), schedule.items().get(i)))
						.toList(),
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

	private static ItemResponse toResponse(Integer id, ScheduledItem scheduled) {
		return new ItemResponse(id, scheduled.item().title(), at(scheduled.start()),
				at(scheduled.end()), scheduled.status());
	}

	private static WarningResponse toResponse(Warning warning) {
		return new WarningResponse(warning.title(), warning.fixedAt().toString(),
				warning.minutesLate(), warning.message());
	}

	private static OffsetDateTime at(ZonedDateTime time) {
		return time == null ? null : time.toOffsetDateTime();
	}

}
