package at.bfnoessler.ntrack.schedule;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Der gerechnete Tag: alle Punkte mit Zeiten und Status, dazu die Warnungen zu festen Punkten, die
 * nicht mehr zu halten sind.
 */
public record DaySchedule(List<ScheduledItem> items, List<Warning> warnings) {

	/**
	 * Rechnet die Uhrzeiten eines Tages ab {@code now}. Reine Funktion: Die Zone kommt aus
	 * {@code now}, gelesen wird sonst nichts.
	 * <p>
	 * Zwei Arten von Zeitrechnung treffen hier aufeinander, und das ist Absicht: Feste Uhrzeiten sind
	 * Wanduhrzeiten (siehe {@link TimeOfDay#atDate}), Dauern dagegen echte Minuten.
	 * <p>
	 * Ein offener Punkt verhält sich je nach Status verschieden: Als aktueller Punkt endet er jetzt
	 * ("wenn ich jetzt fertig bin") und die Kette läuft exakt weiter. Als kommender Punkt hat er einen
	 * Start, aber kein Ende — ab dort ist nur noch eine Untergrenze bekannt, und die Punkte danach
	 * haben keine Uhrzeit, bis ein fester Punkt die Kette wieder verankert.
	 * <p>
	 * Erledigte Punkte sind Protokoll, nicht Rechnung: Ihr Ende ist der gemessene Zeitpunkt, eine
	 * feste Uhrzeit bleibt außen vor (sonst entstünden umgedrehte Intervalle).
	 */
	public static DaySchedule calculate(List<Item> items, LocalDate date, ZonedDateTime now) {
		ZoneId zone = now.getZone();
		int current = IntStream.range(0, items.size())
				.filter(i -> items.get(i).doneAt() == null)
				.findFirst()
				.orElse(items.size());

		List<ScheduledItem> scheduled = new ArrayList<>(items.size());
		List<Warning> warnings = new ArrayList<>();

		// Ende der Kette, solange exakt bekannt, sonst deren Untergrenze. Null, bis die Kette beginnt.
		ZonedDateTime limit = null;
		boolean exact = true;

		for (int i = 0; i < items.size(); i++) {
			Item item = items.get(i);
			ZonedDateTime fixed = item.fixedAt() == null ? null : item.fixedAt().atDate(date, zone);
			ZonedDateTime start;
			ZonedDateTime end;

			if (i < current) {
				start = limit;
				end = item.doneAt();
				limit = end;
			}
			else if (i == current) {
				start = limit == null ? now : limit;
				if (fixed != null && fixed.isAfter(start)) {
					start = fixed;
				}
				end = later(item.duration() == null ? start : start.plusMinutes(item.duration()), now);
				limit = end;
			}
			// Ab hier ist die Kette angelaufen, limit also gesetzt.
			else if (fixed != null) {
				if (limit.isAfter(fixed)) {
					warnings.add(new Warning(item.title(), item.fixedAt(),
							Duration.between(fixed, limit).toMinutes()));
				}
				start = later(fixed, limit);
				end = item.duration() == null ? start : start.plusMinutes(item.duration());
				limit = end;
				exact = true;
			}
			else if (!exact) {
				start = null;
				end = null;
				if (item.duration() != null) {
					limit = limit.plusMinutes(item.duration());
				}
			}
			else if (item.isOpen()) {
				start = limit;
				end = null;
				exact = false;
			}
			else {
				start = limit;
				end = start.plusMinutes(item.duration());
				limit = end;
			}

			scheduled.add(new ScheduledItem(item, start, end, statusAt(i, current)));
		}

		return new DaySchedule(List.copyOf(scheduled), List.copyOf(warnings));
	}

	private static Status statusAt(int index, int current) {
		if (index < current) {
			return Status.DONE;
		}
		return index == current ? Status.CURRENT : Status.UPCOMING;
	}

	private static ZonedDateTime later(ZonedDateTime one, ZonedDateTime other) {
		return one.isAfter(other) ? one : other;
	}

}
