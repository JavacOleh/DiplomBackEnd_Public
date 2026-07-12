package diplom.model;

import diplom.entity.order.OrderItem;
import diplom.entity.order.Order;
import diplom.service.GoodService;

import java.util.ArrayList;
import java.util.List;

public record OrderBuildResult(
        List<OrderItem> items,
        double total) {

    public static OrderBuildResult buildOrderItems(Order order, List<CreateOrderRequest.Item> requestItems, GoodService goodService) {

        List<OrderItem> items = new ArrayList<>();
        double total = 0;

        for (var reqItem : requestItems) {

            var good = goodService.getGood(reqItem.goodId);

            if (good != null) {
                if (good.getInStock() != null && good.getInStock() < reqItem.quantity) {
                    return null;
                }

                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setGood(good);
                item.setQuantity(reqItem.quantity);
                item.setPriceAtOrder(good.getPrice());

                items.add(item);

                total += reqItem.quantity * item.getPriceAtOrder();
            } else
                return null;
        }

        return new OrderBuildResult(items, total);
    }

    public static OrderResponse toResponse(Order order) {
        OrderResponse dto = new OrderResponse();
        dto.setId(order.getId());
        dto.setPrice(order.getPrice());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedDate());
        dto.setDeliveredAt(order.getDeliveredDate());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setRestaurantSnapshot(order.getRestaurantSnapshot());
        dto.setDeliveryFlUser(order.getUser().getLastName() + " " + order.getUser().getFirstName());
        dto.setDeliveryNumberUser(order.getUser().getPhoneNumber());
        if (order.getRider() != null)
            dto.setDeliveryFlRider(order.getRider().getLastName() + " " + order.getRider().getFirstName());

        dto.setItems(order.getItems().stream()
                .map(i -> {
                    OrderResponse.Item item = new OrderResponse.Item();
                    item.goodId = i.getGood().getId();
                    item.goodName = i.getGood().getCaption();
                    item.quantity = i.getQuantity();
                    item.priceAtOrder = i.getPriceAtOrder();
                    return item;
                })
                .toList());

        return dto;
    }
}
