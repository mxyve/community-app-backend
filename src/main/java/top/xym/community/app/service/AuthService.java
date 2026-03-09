package top.xym.community.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.xym.community.app.model.dto.WxLoginDTO;
import top.xym.community.app.model.entity.User;
import top.xym.community.app.model.vo.UserLoginVO;

public interface AuthService extends IService<User> {

    /**
     * 登录
     *
     * @param phone 电话
     * @param code 验证码
     * @return {@link UserLoginVO}
     */
    UserLoginVO loginByPhone(String phone, String code);

    /**
     * 微信登录
     *
     * @param loginDTO DTO
     * @return {@link UserLoginVO}
     */
    UserLoginVO weChatLogin(WxLoginDTO loginDTO);

    /**
     * 登出
     */
    void logout();

    /**
     * 绑定手机号
     *
     * @param phone 电话
     * @param code 验证码
     * @param accessToken 访问令牌
     */
    void bindPhone(String phone, String code, String accessToken);
}
