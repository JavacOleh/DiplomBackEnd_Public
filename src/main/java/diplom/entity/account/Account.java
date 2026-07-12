package diplom.entity.account;

import diplom.security.AccountAuthRoles;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "firstName is required")
    private String firstName;

    @NotNull(message = "lastName is required")
    private String lastName;

    @NotNull(message = "email is required")
    @Column(unique = true)
    private String email;

    @NotNull(message = "password is required")
    private String password;

    @NotNull(message = "phoneNumber is required")
    @Column(unique = true)
    private String phoneNumber;

    @NotNull(message = "address is required")
    private String address;

    @Enumerated(EnumType.STRING)
    private AccountAuthRoles role;
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