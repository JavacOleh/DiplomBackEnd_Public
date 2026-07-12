package diplom.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class RestaurantContacts {
    @Id
    private Long id = 1L;
    @Embedded
    private RestaurantSnapshot snapshot;
}
