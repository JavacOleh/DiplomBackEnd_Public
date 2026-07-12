package diplom.service;

import diplom.entity.account.Account;
import diplom.security.AccountAuthRoles;
import diplom.repository.AccountRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class JwtTokenService {

    @Value("${jwt.secret.key}")
    private String secretKey; // Секретный ключ для подписи токенов

    @Autowired
    private AccountRepository accountRepository;

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24 часа

    /**
     * Генерация токена для пользователя или водителя по email.
     */
    public String generateToken(String email) {
        var role = getUserRoleByEmail(email); // Получаем роль из базы данных (пользователь или водитель)
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role.name())  // Сохраняем роль в токене
                .setIssuedAt(new Date())  // Время выдачи
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))  // Время истечения
                .signWith(SignatureAlgorithm.HS256, secretKey) // Подписываем токен
                .compact();
    }

    /**
     * Извлечение роли из токена.
     */
    public String getRoleFromToken(String token) {
        Claims claims = extractClaims(token);
        return claims.get("role", String.class); // Получаем роль из токена
    }

    /**
     * Извлечение данных из токена.
     */
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()  // Используем Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Получаем роль пользователя по email.
     */
    private AccountAuthRoles getUserRoleByEmail(String email) {
        // Ищем пользователя по email
        Optional<Account> user = accountRepository.findUserByEmail(email);
        if (user.isPresent()) {
            return user.get().getRole();
        }

        // Если пользователь не найден, выбрасываем исключение
        throw new RuntimeException("User not found!");
    }

    public String getEmailFromToken(String token) {
        return extractClaims(token).getSubject();
    }
}