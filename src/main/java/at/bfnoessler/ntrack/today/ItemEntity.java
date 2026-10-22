package at.bfnoessler.ntrack.today;

import java.time.OffsetDateTime;

import at.bfnoessler.ntrack.schedule.Item;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Eine Zeile der Tabelle {@code item}. Bewusst ohne {@code @ManyToOne}: Die flache
 * {@code day_id}-Spalte reicht, und sortiert wird in der Abfrage.
 */
@Entity
@Table(name = "item")
class ItemEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private Integer dayId;

	private Integer position;

	private String title;

	private Integer duration;

	private Integer fixedAt;

	private OffsetDateTime doneAt;

	protected ItemEntity() {
	}

	static ItemEntity fromTemplate(Integer dayId, int position, Item item) {
		ItemEntity entity = new ItemEntity();
		entity.dayId = dayId;
		entity.position = position;
		entity.title = item.title();
		entity.duration = item.duration();
		entity.fixedAt = item.fixedAt() == null ? null : item.fixedAt().minutes();
		return entity;
	}

	Integer getId() {
		return id;
	}

	Integer getDayId() {
		return dayId;
	}

	String getTitle() {
		return title;
	}

	Integer getDuration() {
		return duration;
	}

	Integer getFixedAt() {
		return fixedAt;
	}

	OffsetDateTime getDoneAt() {
		return doneAt;
	}

	void setDoneAt(OffsetDateTime doneAt) {
		this.doneAt = doneAt;
	}

}
