package at.bfnoessler.ntrack.today;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/** Der gerechnete Tag, wie er über die API geht. */
record TodayResponse(LocalDate date, OffsetDateTime now, List<ItemResponse> items,
		List<WarningResponse> warnings) {
}
