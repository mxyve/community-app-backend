package top.xym.community.app.module.service.service;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.xym.community.app.common.result.Result;
import top.xym.community.app.mapper.UserMapper;
import top.xym.community.app.model.dto.PageResponse;
import top.xym.community.app.model.entity.User;
import top.xym.community.app.module.service.config.AliPayConfig;
import top.xym.community.app.module.service.mapper.PayServiceOrderMapper;
import top.xym.community.app.module.service.mapper.ServiceMerchantMapper;
import top.xym.community.app.module.service.mapper.ServiceOrderMapper;
import top.xym.community.app.module.service.mapper.ServicesMapper;
import top.xym.community.app.module.service.model.dto.OrderPageRequest;
import top.xym.community.app.module.service.model.entity.PayServiceOrder;
import top.xym.community.app.module.service.model.entity.ServiceMerchant;
import top.xym.community.app.module.service.model.entity.ServiceOrder;
import top.xym.community.app.module.service.model.entity.Services;
import top.xym.community.app.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceOrderService {

    private final ServiceOrderMapper orderMapper;
    private final ServicesMapper servicesMapper;
    private final ServiceMerchantMapper merchantMapper;
    private final UserMapper userMapper;

    // 创建订单
    public ServiceOrder createOrder(ServiceOrder order) {
        Long userId = SecurityUtils.getCurrentUserId();
        // 生成唯一订单话号（时间戳+随机数）
        String orderNo = "ORD" + System.currentTimeMillis() + (int)((Math.random() * 9000) + 1000);
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setDeleted(0);
        order.setStatus(1); // 待服务
        orderMapper.insert(order);
        return order;
    }

    // 订单分页列表（按当前用户筛选）
    public PageResponse<ServiceOrder> getOrderPage(OrderPageRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<ServiceOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceOrder::getUserId, userId)
                .eq(ServiceOrder::getDeleted, 0)
                .orderByDesc(ServiceOrder::getCreateTime);

        if (request.getStatus() != null) {
            wrapper.eq(ServiceOrder::getStatus, request.getStatus());
        }

        Page<ServiceOrder> page = new Page<>(request.getCurrent(), request.getSize());
        Page<ServiceOrder> resultPage = orderMapper.selectPage(page, wrapper);

        // 关联服务、商家、用户名称
        List<ServiceOrder> records = resultPage.getRecords();
        for (ServiceOrder order : records) {
            Services service = servicesMapper.selectById(order.getServiceId());
            if (service != null) {
                order.setServiceName(service.getName());
            }
            ServiceMerchant merchant = merchantMapper.selectById(order.getMerchantId());
            if (merchant != null) {
                order.setMerchantName(merchant.getName());
            }
            User user = userMapper.selectById(order.getUserId());
            if (user != null) {
                order.setUserName(user.getNickName());
            }
        }
        return new PageResponse<>(resultPage.getCurrent(), resultPage.getSize(),
                resultPage.getTotal(), resultPage.getPages(), records);
    }

    // 订单详情
    public ServiceOrder getOrderDetail(Integer id) {
        ServiceOrder order = orderMapper.selectOne(
                new LambdaQueryWrapper<ServiceOrder>()
                        .eq(ServiceOrder::getId, id)
                        .eq(ServiceOrder::getDeleted, 0)
        );
        if (order != null) {
            // 1. 关联服务名称
            Services service = servicesMapper.selectById(order.getServiceId());
            if (service != null) {
                order.setServiceName(service.getName());
            }

            // 2. 关联商家名称
            ServiceMerchant merchant = merchantMapper.selectById(order.getMerchantId());
            if (merchant != null) {
                order.setMerchantName(merchant.getName());
            }

            // 3. 关联用户名称（可选）
            User user = userMapper.selectById(order.getUserId());
            if (user != null) {
                order.setUserName(user.getNickName());
            }
        }
        return order;
    }

    // 更新订单
    public void updateOrder(ServiceOrder order) {
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    // 逻辑删除订单
    public void deleteOrder(Integer id) {
        ServiceOrder order = new ServiceOrder();
        order.setId(id);
        order.setDeleted(1);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Autowired
    private AliPayConfig aliPayConfig;
    @Autowired
    private PayServiceOrderMapper payServiceOrderMapper;

    private static final String GATEWAY = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private static final String FORMAT = "JSON";
    private static final String CHARSET = "UTF-8";
    private static final String SIGN_TYPE = "RSA2";

    public Result createAlipayQrCode(String orderNo) {
        ServiceOrder order = orderMapper.selectOne(new LambdaQueryWrapper<ServiceOrder>()
                .eq(ServiceOrder::getOrderNo, orderNo));
        if (order == null) return Result.error("订单不存在");

        try {
            PayServiceOrder pay = new PayServiceOrder();
            pay.setOrderNo(orderNo);
            pay.setUserId(order.getUserId());
            pay.setPayAmount(order.getPayAmount());
            pay.setPayStatus(0);
            payServiceOrderMapper.insert(pay);

            AlipayClient client = new DefaultAlipayClient(
                    GATEWAY, aliPayConfig.getAppId(),
                    aliPayConfig.getAppPrivateKey(), FORMAT, CHARSET,
                    aliPayConfig.getAlipayPublicKey(), SIGN_TYPE
            );

            AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
            request.setNotifyUrl(aliPayConfig.getNotifyUrl());

            String biz = String.format(
                    "{\"out_trade_no\":\"%s\",\"total_amount\":\"%s\",\"subject\":\"服务订单支付\"}",
                    orderNo, order.getPayAmount()
            );
            request.setBizContent(biz);

            AlipayTradePrecreateResponse response = client.execute(request);
            if (response.isSuccess()) {
                return Result.success("ok", response.getQrCode());
            } else {
                return Result.error("支付创建失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("支付异常");
        }
    }

    @Transactional
    public String payNotify(HttpServletRequest request) {
        String outTradeNo = request.getParameter("out_trade_no");
        String tradeStatus = request.getParameter("trade_status");

        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            // 更新支付表
            payServiceOrderMapper.updatePayStatus(outTradeNo);

            // 更新业务订单 → 已完成
            ServiceOrder order = new ServiceOrder();
            order.setOrderNo(outTradeNo);
            order.setStatus(4); // 已完成
            order.setPaymentTime(LocalDateTime.now());
            order.setPaymentMethod(1); // 支付宝
            orderMapper.update(order, new LambdaUpdateWrapper<ServiceOrder>()
                    .eq(ServiceOrder::getOrderNo, outTradeNo));

            return "success";
        }
        return "fail";
    }

    public Result getPayStatus(String orderNo) {
        Integer status = payServiceOrderMapper.getPayStatus(orderNo);
        return Result.success(null, status != null && status == 1);
    }

}
