package diplom.model.entity.rider;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Riders")
public class Rider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "firstName is required")
    private String firstName;

    @NotNull(message = "lastName is required")
    private String lastName;

    @NotNull(message = "email is required")
    @Column(unique = true)
    private String email;

    @NotNull(message = "phoneNumber is required")
    @Column(unique = true)
    private String phoneNumber;

    @NotNull(message = "address is required")
    private String address;
    //private BigDecimal latitude;
    //private BigDecimal longitude;

    @Version
    private Long version;

    @NotNull(message = "carType is required")
    @Enumerated(EnumType.STRING)
    private CarType carType;
    /*
    1 - bicycle,
    2 - motorcycle,
    3 - car.
    */
}

/*
Пример запроса с клиента:

POST /api/addOrder HTTP/1.1
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "johndoe@example.com",
  "phoneNumber": "+1234567890",
  "address": "123 Main St, Springfield",
  "carType": "CAR"
}

*/
