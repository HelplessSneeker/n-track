package at.bfnoessler.ntrack.today;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface ItemRepository extends JpaRepository<ItemEntity, Integer> {

	List<ItemEntity> findByDayIdOrderByPositionAsc(Integer dayId);

	/** Der aktuelle Punkt: der erste, der noch nicht erledigt ist. */
	Optional<ItemEntity> findFirstByDayIdAndDoneAtIsNullOrderByPositionAsc(Integer dayId);

	/**
	 * Das letzte Weiter. Über die Position und nicht über das größte {@code done_at}: Bei einer
	 * festen Uhr sind mehrere {@code done_at} gleich, die Position bleibt eindeutig.
	 */
	Optional<ItemEntity> findFirstByDayIdAndDoneAtIsNotNullOrderByPositionDesc(Integer dayId);

}
