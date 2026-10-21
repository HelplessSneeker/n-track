package at.bfnoessler.ntrack.schedule;

/** Ein fester Punkt, der nicht mehr zu halten ist, weil die Kette davor länger läuft. */
public record Warning(String title, TimeOfDay fixedAt, long minutesLate) {

	public String message() {
		return "%s %s: %d min zu spät".formatted(title, fixedAt, minutesLate);
	}

}
