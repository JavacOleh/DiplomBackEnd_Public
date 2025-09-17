package diplom.service;

import diplom.model.entity.food.Food;
import diplom.model.entity.order.Order;
import diplom.model.entity.order.OrderStatus;
import diplom.repository.FoodRepository;
import diplom.repository.OrderRepository;
import diplom.repository.RiderRepository;
import diplom.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class OrderService {

    public final OrderRepository orderRepository;
    public final RiderRepository riderRepository;
    public final UserRepository userRepository;
    public final FoodRepository foodRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, RiderRepository riderRepository, UserRepository userRepository, FoodRepository foodRepository) {
        this.orderRepository = orderRepository;
        this.riderRepository = riderRepository;
        this.userRepository = userRepository;
        this.foodRepository = foodRepository;
    }

    public boolean updateOrderDeliveredDate(UUID uniqueKey, String deliveredDate) {
        Optional<Order> order = orderRepository.findById(uniqueKey);
        if (order.isPresent()) {
            Order existingOrder = order.get();
            existingOrder.setDeliveredDate(deliveredDate);  // Обновляем только необходимое поле
            orderRepository.save(existingOrder);  // Сохраняем обновленную сущность
            return true;
        }
        return false;
    }

    public boolean updateOrderStatus(UUID uniqueKey, OrderStatus status) {
        if (OrderStatus.isValidStatus(status)) {
            Optional<Order> order = orderRepository.findById(uniqueKey);
            if (order.isPresent()) {
                Order existingOrder = order.get();
                existingOrder.setStatus(status);  // Обновляем только статус
                orderRepository.save(existingOrder);  // Сохраняем обновленную сущность
                return true;
            }
            return false;
        }
        return false;
    }

    public boolean addOrder(Order order) {
        var user = userRepository.findById(order.getUser().getId());
        var rider = riderRepository.findById(order.getRider().getId());
        var check = user.isPresent() && rider.isPresent();

        if (check) {
            order.setUser(user.get());
            order.setRider(rider.get());

            // Обработка списка food
            List<Food> foods = order.getFoods().stream()
                    .map(food -> foodRepository.findById(food.getId()).orElse(null))
                    .filter(food -> food != null)
                    .collect(Collectors.toList());
            order.setFoods(foods);

            AtomicReference<Double> sum = new AtomicReference<>(0.0);

            order.getFoods().forEach(v -> {
                sum.updateAndGet(v1 -> Double.sum(v1, v.getPrice()));
            });

            order.setPrice(sum.get());

            System.out.println(foods);

            orderRepository.save(order);
        }
        return check;
    }


    public boolean deleteOrder(UUID uuid) {
        var order = orderRepository.findById(uuid);
        var check = order.isPresent();

        if (check) {
            orderRepository.deleteById(uuid);
        }
        return check;
    }

    public Order getOrder(UUID uuid) {
        var order = orderRepository.findById(uuid);
        var check = order.isPresent();

        return check ? order.get() : null;
    }
}
