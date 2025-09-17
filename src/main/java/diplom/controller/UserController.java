package diplom.controller;

import diplom.model.entity.order.OrderStatus;
import diplom.model.entity.user.User;
import diplom.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public List<User> getUsers() {
        return userService.userRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable long id) {
        var user = userService.getUser(id);

        return user == null
                ? ResponseEntity.badRequest().build()
                : ResponseEntity.ok(user);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addUser(@Valid @RequestBody User user, BindingResult result) {

        if (result.hasErrors()) {
            List<String> errorMessages = result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();

            return ResponseEntity.badRequest().body(errorMessages.toString());
        }

        var updated = userService.addUser(user);

        return updated
                ? ResponseEntity.ok().body("Ok")
                : ResponseEntity.badRequest().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateUser(@RequestBody User user) {
        var updated = userService.updateUserData(user);

        return updated
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable long id) {
        var user = userService.getUser(id);

        if(user != null) {
            userService.deleteUser(id);
        }

        return user == null
                ? ResponseEntity.badRequest().build()
                : ResponseEntity.ok().build();
    }
}
