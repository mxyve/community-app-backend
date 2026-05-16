package top.xym.community.app.module.message.tool;

import com.alibaba.fastjson2.JSON;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import top.xym.community.app.module.service.model.dto.OrderPageRequest;
import top.xym.community.app.module.service.model.entity.ServiceOrder;
import top.xym.community.app.module.service.service.ServiceOrderService;
import top.xym.community.app.utils.SecurityUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class OrderTool {

    private final ServiceOrderService orderService;

    public OrderTool(ServiceOrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 查询我的订单列表（支持状态筛选）
     */
    @Tool(description = "查询当前用户的服务订单列表，可以按订单状态筛选")
    public String listOrders(Long userId,
                             @ToolParam(required = false, description = "订单状态：1待服务2服务中3待付款4已完成5取消申请中6已取消7退款中8已退款，不传查全部") Integer status) {
        OrderPageRequest request = new OrderPageRequest();
        request.setCurrent(1L);
        request.setSize(10L);
        request.setStatus(status);
        System.out.println("status: " + status);

        var page = orderService.getOrderPage(request);
        return JSON.toJSONString(page);
    }

    /**
     * 查询订单详情
     */
    @Tool(description = "根据订单ID查询订单的详细信息")
    public String getOrderDetail(Long userId, Integer orderId) {
        userId = SecurityUtils.getCurrentUserId();
        ServiceOrder order = orderService.getOrderDetail(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return "订单不存在或无权查看";
        }
        return JSON.toJSONString(order);
    }

    /**
     * 创建订单
     */
    @Tool(description = "创建服务订单，需要：服务ID、商家ID、规格ID、数量、单价、总价、支付金额、服务时间、服务地址、联系人、联系电话")
    public String createOrder(
            Long userId,
            Integer merchantId,
            Integer serviceId,
            Integer specId,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal totalAmount,
            BigDecimal payAmount,
            LocalDateTime serviceTime,
            String serviceAddress,
            String contactName,
            String contactPhone
    ) {
        ServiceOrder order = new ServiceOrder();
        order.setUserId(userId);
        order.setMerchantId(merchantId);
        order.setServiceId(serviceId);
        order.setSpecId(specId);
        order.setQuantity(quantity);
        order.setUnitPrice(unitPrice);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(payAmount);
        order.setServiceTime(serviceTime);
        order.setServiceAddress(serviceAddress);
        order.setContactName(contactName);
        order.setContactPhone(contactPhone);

        ServiceOrder result = orderService.createOrder(order);
        return "订单创建成功，订单号：" + result.getOrderNo();
    }

    /**
     * 取消订单
     */
    @Tool(description = "取消并删除自己的订单，执行逻辑删除，传入订单编号 orderNo")
    public String cancelOrder(Long userId, @ToolParam(description = "订单编号") String orderNo) {
        System.out.println("订单ID orderId：" + orderNo);
        userId = SecurityUtils.getCurrentUserId();
        System.out.println("用户Id userId：" + userId);

        // 1. 先查订单是否存在 & 是否是本人订单
        ServiceOrder order = orderService.getOrderDetailByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            return "订单不存在或无权取消";
        }

        // 2. 直接调用【逻辑删除方法】
        orderService.deleteOrderByOrderNo(userId, orderNo);

        return "订单已成功取消并删除";
    }

    /**
     * 删除订单
     */
    @Tool(description = "删除自己的订单（逻辑删除）")
    public String deleteOrder(Long userId, Integer orderId) {
        ServiceOrder order = orderService.getOrderDetail(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return "订单不存在或无权删除";
        }

        orderService.deleteOrder(orderId);
        return "订单已删除";
    }
}