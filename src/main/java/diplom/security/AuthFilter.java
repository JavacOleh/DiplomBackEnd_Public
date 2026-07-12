package diplom.security;

import diplom.service.JwtTokenService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthFilter extends OncePerRequestFilter {
    private final JwtTokenService jwtTokenService;

    @Autowired
    public AuthFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // 🔓 1. ПУБЛИЧНЫЕ ENDPOINTS (БЕЗ JWT)
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 🔐 2. JWT ОБЯЗАТЕЛЕН ТОЛЬКО ДАЛЬШЕ
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String roleStr = jwtTokenService.getRoleFromToken(token);
            AccountAuthRoles role = AccountAuthRoles.valueOf(roleStr);

            String email = jwtTokenService.getEmailFromToken(token);
            request.setAttribute("email", email);
            request.setAttribute("role", role.name());

            // 🔐 3. ПРОВЕРКА ДОСТУПА ПО РОЛИ
            if (isRoleAllowedForPathAndMethod(role, path, method)) {
                filterChain.doFilter(request, response);
                return;
            }

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        } catch (Exception e) {
            System.out.println("AUTH_FILTER ERROR: " + e.getClass());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }


    // 🔓 публичные пути
    private boolean isPublicPath(String path) {
        return path.startsWith("/api/accounts/auth")
                || path.startsWith("/api/accounts/add")
                || path.startsWith("/api/contacts")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui.html")
                || path.startsWith("/webjars");
    }

    private static boolean isSwaqgger(FilterChain filterChain, String path, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui.html")
                || path.startsWith("/webjars")) {

            filterChain.doFilter(request, response);
            return true;
        }
        return false;
    }

    private boolean isRoleAllowedForPathAndMethod(AccountAuthRoles role, String path, String method) {

        // 🔓 Публичные эндпоинты
        if (path.startsWith("/api/accounts/auth") || path.startsWith("/api/accounts/add") || path.startsWith("/api/contacts")) {
            return true;
        }

        return switch (role) {
            case USER -> {
                if (path.startsWith("/api/goods")
                        || path.startsWith("/api/drive")
                        || path.startsWith("/api/contacts")) {
                    yield method.equalsIgnoreCase("GET");
                }

                yield path.startsWith("/api/accounts") || path.startsWith("/api/orders");
            }
            case RIDER -> {
                if (path.startsWith("/api/goods")
                        || path.startsWith("/api/drive")
                        || path.startsWith("/api/contacts")) {
                    yield method.equalsIgnoreCase("GET");
                }

                yield path.startsWith("/api/accounts") || path.startsWith("/api/orders");
            }
            case MANAGER -> path.startsWith("/api/accounts")
                    || path.startsWith("/api/goods")
                    || path.startsWith("/api/drive")
                    || path.startsWith("/api/orders")
                    || path.startsWith("/api/contacts");
            default -> false;
        };
    }
}
