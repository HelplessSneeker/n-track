package at.bfnoessler.ntrack.today;

import java.time.OffsetDateTime;

import at.bfnoessler.ntrack.schedule.Status;

/**
 * Ein Punkt mit seinen Zeiten. Die {@code id} ist die Adresse zum Abhaken, damit ein Client sagen
 * kann, welchen Punkt er meint, statt "den aktuellen".
 * <p>
 * {@code OffsetDateTime} und nicht {@code ZonedDateTime}, damit "ISO-8601 mit Offset" am Typ hängt
 * und nicht an einer umschaltbaren Jackson-Option. {@code start} und {@code end} sind leer, wo es
 * keine Uhrzeit gibt.
 */
record ItemResponse(Integer id, String title, OffsetDateTime start, OffsetDateTime end,
		Status status) {
}
