package top.xym.community.app.common.filter;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import top.xym.community.app.common.cache.RedisCache;
import top.xym.community.app.common.cache.RedisKeys;
import top.xym.community.app.utils.JwtUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * JWT 认证过滤器
 *
 * @author mqxu
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final RedisCache redisCache;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 从请求头获取 Token
        String token = getTokenFromRequest(request);
        if (StrUtil.isNotBlank(token)) {
            // 验证 Token
            if (jwtUtils.validateToken(token)) {
                // 从 Token 中获取用户 ID
                Long userId = jwtUtils.getUserIdFromToken(token);
                // 检查 Redis 中是否存在该用户的 token
                String tokenKey = RedisKeys.getUserTokenKey(userId);
                String savedToken = redisCache.get(tokenKey, String.class);

                if (token.equals(savedToken)) {
                    // Token 有效，检查是否需要续期
                    Long remainingTime = jwtUtils.getExpiration(token);
                    // 如果剩余时间小于30分钟，自动续期
                    if (remainingTime != null && remainingTime < 30 * 60 * 1000) {
                        String newToken = jwtUtils.generateToken(userId);
                        // 更新 Redis 中的 token
                        redisCache.set(tokenKey, newToken, jwtUtils.getExpiration(newToken) / 1000, TimeUnit.SECONDS);
                        log.info("Token 自动续期成功，userId: {}", userId);
                    }

                    // Token 有效，创建认证对象,权限改为空权限列表，更适合纯 API 项目
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    Collections.emptyList()
                            );
                    // 设置到 Security 上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头获取 Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
