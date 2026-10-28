package at.bfnoessler.ntrack.schedule;

import java.time.ZonedDateTime;

/**
 * Ein Punkt mit seinen gerechneten Zeiten. {@code start} fehlt beim ersten erledigten Punkt und bei
 * Punkten, die hinter einem offenen Punkt liegen; {@code end} fehlt beim offenen Punkt selbst.
 */
public record ScheduledItem(Item item, ZonedDateTime start, ZonedDateTime end, Status status) {
}
