package top.xym.community.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import top.xym.community.app.common.exception.ErrorCode;
import top.xym.community.app.common.exception.ServerException;

/**
 * Security 工具类
 * 提供获取当前认证用户信息的方法
 *
 * @author xym
 */
@Slf4j
public class SecurityUtils {

    /**
     * 获取当前认证用户的 ID
     *
     * @return 当前认证用户的 ID
     * @throws ServerException 用户未认证时抛出异常
     */
    public static Long getCurrentUserId() {
        Authentication authentication = getCurrentAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            log.warn("用户未认证，无法获取用户 ID");
            throw new ServerException(ErrorCode.UNAUTHORIZED);
        }
        try {
            return (Long) authentication.getPrincipal();
        } catch (ClassCastException e) {
            log.error("获取用户 ID 失败，principal 类型不是 Long: {}", authentication.getPrincipal().getClass(), e);
            throw new ServerException(ErrorCode.UNAUTHORIZED);
        }
    }

    /**
     * 获取当前认证对象
     *
     * @return Authentication 对象，可能位 null
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

}
