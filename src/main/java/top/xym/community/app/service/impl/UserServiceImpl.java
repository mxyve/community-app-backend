package top.xym.community.app.service.impl;


import cn.hutool.system.UserInfo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.xym.community.app.common.cache.RequestContext;
import top.xym.community.app.common.exception.ErrorCode;
import top.xym.community.app.common.exception.ServerException;
import top.xym.community.app.convert.UserConvert;
import top.xym.community.app.mapper.UserMapper;
import top.xym.community.app.model.dto.UserEditDTO;
import top.xym.community.app.model.entity.User;
import top.xym.community.app.model.vo.UserInfoVO;
import top.xym.community.app.service.UserService;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public UserInfoVO userInfo() {
        Integer userId = RequestContext.getUserId();
        // 查询数据库
        User user = baseMapper.selectById(userId);
        if (user == null) {
            log.error("用户不存在，userId: {}", userId);
            throw new ServerException(ErrorCode.USER_NOT_EXIST);
        }
        UserInfoVO userInfoVO = UserConvert.INSTANCE.convert(user);
        return userInfoVO;
    }

    @Override
    public UserInfoVO updateInfo(UserEditDTO userEditDTO) {
        Integer userId = RequestContext.getUserId();
        userEditDTO.setUserId(userId);
        User user = UserConvert.INSTANCE.convert((userEditDTO));
        if (user.getUserId() == null) {
            throw new ServerException(ErrorCode.PARAMS_ERROR);
        }
        try {
            if (baseMapper.updateById(user) < 1) {
                throw new ServerException("修改失败");
            }
        } catch(Exception e) {
            throw new ServerException((e.getMessage()));
        }
        return this.userInfo();
    }

}
