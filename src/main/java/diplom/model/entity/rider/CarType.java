package diplom.model.entity.rider;

import java.util.Arrays;
import java.util.Comparator;

public enum CarType {
    BICYCLE(1), 
    MOTORCYCLE(2), 
    CAR(3);

    private final int value;

    CarType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    // Метод для проверки, находится ли статус в допустимом диапазоне
    public static boolean isValidStatus(CarType status) {
        CarType[] statuses = values();
        Arrays.stream(statuses).sorted(Comparator.naturalOrder());

        CarType minStatus = statuses[0];
        CarType maxStatus = statuses[statuses.length - 1];

        return status.ordinal() >= minStatus.ordinal() && status.ordinal() <= maxStatus.ordinal();
    }

    public static CarType fromValue(int value) {
        for (CarType carType : CarType.values()) {
            if (carType.getValue() == value) {
                return carType;
            }
        }
        throw new IllegalArgumentException("Unexpected value: " + value);
    }
}
