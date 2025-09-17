package diplom.model.entity.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Users")
public class User {
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

    private String password;

    @NotNull(message = "phoneNumber is required")
    @Column(unique = true)
    private String phoneNumber;

    @NotNull(message = "address is required")
    private String address;

    @Version
    private Long version;
}
/*
Пример запроса с клиента:

POST /api/addOrder HTTP/1.1
Content-Type: application/json

{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+1234567890",
    "address": "123 Main St, Springfield, IL"
}

*/