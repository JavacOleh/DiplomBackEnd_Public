package diplom.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.util.Base64;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthFilter implements Filter {
    private String authKey;

    @Autowired
    public AuthFilter(@Value("${auth.key}") String authKey) {
        this.authKey = authKey;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String key = request.getHeader("Authorization");
        if (isKeyTrue(key)) {
            filterChain.doFilter(request, response);  // Если ключ правильный, пропускаем дальше
            return;
        }

        // Если ключ неверный, устанавливаем статус 403 (Forbidden) и возвращаем ошибку
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        /*
        response.getWriter().write("Access Denied: Invalid auth key");
        response.getWriter().flush();
        */
    }

    public boolean isKeyTrue(String authKey) {
        byte[] strInBytes;
        String key;
        try {
            strInBytes = Base64.getDecoder().decode(authKey);
            key = new String(strInBytes);
        }catch (NullPointerException e) {
            return false;
        }
        return this.authKey.equals(key);
    }
}
