package top.xym.community.app.module.tenant.information.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import top.xym.community.app.module.tenant.information.model.entity.Information;

@Mapper
public interface InformationMapper extends BaseMapper<Information> {

    /**
     * 增加浏览量
     */
    @Update("UPDATE i_information SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(Integer id);
}