package top.xym.community.app.module.myProfile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.xym.community.app.module.myProfile.model.User;

@Mapper
public interface MyProfileMapper extends BaseMapper<User> {
}