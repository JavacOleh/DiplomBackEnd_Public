package diplom.model;

import diplom.entity.account.Account;
import diplom.security.AccountAuthRoles;
import lombok.Data;

import java.util.UUID;

@Data
public class UserDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private String token;
    private AccountAuthRoles role;

    public UserDTO(Account account, String token) {
        this.id = account.getId();
        this.firstName = account.getFirstName();
        this.lastName = account.getLastName();
        this.email = account.getEmail();
        this.phoneNumber = account.getPhoneNumber();
        this.address = account.getAddress();
        this.token = token;
        this.role = account.getRole();
    }
}
