package top.xym.community.app.module.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.xym.community.app.common.result.Result;
import top.xym.community.app.model.dto.PageResponse;
import top.xym.community.app.module.service.model.dto.OrderPageRequest;
import top.xym.community.app.module.service.model.entity.ServiceOrder;
import top.xym.community.app.module.service.service.ServiceOrderService;
import top.xym.community.app.utils.SecurityUtils;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "服务订单接口")
@RequiredArgsConstructor
public class ServiceOrderController {
    private final ServiceOrderService orderService;

    // 创建订单
    @PostMapping("/create")
    @Operation(summary = "创建服务订单")
    public Result<ServiceOrder> createOrder(@RequestBody ServiceOrder order) {
        return Result.success("创建成功", orderService.createOrder(order));
    }

    // 订单分页列表
    @PostMapping("/pages")
    @Operation(summary = "获取当前用户订单分页列表")
    public Result<PageResponse<ServiceOrder>> getOrderPage(@RequestBody OrderPageRequest request) {
        return Result.success("查询成功", orderService.getOrderPage(request));
    }

    // 订单详情
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取订单详情")
    public Result<ServiceOrder> getOrderDetail(@PathVariable Integer id) {
        return Result.success("查询成功", orderService.getOrderDetail(id));
    }

    // 更新订单
    @PutMapping("/update")
    @Operation(summary = "更新订单信息")
    public Result<Void> updateOrder(@RequestBody ServiceOrder order) {
        orderService.updateOrder(order);
        return Result.success("更新成功", null);
    }

    // 删除订单（逻辑删除）
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除订单")
    public Result<Void> deleteOrder(@PathVariable Integer id) {
        orderService.deleteOrder(id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/createPay")
    public Result<?> createPay(@RequestBody Map<String, String> map) {
        return orderService.createAlipayQrCode(map.get("orderNo"));
    }

    @PostMapping("/payNotify")
    public String payNotify(HttpServletRequest request) {
        return orderService.payNotify(request);
    }

    @GetMapping("/payStatus/{orderNo}")
    public Result<?> getPayStatus(@PathVariable String orderNo) {
        return orderService.getPayStatus(orderNo);
    }

    @GetMapping("/cancel")
    @Operation(summary = "用户申请取消订单 2、3→申请中）")
    public Result<String> cancelOrder(
            @RequestParam String orderNo,
            @RequestParam String cancelReason
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        boolean success = orderService.cancelUserOrder(userId, orderNo, cancelReason);
        if (success) {
            return Result.success("申请取消成功");
        } else {
            return Result.error("操作失败：订单不存在/非本人/状态不允许取消");
        }
    }

    @GetMapping("/applyRefund")
    @Operation(summary = "申请退款（仅已完成订单）")
    public Result<String> applyRefund(
            @RequestParam String orderNo,
            @RequestParam String refundReason
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        boolean success = orderService.applyRefund(userId, orderNo, refundReason);
        if (success) {
            return Result.success("退款申请已提交");
        } else {
            return Result.error("操作失败：订单不存在/非本人/非已完成状态");
        }
    }

}