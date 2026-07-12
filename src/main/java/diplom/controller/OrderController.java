package diplom.controller;


import diplom.entity.order.Order;
import diplom.entity.order.OrderStatus;
import diplom.model.*;
import diplom.repository.RestaurantContactsRepository;
import diplom.security.AccountAuthRoles;
import diplom.service.AccountService;
import diplom.service.GoodService;
import diplom.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Операції з замовленнями")
public class OrderController {
    private final OrderService orderService;
    private final AccountService accountService;
    private final GoodService goodService;
    private final RestaurantContactsRepository contactsRepository;

    @Autowired
    public OrderController(OrderService orderService,
                           AccountService accountService,
                           RestaurantContactsRepository contactsRepository,
                           GoodService goodService) {
        this.orderService = orderService;
        this.accountService = accountService;
        this.contactsRepository = contactsRepository;
        this.goodService = goodService;
    }

    @Operation(
            summary = "Отримати доступний діапазон аналітики",
            description = "Повертає мінімальну та максимальну дати, доступні для аналітики замовлень."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(schema = @Schema(implementation = AnalyticsRangeResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid time zone", content = @Content)
    })
    @GetMapping("/analytics/range")
    public AnalyticsRangeResponse getRange(
            @RequestHeader("Time-Zone")
            String timezone
    ) {
        return orderService.getRange(timezone);
    }

    @Operation(
            summary = "Отримати аналітику замовлень",
            description = "Повертає сторінку аналітичних даних за вказаний період, статусами та часовим поясом."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content)
    })
    @GetMapping("/analytics")
    public PageResponse<OrderAnalyticsResponse> analytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam List<OrderStatus> statuses,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestHeader("Time-Zone") String timezone
    ) {
        from = from.truncatedTo(ChronoUnit.SECONDS);
        to = to.truncatedTo(ChronoUnit.SECONDS);

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 100));

        Page<OrderAnalyticsResponse> result = orderService.getAnalytics(
                statuses,
                from,
                to,
                timezone,
                PageRequest.of(safePage, safeSize)
        );

        return PageResponse.from(result);
    }

    @Operation(
            summary = "Отримати статус замовлення",
            description = "Повертає поточний статус замовлення за його унікальним ідентифікатором."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(schema = @Schema(implementation = OrderStatus.class))
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @GetMapping("/status/{id}")
    public OrderStatus getStatus(@PathVariable UUID id,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        var roleFromToken = AccountAuthRoles.getByStr((String) request.getAttribute("role"));
        var emailFromToken = (String) request.getAttribute("email");
        response.setStatus(200);

        if (roleFromToken != null && emailFromToken != null) {
            var foundOrder = orderService.getOrder(id);

            if (foundOrder != null)
                return foundOrder.getStatus();
        }

        response.setStatus(403);
        return null;
    }

    @Operation(
            summary = "Отримати активні замовлення",
            description = "Повертає список активних замовлень, доступних поточному менеджеру або кур’єру."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class)))
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @GetMapping()
    public List<OrderResponse> getOrders(HttpServletRequest request, HttpServletResponse response) {
        var roleFromToken = AccountAuthRoles.getByStr((String) request.getAttribute("role"));
        var emailFromToken = (String) request.getAttribute("email");
        response.setStatus(200);

        if (emailFromToken == null || roleFromToken == null || roleFromToken == AccountAuthRoles.USER) {
            response.setStatus(403);
            return List.of();
        }

        var found = accountService.accountRepository.findUserByEmail(emailFromToken);

        if (found.isEmpty()) {
            response.setStatus(404);
            return List.of();
        }

        List<Order> orders = switch (roleFromToken) {
            case MANAGER -> orderService.getByStatuses(List.of(OrderStatus.CREATED, OrderStatus.RECEIVED));
            case RIDER ->
                    orderService.getByStatuses(List.of(OrderStatus.RECEIVED_FOR_DELIVERY, OrderStatus.ACCEPTED_FOR_DELIVERY));
            default -> List.of();
        };

        var emptyGoodsInOrders = orders
                .stream()
                .filter(order -> order.getItems() == null || order.getItems().isEmpty())
                .toList();
        orderService.deleteOrders(emptyGoodsInOrders);

        return orders.stream()
                .filter(order -> order.getItems() != null && !order.getItems().isEmpty())
                .filter(order -> switch (roleFromToken) {
                    case RIDER -> order.getRider() == null || order.getRider().getEmail().equals(emailFromToken);
                    case MANAGER -> order.getManager() == null || order.getManager().getEmail().equals(emailFromToken);
                    default -> true;
                })
                .map(OrderBuildResult::toResponse)
                .sorted(Comparator.comparing(OrderResponse::getCreatedAt).reversed())
                .toList();
    }

    @Operation(
            summary = "Отримати історію замовлень",
            description = "Повертає історію замовлень поточного користувача відповідно до його ролі."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class)))
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @GetMapping("/history")
    public List<OrderResponse> getHistory(HttpServletRequest request, HttpServletResponse response) {
        var roleFromToken = AccountAuthRoles.getByStr((String) request.getAttribute("role"));
        var emailFromToken = (String) request.getAttribute("email");

        if (emailFromToken == null || roleFromToken == null) {
            response.setStatus(403);
            return List.of();
        }

        var found = accountService.accountRepository.findUserByEmail(emailFromToken);

        if (found.isEmpty()) {
            response.setStatus(404);
            return List.of();
        }

        var account = found.get();

        List<Order> orders = switch (roleFromToken) {
            case MANAGER -> orderService.getByManagerId(account.getId());
            case USER -> orderService.getByUserId(account.getId());
            case RIDER -> orderService.getByRiderId(account.getId());
        };

        var emptyGoodsInOrders = orders
                .stream()
                .filter(order -> order.getItems() == null || order.getItems().isEmpty())
                .toList();
        orderService.deleteOrders(emptyGoodsInOrders);

        return orders.stream()
                .filter(order -> order.getItems() != null && !order.getItems().isEmpty())
                .map(OrderBuildResult::toResponse)
                .sorted(Comparator.comparing(OrderResponse::getCreatedAt).reversed())
                .toList();
    }

    @Operation(
            summary = "Створити замовлення",
            description = "Створює нове замовлення для авторизованого користувача та оновлює залишки товарів."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content),
            @ApiResponse(responseCode = "422", description = "Invalid order data or insufficient stock", content = @Content)
    })
    @PostMapping("/add")
    public OrderResponse addOrder(@RequestBody CreateOrderRequest request,
                                  HttpServletRequest httpRequest,
                                  HttpServletResponse httpServletResponse) {
        var email = (String) httpRequest.getAttribute("email");
        var roleFromToken = AccountAuthRoles.getByStr((String) httpRequest.getAttribute("role"));
        httpServletResponse.setStatus(404);
        var user = accountService.accountRepository.findUserByEmail(email)
                .orElse(null);

        if (user != null && roleFromToken != null) {
            if (roleFromToken.equals(AccountAuthRoles.USER)) {
                Order order = new Order();
                order.setUser(user);
                order.setStatus(OrderStatus.CREATED);
                order.setCreatedDate(LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MINUTES));
                var contacts = contactsRepository.findById(1L);
                contacts.ifPresent(restaurantContacts -> order.setRestaurantSnapshot(restaurantContacts.getSnapshot()));
                order.setDeliveryAddress(user.getAddress());


                var result = OrderBuildResult.buildOrderItems(order, request.items, goodService);

                if (result != null) {
                    order.setItems(result.items());
                    var price = BigDecimal.valueOf(result.total())
                            .setScale(2, RoundingMode.HALF_UP);

                    order.setPrice(price.doubleValue());

                    var check = order.getItems().stream().anyMatch(v -> {
                        var a = v.getGood();
                        return a.getInStock() - v.getQuantity() < 0;
                    });
                    if (!check) {
                        var saved = orderService.save(order);

                        updateInStockGoods(order);

                        httpServletResponse.setStatus(200);
                        return OrderBuildResult.toResponse(saved);
                    } else
                        httpServletResponse.setStatus(422);
                } else
                    httpServletResponse.setStatus(422);
            }
        }

        return null;
    }

    @Operation(
            summary = "Оновити статус замовлення",
            description = "Змінює статус замовлення відповідно до ролі поточного користувача та дозволеного порядку переходів."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(responseCode = "400", description = "Order not found or invalid account credentials", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "409", description = "Invalid status transition", content = @Content),
            @ApiResponse(responseCode = "412", description = "Order has already been accepted by another employee", content = @Content),
            @ApiResponse(responseCode = "422", description = "Unprocessable status update", content = @Content)
    })
    @PutMapping("/update/status/{uniqueKey}")
    public ResponseEntity<String> updateOrderStatus(@PathVariable UUID uniqueKey, @RequestParam OrderStatus status, HttpServletRequest request) {
        var roleFromToken = AccountAuthRoles.getByStr((String) request.getAttribute("role"));
        var emailFromToken = (String) request.getAttribute("email");
        var responseEntity = ResponseEntity.badRequest().body("Order not found or wrong account credits");
        var foundOrder = orderService.getOrder(uniqueKey);
        var foundAccountOptional = accountService.accountRepository.findUserByEmail(emailFromToken);

        if (foundOrder != null && roleFromToken != null && emailFromToken != null && foundAccountOptional.isPresent()) {
            var foundAccount = foundAccountOptional.get();
            var foundOrderStatus = foundOrder.getStatus();
            var check = !roleFromToken.equals(AccountAuthRoles.USER)
                    && foundOrderStatus.getCode() < status.getCode();

            if (foundOrderStatus == OrderStatus.CREATED && roleFromToken.equals(AccountAuthRoles.USER) && status == OrderStatus.DECLINED) {
                foundOrder.getItems().forEach(orderItem -> {
                    var good = orderItem.getGood();

                    if (good != null) {
                        good.setInStock(good.getInStock() + orderItem.getQuantity());

                        goodService.updateFood(good);
                    }
                });
                orderService.updateOrderStatus(uniqueKey, status);
                return ResponseEntity.ok("Ok");
            }

            if (check) {
                responseEntity = switch (status) {
                    case CREATED, RECEIVED, RECEIVED_FOR_DELIVERY -> {
                        if (roleFromToken == AccountAuthRoles.MANAGER) {
                            if (foundOrder.getManager() == null) {
                                foundOrder.setStatus(status);
                                foundOrder.setManager(foundAccount);
                                orderService.save(foundOrder);
                            } else {
                                if (emailFromToken.equals(foundOrder.getManager().getEmail()))
                                    orderService.updateOrderStatus(uniqueKey, status);
                                else
                                    yield ResponseEntity.status(412).body("This order has been accepted by other manager.");
                            }
                            yield ResponseEntity.ok("Ok");
                        } else
                            yield ResponseEntity.status(403).body("Wrong role");
                    }

                    //Может на стороне клиента сделать проверку на то рядом ли курьер возле клиента и если да то ток тогда можно DELIVERED
                    case ACCEPTED_FOR_DELIVERY, DELIVERED -> {
                        if (roleFromToken == AccountAuthRoles.RIDER) {
                            if (status.equals(OrderStatus.DELIVERED))
                                orderService.updateOrderDeliveredDate(uniqueKey, LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MINUTES));

                            if (foundOrder.getRider() == null) {
                                foundOrder.setStatus(status);
                                foundOrder.setRider(foundAccount);
                                orderService.save(foundOrder);
                            } else {
                                if (emailFromToken.equals(foundOrder.getRider().getEmail()))
                                    orderService.updateOrderStatus(uniqueKey, status);
                                else
                                    yield ResponseEntity.status(412).body("This order has been accepted by other rider.");
                            }
                            yield ResponseEntity.ok("Ok");
                        } else
                            yield ResponseEntity.status(403).body("Wrong role");
                    }

                    case null, default ->
                            ResponseEntity.status(422).body("Something went wrong, trying to set DECLINED status with wrong role or when status isn't created?");
                };
            } else
                responseEntity = ResponseEntity.status(409).body("You can't set prev status");
        }

        return responseEntity;
    }

    private void updateInStockGoods(Order order) {
        order.getItems().forEach(orderItem -> {
            var id = orderItem.getGood().getId();

            var foundGood = goodService.getGood(id);

            if (foundGood != null) {
                foundGood.setInStock(Math.max(0, foundGood.getInStock() - orderItem.getQuantity()));
                goodService.updateFood(foundGood);
            }
        });
    }
}
