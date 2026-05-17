package top.xym.community.app.module.tenant.information.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.xym.community.app.common.result.Result;
import top.xym.community.app.module.tenant.information.model.entity.Information;
import top.xym.community.app.module.tenant.information.service.InformationService;

@RestController
@RequestMapping("/api/front/information")
@AllArgsConstructor
@Tag(name = "前台资讯模块")
public class InformationFrontController {

    private final InformationService informationService;

    /**
     * 前台分页查询资讯列表（支持地区匹配）
     */
    @GetMapping("/page")
    @Operation(summary = "前台分页查询资讯列表")
    public Result<?> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district) {
        return Result.success("查询成功",
                informationService.frontPageList(pageNum, pageSize, title, province, city, district));
    }

    /**
     * 前台获取资讯详情（自动增加浏览量）
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "前台获取资讯详情")
    public Result<?> detail(@PathVariable Integer id) {
        Information information = informationService.frontGetDetail(id);
        if (information == null) {
            return Result.error("资讯不存在或已下架");
        }
        return Result.success("查询成功", information);
    }
}