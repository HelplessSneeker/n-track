package at.bfnoessler.ntrack.today;

/** Ein fester Punkt, der nicht mehr zu halten ist. {@code message} ist der fertige Text. */
record WarningResponse(String title, String fixedAt, long minutesLate, String message) {
}
