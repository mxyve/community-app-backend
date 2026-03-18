package top.xym.community.app.module.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.xym.community.app.common.result.Result;
import top.xym.community.app.model.dto.PageResponse;
import top.xym.community.app.module.service.model.dto.ServicePageRequest;
import top.xym.community.app.module.service.model.entity.ServiceCategory;
import top.xym.community.app.module.service.model.entity.Services;
import top.xym.community.app.module.service.service.ServicesService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
@Tag(name = "生活助手服务接口")
@RequiredArgsConstructor
public class ServicesController {

    private final ServicesService servicesService;

    /**
     * 1. 服务分类列表
     */
    @GetMapping("/category/list")
    @Operation(summary = "获取服务分类列表")
    public Result<List<ServiceCategory>> getCategoryList() {
        List<ServiceCategory> list = servicesService.getCategoryList();
        return Result.success("查询成功", list);
    }

    /**
     * 2. 服务列表分页
     */
    @PostMapping("/pages")
    @Operation(summary = "生活助手服务分页列表（自动按用户地区筛选）")
    public Result<PageResponse<Services>> getServicePage(@RequestBody ServicePageRequest request) {
        PageResponse<Services> page = servicesService.getServicePage(request);
        return Result.success("查询成功", page);
    }

    /**
     * 3. 服务详情
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取服务详情")
    public Result<Services> getServiceDetail(@PathVariable Integer id) {
        Services detail = servicesService.getServiceDetail(id);
        return Result.success("查询成功", detail);
    }

}
