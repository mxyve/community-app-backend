package top.xym.community.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.xym.community.app.model.dto.UserEditDTO;
import top.xym.community.app.model.entity.User;
import top.xym.community.app.model.vo.UserInfoVO;

public interface UserService extends IService<User> {

    /**
     * 用户信息
     *
     *  @return {@link UserInfoVO}
     */
    UserInfoVO userInfo();

    /**
     * 更新信息
     *
     * @param userEditDTO 用户编辑 DTO
     * @return UserInfoVO
     */
    UserInfoVO updateInfo(UserEditDTO userEditDTO);
}
