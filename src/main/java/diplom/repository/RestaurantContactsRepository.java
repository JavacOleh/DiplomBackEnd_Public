package diplom.repository;

import diplom.model.RestaurantContacts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantContactsRepository extends JpaRepository<RestaurantContacts, Long> { }
