package top.xym.community.app.common.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import top.xym.community.app.common.result.Result;

import java.io.IOException;

/**
 * Security 统一异常处理器
 *
 * @author mqxu
 */
@Component
@RequiredArgsConstructor
public class SecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /**
     * 未登录或登录过期处理
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "未登录或登录已过期，请先登录");
    }

    /**
     * 权限不足处理
     */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        writeResponse(response, HttpServletResponse.SC_FORBIDDEN, "权限不足，拒绝访问");
    }

    /**
     * 统一写入响应
     */
    private void writeResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        Result<String> result = Result.error(status, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
