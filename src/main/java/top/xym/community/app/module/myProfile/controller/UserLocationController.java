package top.xym.community.app.module.myProfile.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.xym.community.app.common.result.Result;
import top.xym.community.app.module.myProfile.model.User;
import top.xym.community.app.module.myProfile.service.MyProfileService;
import top.xym.community.app.utils.SecurityUtils;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/location")
@Tag(name = "位置接口")
@RequiredArgsConstructor
public class UserLocationController {

    @Resource
    private MyProfileService userService;

    /**
     * 更新用户省市区位置
     */
    @PostMapping("/updateLocation")
    public Result<?> updateLocation(@RequestBody User user) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 执行更新：只更新 province、city、district
        User updateUser = new User();
        updateUser.setUserId(userId);
        // 可为空
        updateUser.setProvince(user.getProvince());
        // 可为空
        updateUser.setCity(user.getCity());
        // 可为空
        updateUser.setDistrict(user.getDistrict());

        boolean success = userService.updateById(updateUser);
        return success ? Result.success("位置保存成功") : Result.error("位置保存失败");
    }

    @GetMapping("/getLocation")
    public Result<?> getLocation() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userService.getById(userId);
        Map<String, Object> map = new HashMap<>();
        map.put("province", user.getProvince());
        map.put("city", user.getCity());
        map.put("district", user.getDistrict());
        map.put("fullText",
                (user.getProvince() == null ? "" : user.getProvince())
                        + (user.getCity() == null ? "" : " " + user.getCity())
                        + (user.getDistrict() == null ? "" : " " + user.getDistrict())
        );
        return Result.success(map);
    }


}
