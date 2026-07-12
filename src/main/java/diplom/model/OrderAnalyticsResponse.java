package diplom.model;

import diplom.entity.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderAnalyticsResponse {
    private LocalDateTime createdDate;
    private LocalDateTime deliveredDate;
    private OrderStatus status;
    private long count;
    private double totalRevenue;
}