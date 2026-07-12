package diplom.security;

public enum AccountAuthRoles {
    USER,
    RIDER,
    MANAGER;

    public static AccountAuthRoles getByStr(String role) {
        return switch (role) {
            case "USER" -> USER;
            case "RIDER" -> RIDER;
            case "MANAGER" -> MANAGER;

            case null, default -> null;
        };
    }
}
