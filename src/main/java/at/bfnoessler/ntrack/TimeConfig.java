package at.bfnoessler.ntrack;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class TimeConfig {

	/**
	 * Die einzige Quelle für die Zeit. Nie {@code LocalDate.now()} ohne Clock: Der Server läuft in
	 * UTC, und Tests brauchen eine feste Uhr.
	 * <p>
	 * Auf die Minute genau, weil der Tagesplan in Minuten rechnet: Sekundenbruchteile würden sich
	 * über die ganze Kette ziehen und im JSON nur Rauschen erzeugen.
	 */
	@Bean
	Clock clock(@Value("${ntrack.zone}") String zone) {
		return Clock.tick(Clock.system(ZoneId.of(zone)), Duration.ofMinutes(1));
	}

}
