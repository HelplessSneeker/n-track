package at.bfnoessler.ntrack.today;

import java.time.OffsetDateTime;

import at.bfnoessler.ntrack.schedule.Status;

/**
 * Ein Punkt mit seinen Zeiten. {@code OffsetDateTime} und nicht {@code ZonedDateTime}, damit
 * "ISO-8601 mit Offset" am Typ hängt und nicht an einer umschaltbaren Jackson-Option.
 * {@code start} und {@code end} sind leer, wo es keine Uhrzeit gibt.
 */
record ItemResponse(String title, OffsetDateTime start, OffsetDateTime end, Status status) {
}
