package top.xym.community.app.module.message.tool;

import com.alibaba.fastjson2.JSON;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import top.xym.community.app.model.entity.User;
import top.xym.community.app.module.message.model.vo.ServiceVO;
import top.xym.community.app.module.message.service.AppCommunityService;
import top.xym.community.app.service.UserService;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CommunityServiceTool {

    private final AppCommunityService appCommunityService;
    private final UserService userService;

    public CommunityServiceTool(AppCommunityService appCommunityService, UserService userService) {
        this.appCommunityService = appCommunityService;
        this.userService = userService;
    }

    @Tool(description = "查询社区服务列表，包含维修、回收、宠物、养老、家政、附近服务，仅返回纯JSON数组，禁止解释、禁止添加文字")
    public String queryCommunityService(Long userId, String targetProvince, String targetCity, String targetDistrict, String userQueryText) {
        String province = targetProvince;
        String city = targetCity;
        String district = targetDistrict;

        // 地址为空 → 取用户资料
        if (province == null || province.isBlank()) {
            User user = userService.getById(userId);
            if (user != null) {
                province = user.getProvince();
                city = user.getCity();
                district = user.getDistrict();
            }
        }

        // 最终还是空 → 返回空数组
        if (province == null || province.isBlank()) {
            return "[]";
        }

        // 拼接地区
        String cityFull = (city != null && !city.isEmpty()) ? (province + "-" + city) : null;
        String districtFull = null;

        if (cityFull != null && district != null && !district.isEmpty()) {
            districtFull = cityFull + "-" + district;
        }

        // ：null 必须转空字符串，否则 SQL 会查 "null" 字符串！
        String districtParam = (districtFull == null) ? "" : districtFull;
        String cityParam = (cityFull == null) ? "" : cityFull;

        // ====================== 智能数量 ======================
        int limit = 5;
        if (userQueryText != null) {
            if (userQueryText.contains("最好") || userQueryText.contains("最优") ||
                    userQueryText.contains("评分最高") || userQueryText.contains("最高评分") ||
                    userQueryText.contains("第一名") || userQueryText.contains("1个") || userQueryText.contains("一个")) {
                limit = 1;
            } else {
                Pattern pattern = Pattern.compile("([0-9]+)个|([0-9]+)条");
                Matcher matcher = pattern.matcher(userQueryText);
                if (matcher.find()) {
                    String numStr = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                    try {
                        int userLimit = Integer.parseInt(numStr);
                        if (userLimit > 0 && userLimit <= 20) limit = userLimit;
                    } catch (Exception ignored) {}
                }
            }
        }

        List<ServiceVO> list = appCommunityService.queryNearbyHighScoreService(
                province,
                cityParam,
                districtParam,
                limit
        );

        return JSON.toJSONString(list == null ? List.of() : list);
    }
}