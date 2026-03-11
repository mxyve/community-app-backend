package top.xym.community.app.convert;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import top.xym.community.app.model.dto.UserEditDTO;
import top.xym.community.app.model.entity.User;
import top.xym.community.app.model.vo.UserInfoVO;

@Mapper
public interface UserConvert {

    // 获取 UserConvert 实例，由 MapStruct 自动生成实现类并提供实例
    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    // 将 User 对象转换为 UserInfoVO 对象
    UserInfoVO convert(User user);

    // 将 UserEditDTO 对象转换为 User 对象
    User convert(UserEditDTO dto);
}
