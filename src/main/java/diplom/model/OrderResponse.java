package diplom.model;

import diplom.entity.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private UUID id;
    private double price;
    private OrderStatus status;
    private List<Item> items;
    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;
    private String deliveryAddress;
    private String deliveryFlUser;
    private String deliveryFlRider;
    private String deliveryNumberUser;
    private RestaurantSnapshot restaurantSnapshot;

    public static class Item {
        public Long goodId;
        public String goodName;
        public int quantity;
        public double priceAtOrder;
    }
}