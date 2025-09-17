package diplom.controller;


import diplom.model.entity.order.Order;
import diplom.model.entity.order.OrderStatus;
import diplom.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping()
    public List<Order> getOrders() {
        return orderService.orderRepository.findAll();
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<Order> getOrder(@PathVariable UUID uuid) {
        var order = orderService.getOrder(uuid);

        return order == null
                ? ResponseEntity.badRequest().build()
                : ResponseEntity.ok(order);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addOrder(@Valid @RequestBody Order order, BindingResult result) {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();

            return ResponseEntity.badRequest().body(errorMessages.toString());
        }

        var updated = orderService.addOrder(order);

        return updated
                ? ResponseEntity.ok().body("Ok")
                : ResponseEntity.badRequest().build();
    }

    @PutMapping("/update/deliveredDate/{uniqueKey}")
    public ResponseEntity<Void> updateOrderDeliveredDate(@PathVariable UUID uniqueKey, @RequestParam String deliveredDate) {
        var updated = orderService.updateOrderDeliveredDate(uniqueKey, deliveredDate);

        return updated
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    @PutMapping("/update/status/{uniqueKey}")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable UUID uniqueKey, @RequestParam OrderStatus status) {
        var updated = orderService.updateOrderStatus(uniqueKey, status);

        return updated
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{uniqueKey}")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID uniqueKey) {
        var updated = orderService.deleteOrder(uniqueKey);

        return updated
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }
}
