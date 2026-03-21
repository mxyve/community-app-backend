package top.xym.community.app.module.service.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.xym.community.app.common.result.Result;
import top.xym.community.app.model.dto.PageResponse;
import top.xym.community.app.module.service.model.dto.OrderPageRequest;
import top.xym.community.app.module.service.model.entity.ServiceCarts;
import top.xym.community.app.module.service.service.ServiceCartsService;

@RestController
@RequestMapping("/api/v1/carts")
@Tag(name = "购物车接口")
@RequiredArgsConstructor
public class ServiceCartsController {

    private final ServiceCartsService serviceCartsService;

    // 新增购物车
    @PostMapping("/add")
    @Operation(summary = "添加商品到购物车")
    public Result<ServiceCarts> addCarts(@RequestBody ServiceCarts cart) {
        return Result.success("添加成功", serviceCartsService.addCarts(cart));
    }

    // 分页查询购物车
    @PostMapping("/pages")
    @Operation(summary = "获取当前用户购物车列表")
    public Result<PageResponse<ServiceCarts>> getCartPage(@RequestBody OrderPageRequest request) {
        return Result.success("查询成功", serviceCartsService.getCartPage(request));
    }

    // 更新购物车（数量/选中状态）
    @PutMapping("/update")
    @Operation(summary = "更新购物车项")
    public Result<Void> updateCarts(@RequestBody ServiceCarts cart) {
        serviceCartsService.updateCarts(cart);
        return Result.success("更新成功", null);
    }

    // 删除购物车项
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除购物车项")
    public Result<Void> deleteCarts(@PathVariable Integer id) {
        serviceCartsService.deleteCarts(id);
        return Result.success("删除成功", null);
    }

}
