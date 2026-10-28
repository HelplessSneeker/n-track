package at.bfnoessler.ntrack.today;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "day")
class DayEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private LocalDate date;

	protected DayEntity() {
	}

	DayEntity(LocalDate date) {
		this.date = date;
	}

	Integer getId() {
		return id;
	}

	LocalDate getDate() {
		return date;
	}

}
