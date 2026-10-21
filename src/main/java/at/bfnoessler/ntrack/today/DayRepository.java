package at.bfnoessler.ntrack.today;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface DayRepository extends JpaRepository<DayEntity, Integer> {

	Optional<DayEntity> findByDate(LocalDate date);

}
