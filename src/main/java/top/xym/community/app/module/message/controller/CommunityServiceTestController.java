package top.xym.community.app.module.message.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.xym.community.app.module.message.tool.CommunityServiceTool;
import top.xym.community.app.utils.SecurityUtils;

@RestController
@RequestMapping("/api/v1/community/service")
@RequiredArgsConstructor
public class CommunityServiceTestController {

    private final CommunityServiceTool communityServiceTool;

    @GetMapping("/query")
    public String testQuery(
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false, defaultValue = "") String query
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        return communityServiceTool.queryCommunityService(userId, province, city, district, query);
    }
}