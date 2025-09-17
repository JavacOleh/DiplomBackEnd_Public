package diplom.model.entity.order;

import java.util.Arrays;
import java.util.Comparator;

public enum OrderStatus {
    CREATED(0),
    RECEIVED(1),
    ACCEPTED(2),
    DELIVERING(3),
    DELIVERED(4),
    RETURNED(5);

    private final int code;

    OrderStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static OrderStatus fromCode(int code) {
        for (OrderStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
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
