package diplom.service;

import diplom.entity.good.Good;
import diplom.repository.GoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoodService {
    public GoodRepository goodRepository;

    @Autowired
    public GoodService(GoodRepository goodRepository) {
        this.goodRepository = goodRepository;
    }

    public Good getGood(Long id) {
        var food = goodRepository.findById(id);
        var check = food.isPresent();

        return check ? food.get() : null;
    }

    public boolean addFood(Good good) {
        boolean update = good != null;

        if (update) {
            goodRepository.save(good);
        }

        return update;
    }

    public boolean deleteFood(Long id) {
        var food = goodRepository.findById(id);
        boolean update = food.isPresent();

        if (update) {
            goodRepository.deleteById(id);
        }

        return update;
    }

    public boolean updateFood(Good good) {
        var updated = good != null;

        if (updated) {
            var temp = goodRepository.findById(good.getId());

            if (temp.isPresent()) {
                Good existingGood = temp.get();
                existingGood.setCaption(good.getCaption());
                existingGood.setPrice(good.getPrice());
                existingGood.setDescription(good.getDescription());
                existingGood.setImageFileName(good.getImageFileName());
                goodRepository.save(existingGood);
            }
        }

        return updated;
    }

}
