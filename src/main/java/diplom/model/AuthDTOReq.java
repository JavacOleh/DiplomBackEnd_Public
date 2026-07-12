package diplom.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuthDTOReq {
    @NotEmpty
    String email;

    @NotNull
    String password;
}
