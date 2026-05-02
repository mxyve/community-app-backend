package top.xym.community.app.module.message.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.xym.community.app.module.message.mapper.AppCommunityServiceMapper;
import top.xym.community.app.module.message.model.vo.ServiceVO;
import top.xym.community.app.module.message.service.AppCommunityService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppCommunityServiceImpl implements AppCommunityService {

    private final AppCommunityServiceMapper appCommunityServiceMapper;

    @Override
    public List<ServiceVO> queryNearbyHighScoreService(
            String province,
            String city,
            String district,
            int limit
    ) {
        // 直接按顺序传给 XML：district、city、province
        return appCommunityServiceMapper.selectNearbyHighScoreService(
                district,
                city,
                province,
                limit
        );
    }
}
