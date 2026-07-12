package diplom.entity.order;

import java.util.Arrays;
import java.util.Comparator;

public enum OrderStatus {
    CREATED(0),
    RECEIVED(1),
    RECEIVED_FOR_DELIVERY(2),
    ACCEPTED_FOR_DELIVERY(3),
    DELIVERED(4),
    DECLINED(5);

    private final int code;

    OrderStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    // Метод для проверки, находится ли статус в допустимом диапазоне
    public static boolean isValidStatus(OrderStatus status) {
        OrderStatus[] statuses = values();
        Arrays.stream(statuses).sorted(Comparator.naturalOrder());

        OrderStatus minStatus = statuses[0];
        OrderStatus maxStatus = statuses[statuses.length - 1];

        return status.ordinal() >= minStatus.ordinal() && status.ordinal() <= maxStatus.ordinal();
    }
}
