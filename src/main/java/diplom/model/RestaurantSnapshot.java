package diplom.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RestaurantSnapshot {
    private String restaurantName;
    private String restaurantAddress;
    private String restaurantPhoneNumber;
}