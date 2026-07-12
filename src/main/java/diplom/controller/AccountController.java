package diplom.controller;

import diplom.entity.account.Account;
import diplom.entity.order.Order;
import diplom.security.AccountAuthRoles;
import diplom.model.AuthDTOReq;
import diplom.model.UserDTO;
import diplom.service.JwtTokenService;
import diplom.service.AccountService;
import diplom.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Users", description = "Операції з користувачами")
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;
    private final JwtTokenService jwtTokenService;
    private final OrderService orderService;

    @Autowired
    public AccountController(AccountService accountService,
                             JwtTokenService jwtTokenService,
                             OrderService orderService) {
        this.orderService = orderService;
        this.accountService = accountService;
        this.jwtTokenService = jwtTokenService;
    }

    @Operation(summary = "Авторизація")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid Authorization header")
    })
    @PostMapping("/auth")
    public ResponseEntity<?> onAuth(@Valid @RequestBody AuthDTOReq authDTOReq) {
        var exist = accountService.accountRepository.findUserByEmail(authDTOReq.getEmail());

        return (exist.isPresent()
                && exist.get().getPassword().equals(authDTOReq.getPassword())
                && exist.get().getEmail().equals(authDTOReq.getEmail())
                ? ResponseEntity.ok(new UserDTO(exist.get(), jwtTokenService.generateToken(exist.get().getEmail())))
                : ResponseEntity.status(401).build());
    }

    @Operation(summary = "Реєстрація")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid Authorization header")
    })
    @PostMapping("/add")
    public ResponseEntity<String> addUser(@Valid @RequestBody Account account, BindingResult result) {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();

            return ResponseEntity.badRequest().body(errorMessages.toString());
        }

        account.setRole(AccountAuthRoles.USER);
        var updated = accountService.addUser(account);

        return updated
                ? ResponseEntity.ok().body("Ok")
                : ResponseEntity.badRequest().build();
    }

    @Operation(summary = "Оновити інформацію про користувача")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid Authorization header"),
            @ApiResponse(responseCode = "403", description = "Attempt to update other user or wrong token")
    })
    @PutMapping("/update")
    public ResponseEntity<Void> updateUser(HttpServletRequest request, @RequestBody Account account) {
        String emailFromToken = (String) request.getAttribute("email");

        if (emailFromToken == null || !emailFromToken.equals(account.getEmail())) {
            return ResponseEntity.status(403).build();
        }

        var updated = accountService.updateUserData(account);

        return updated
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    @Operation(summary = "Видалити користувача")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid Authorization header")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id, HttpServletRequest request) {
        var account = accountService.getUser(id);
        String emailFromToken = (String) request.getAttribute("email");
        if (account == null || emailFromToken == null || !emailFromToken.equals(account.getEmail()))
            return ResponseEntity.status(403).build();

        orderService.deleteOrDetachAccountWithOrders(account);

        accountService.deleteUser(id);

        return ResponseEntity.ok().build();
    }
}
