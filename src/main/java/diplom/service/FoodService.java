package diplom.service;

import diplom.model.entity.food.Food;
import diplom.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FoodService {
    public FoodRepository foodRepository;

    @Autowired
    public FoodService(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    public Food getFood(Long id) {
        var food = foodRepository.findById(id);
        var check = food.isPresent();

        return check ? food.get() : null;
    }

    public boolean addFood(Food food) {
        boolean update = food != null;

        if (update) {
            foodRepository.save(food);
        }

        return update;
    }

    public boolean deleteFood(Long id) {
        var food = foodRepository.findById(id);
        boolean update = food.isPresent();

        if (update) {
            foodRepository.deleteById(id);
        }

        return update;
    }

    public boolean updateFood(Food food) {
        var updated = food != null;

        if (updated) {
            var temp = foodRepository.findById(food.getId());

            if (temp.isPresent()) {
                Food existingFood = temp.get();
                existingFood.setCaption(food.getCaption());
                existingFood.setPrice(food.getPrice());
                existingFood.setDescription(food.getDescription());
                existingFood.setImageFileName(food.getImageFileName());
                foodRepository.save(existingFood);
            }
        }

        return updated;
    }

}
