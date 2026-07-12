package diplom.service;

import diplom.entity.account.Account;
import diplom.entity.order.Order;
import diplom.entity.order.OrderStatus;
import diplom.model.AnalyticsRangeResponse;
import diplom.model.OrderAnalyticsResponse;
import diplom.repository.GoodRepository;
import diplom.repository.OrderItemRepository;
import diplom.repository.OrderRepository;
import diplom.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    public final OrderRepository orderRepository;
    public final AccountRepository accountRepository;
    public final GoodRepository goodRepository;
    public final OrderItemRepository orderItemRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        AccountRepository accountRepository,
                        GoodRepository goodRepository,
                        OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.accountRepository = accountRepository;
        this.goodRepository = goodRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public List<Order> getByStatuses(List<OrderStatus> statuses) {
        return orderRepository.findByStatusIn(statuses);
    }

    public List<Order> getByManagerId(UUID id) {
        return orderRepository.findByManager_Id(id);
    }

    public List<Order> getByRiderId(UUID id) {
        return orderRepository.findByRider_Id(id);
    }

    public List<Order> getByUserId(UUID id) {
        return orderRepository.findByUser_Id(id);
    }

    @Transactional
    public boolean deleteFood(Long goodId) {

        var items = orderItemRepository.findByGood_Id(goodId);

        for (var item : items) {
            Order order = item.getOrder();

            if (order != null) {
                order.getItems().remove(item);
            }
        }

        goodRepository.deleteById(goodId);

        return true;
    }

    public boolean updateOrderDeliveredDate(UUID uniqueKey, LocalDateTime deliveredDate) {
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

    @Transactional
    public void deleteOrDetachAccountWithOrders(Account account) {

        switch (account.getRole()) {
            case MANAGER -> orderRepository.detachManager(account.getId());

            case RIDER -> orderRepository.detachRider(account.getId());

            case USER -> orderRepository.deleteByUserId(account.getId());
        }

        accountRepository.delete(account);
    }

    public Page<OrderAnalyticsResponse> getAnalytics(
            List<OrderStatus> statuses,
            LocalDateTime from,
            LocalDateTime to,
            String userZone,
            Pageable pageable
    ) {
        List<Order> orders = orderRepository.getAnalytics(statuses, from, to);

        List<OrderAnalyticsResponse> analytics = orders.stream()

                .collect(Collectors.groupingBy(
                        o -> o.getCreatedDate()

                                // считаем что DB хранит UTC
                                .atOffset(ZoneOffset.UTC)

                                // переводим в timezone пользователя
                                .atZoneSameInstant(ZoneId.of(userZone))

                                // локальный календарный день
                                .toLocalDate()

                                .atStartOfDay(),

                        Collectors.groupingBy(Order::getStatus)
                ))

                .entrySet()

                .stream()

                .flatMap(dateEntry -> {

                    LocalDateTime createdDate = dateEntry.getKey();

                    return dateEntry.getValue()
                            .entrySet()
                            .stream()
                            .map(statusEntry -> {

                                OrderStatus status = statusEntry.getKey();

                                List<Order> groupedOrders = statusEntry.getValue();

                                long count = groupedOrders.size();

                                double revenue = groupedOrders.stream()
                                        .mapToDouble(Order::getPrice)
                                        .sum();

                                LocalDateTime deliveredDate = groupedOrders.stream()
                                        .map(Order::getDeliveredDate)
                                        .filter(Objects::nonNull)
                                        .max(LocalDateTime::compareTo)
                                        .orElse(null);

                                return new OrderAnalyticsResponse(
                                        createdDate,
                                        deliveredDate,
                                        status,
                                        count,
                                        revenue
                                );
                            });
                })

                .sorted(
                        Comparator
                                .comparing(OrderAnalyticsResponse::getCreatedDate)
                                .thenComparing(OrderAnalyticsResponse::getStatus)
                )

                .toList();

        return paginate(analytics, pageable);
    }

    @Transactional
    public void deleteOrders(List<Order> ordersToRm) {
        orderRepository.deleteAll(ordersToRm);
    }

    public Order getOrder(UUID uuid) {
        var order = orderRepository.findById(uuid);
        var check = order.isPresent();

        return check ? order.get() : null;
    }

    public AnalyticsRangeResponse getRange(String timezone) {

        ZoneId userZone = ZoneId.of(timezone);

        AnalyticsRangeResponse range = orderRepository.getRange();

        if (range == null || range.getMinDate() == null) {
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

            LocalDateTime min = now.minusDays(7);
            LocalDateTime max = now;

            return new AnalyticsRangeResponse(
                    convert(min, userZone),
                    convert(max, userZone)
            );
        }

        return new AnalyticsRangeResponse(
                convert(range.getMinDate(), userZone),
                convert(range.getMaxDate(), userZone)
        );
    }

    private LocalDateTime convert(LocalDateTime utc, ZoneId zone) {
        return utc
                .atOffset(ZoneOffset.UTC)
                .atZoneSameInstant(zone)
                .toLocalDateTime();
    }

    private Page<OrderAnalyticsResponse> paginate(
            List<OrderAnalyticsResponse> items,
            Pageable pageable
    ) {
        int total = items.size();

        long offset = pageable.getOffset();

        if (offset >= total) {
            return new PageImpl<>(
                    List.of(),
                    pageable,
                    total
            );
        }

        int start = (int) offset;
        int end = Math.min(start + pageable.getPageSize(), total);

        List<OrderAnalyticsResponse> content = items.subList(start, end);

        return new PageImpl<>(
                content,
                pageable,
                total
        );
    }
}
