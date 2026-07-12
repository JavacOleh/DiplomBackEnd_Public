package diplom.controller;

import diplom.model.RestaurantContacts;
import diplom.repository.RestaurantContactsRepository;
import diplom.security.AccountAuthRoles;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contacts")
@Tag(name = "Contacts", description = "Операції з контактами ресторану")
@SecurityRequirement(name = "bearerAuth")
public class ContactsController {
    @Autowired
    RestaurantContactsRepository restaurantContactsRepository;

    @Operation(
            summary = "Отримати контакти ресторану",
            description = "Повертає контактну інформацію ресторану з id 1, або null, якщо не знайдено",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = RestaurantContacts.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Contacts not found")
            }
    )
    @GetMapping("")
    public RestaurantContacts getContacts() {
        var found = restaurantContactsRepository.findById(1L);

        return found.orElse(null);
    }

    @Operation(
            summary = "Встановити контакти ресторану",
            description = "Оновлює контактну інформацію ресторану. Потрібна роль MANAGER.",
            requestBody = @RequestBody(
                    description = "Об'єкт RestaurantContacts для встановлення",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RestaurantContacts.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "Wrong role or body"),
            }
    )
    @PostMapping("")
    public ResponseEntity<String> setContacts(@RequestBody RestaurantContacts restaurantContacts, HttpServletRequest request) {
        var responseEntity = ResponseEntity.badRequest().body("Wrong role");
        var roleFromToken = AccountAuthRoles.getByStr((String) request.getAttribute("role"));

        return switch (roleFromToken) {
            case MANAGER -> {
                restaurantContactsRepository.deleteById(1L);
                restaurantContacts.setId(1L);
                restaurantContactsRepository.save(restaurantContacts);

                yield ResponseEntity.ok("Ok");
            }
            case null, default -> responseEntity;
        };
    }
}
