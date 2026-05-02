package top.xym.community.app.module.message.service;

import top.xym.community.app.module.message.model.vo.ServiceVO;

import java.util.List;

public interface AppCommunityService {
    List<ServiceVO> queryNearbyHighScoreService(
            String province,
            String city,
            String district,
            int limit
    );
}