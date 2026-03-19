package top.xym.community.app.module.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.xym.community.app.module.service.model.entity.ServiceOrder;

@Mapper
public interface ServiceOrderMapper extends BaseMapper<ServiceOrder> {
}
