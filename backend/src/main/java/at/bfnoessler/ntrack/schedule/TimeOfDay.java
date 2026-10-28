package at.bfnoessler.ntrack.schedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.regex.Pattern;

/**
 * Eine feste Uhrzeit im Tagesplan, als Minuten nach Mitternacht. {@code 1440} ist "24:00", also
 * Mitternacht am Ende des Tages.
 */
public record TimeOfDay(int minutes) implements Comparable<TimeOfDay> {

	private static final int MINUTES_PER_DAY = 24 * 60;

	private static final Pattern FORMAT = Pattern.compile("(\\d{1,2}):(\\d{2})");

	public TimeOfDay {
		if (minutes < 0 || minutes > MINUTES_PER_DAY) {
			throw new IllegalArgumentException("Uhrzeit liegt nicht zwischen 00:00 und 24:00: " + minutes);
		}
	}

	/** Liest "20:15", "8:00" oder "24:00". */
	public static TimeOfDay of(String text) {
		if (text == null || !FORMAT.matcher(text).matches()) {
			throw new IllegalArgumentException("Keine Uhrzeit im Format HH:mm: " + text);
		}
		int colon = text.indexOf(':');
		int hour = Integer.parseInt(text.substring(0, colon));
		int minute = Integer.parseInt(text.substring(colon + 1));
		if (minute > 59) {
			throw new IllegalArgumentException("Minute liegt nicht zwischen 00 und 59: " + text);
		}
		return new TimeOfDay(hour * 60 + minute);
	}

	public static TimeOfDay of(int hour, int minute) {
		return new TimeOfDay(hour * 60 + minute);
	}

	/**
	 * Diese Uhrzeit als Zeitpunkt an diesem Datum. "24:00" fällt auf den Folgetag um 00:00.
	 * <p>
	 * Bewusst über {@code atTime} und nicht über Minuten auf den Tagesbeginn: Eine feste Uhrzeit ist
	 * eine Wanduhrzeit, {@code plusMinutes} rechnet aber auf der Instant-Achse. Am Tag der
	 * Zeitumstellung (So 25.10.2026) landete 20:15 sonst auf 19:15.
	 */
	public ZonedDateTime atDate(LocalDate date, ZoneId zone) {
		return date.plusDays(minutes / MINUTES_PER_DAY)
				.atTime(LocalTime.ofSecondOfDay((minutes % MINUTES_PER_DAY) * 60L))
				.atZone(zone);
	}

	@Override
	public int compareTo(TimeOfDay other) {
		return Integer.compare(this.minutes, other.minutes);
	}

	@Override
	public String toString() {
		return "%02d:%02d".formatted(minutes / 60, minutes % 60);
	}

}
