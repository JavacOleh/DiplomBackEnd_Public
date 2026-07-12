package diplom.entity.order;

import diplom.entity.account.Account;
import diplom.model.RestaurantSnapshot;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @NotNull(message = "User is required")
    private Account user; // Тут важно проверять роль, чтобы это был именно USER

    @ManyToOne
    private Account rider; // Тут важно проверять роль, чтобы это был именно RIDER

    @ManyToOne
    private Account manager; // Тут важно проверять роль, чтобы это был именно MANAGER

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    @NotNull(message = "Price is required")
    private double price;

    @NotNull(message = "CreatedDate is required")
    private LocalDateTime createdDate;

    private LocalDateTime deliveredDate;

    @Embedded
    private RestaurantSnapshot restaurantSnapshot;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private String deliveryAddress;

    /*
    CREATED(0),
    RECEIVED(1),
    RECEIVED_FOR_DELIVERY(2),
    ACCEPTED_FOR_DELIVERY(3),
    DELIVERED(4),
    DECLINED(5);
    */
}
