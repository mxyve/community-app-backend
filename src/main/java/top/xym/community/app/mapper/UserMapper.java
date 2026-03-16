package top.xym.community.app.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.xym.community.app.model.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    default User getByPhone(String phone) {
        return this.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }

    default User getByWxOpenId(String openId) {
        return this.selectOne(new LambdaQueryWrapper<User>().eq(User::getWxOpenId, openId));
    }
}
