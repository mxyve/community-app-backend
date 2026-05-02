package top.xym.community.app.module.message.mapper;

import org.apache.ibatis.annotations.Param;
import top.xym.community.app.module.message.model.vo.ServiceVO;

import java.util.List;

public interface AppCommunityServiceMapper {
    List<ServiceVO> selectNearbyHighScoreService(
            @Param("province") String province,
            @Param("city") String city,
            @Param("district") String district,
            @Param("limit") int limit
    );
}