package at.bfnoessler.ntrack.today;

import java.io.IOException;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import at.bfnoessler.ntrack.schedule.Item;
import at.bfnoessler.ntrack.schedule.TimeOfDay;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Die Tagesvorlagen aus {@code vorlagen.yaml}, beim Start eingelesen.
 * <p>
 * Bewusst direkt mit SnakeYAML und nicht über {@code @ConfigurationProperties}: So bleibt die Datei
 * genau die handgepflegte Form, und ein Vorlagenname wie {@code werktag_morgen} wird nie zu einem
 * Property-Namen, in dem Springs Relaxed Binding den Unterstrich verschluckt.
 */
@Component
class Templates {

	private static final Map<DayOfWeek, List<String>> BY_WEEKDAY = Map.of(
			DayOfWeek.MONDAY, List.of("werktag_morgen", "gym"),
			DayOfWeek.TUESDAY, List.of("werktag_morgen", "laufen"),
			DayOfWeek.WEDNESDAY, List.of("werktag_morgen", "gym"),
			DayOfWeek.THURSDAY, List.of("werktag_morgen", "laufen"),
			DayOfWeek.FRIDAY, List.of("werktag_morgen", "gym"),
			DayOfWeek.SATURDAY, List.of("samstag"),
			DayOfWeek.SUNDAY, List.of("sonntag"));

	private final Map<String, List<Item>> byName;

	Templates() {
		this(new ClassPathResource("vorlagen.yaml"));
	}

	Templates(Resource resource) {
		this.byName = read(resource);
		// Ein Tippfehler in der Zuordnung soll beim Start auffallen, nicht an dem Wochentag.
		BY_WEEKDAY.values().stream().flatMap(List::stream).distinct().forEach(this::template);
	}

	/** Die Punkte, mit denen ein Tag dieses Wochentags anfängt, in Reihenfolge. */
	List<Item> forDay(DayOfWeek weekday) {
		return BY_WEEKDAY.get(weekday).stream().flatMap(name -> template(name).stream()).toList();
	}

	private List<Item> template(String name) {
		List<Item> template = byName.get(name);
		if (template == null) {
			throw new IllegalStateException("vorlagen.yaml kennt keine Vorlage " + name);
		}
		return template;
	}

	private static Map<String, List<Item>> read(Resource resource) {
		Map<String, List<Map<String, Object>>> raw;
		try (InputStream stream = resource.getInputStream()) {
			raw = new Yaml(new SafeConstructor(new LoaderOptions())).load(stream);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Vorlagen nicht lesbar: " + resource, ex);
		}
		if (raw == null || raw.isEmpty()) {
			throw new IllegalStateException("Vorlagen sind leer: " + resource);
		}

		Map<String, List<Item>> templates = new LinkedHashMap<>();
		raw.forEach((name, entries) -> templates.put(name, entries.stream()
				.map(entry -> toItem(name, entry))
				.toList()));
		return Map.copyOf(templates);
	}

	private static Item toItem(String template, Map<String, Object> entry) {
		Object title = entry.get("titel");
		if (!(title instanceof String text) || text.isBlank()) {
			throw new IllegalStateException("Punkt ohne titel in Vorlage " + template + ": " + entry);
		}
		Object duration = entry.get("dauer");
		if (duration != null && !(duration instanceof Integer)) {
			throw new IllegalStateException("dauer ist keine Minutenzahl in " + template + ": " + entry);
		}
		Object fixedAt = entry.get("um");
		return new Item(text, (Integer) duration,
				fixedAt == null ? null : TimeOfDay.of(fixedAt.toString()), null);
	}

}
