package top.xym.community.app.module.session.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.xym.community.app.module.session.model.entity.ChatSession;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession>{
}
