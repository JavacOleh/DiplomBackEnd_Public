package diplom.controller;

import diplom.model.entity.food.Food;
import diplom.service.FoodService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/foods")
public class FoodController {
    private final FoodService foodService;

    @Autowired
    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping
    public List<Food> getAllFoods() {
        return foodService.foodRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Food> getOrder(@PathVariable Long id) {
        var order = foodService.getFood(id);

        return order == null
                ? ResponseEntity.badRequest().build()
                : ResponseEntity.ok(order);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addFood(@Valid @RequestBody Food food, BindingResult result) {

        if (result.hasErrors()) {
            List<String> errorMessages = result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();

            return ResponseEntity.badRequest().body(errorMessages.toString());
        }

        var updated = foodService.addFood(food);

        return updated
                ? ResponseEntity.ok().body("Ok")
                : ResponseEntity.badRequest().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateFood(@RequestBody Food food) {
        var updated = foodService.updateFood(food);

        return updated
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFood(@PathVariable long id) {
        var food = foodService.getFood(id);
        boolean update = food != null;

        if(update) {
            update = foodService.deleteFood(id);
        }

        return update
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }
}
