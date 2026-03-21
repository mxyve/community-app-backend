package top.xym.community.app.module.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.xym.community.app.model.dto.PageResponse;
import top.xym.community.app.module.service.mapper.ServiceCartsMapper;
import top.xym.community.app.module.service.mapper.ServiceMerchantMapper;
import top.xym.community.app.module.service.mapper.ServicesMapper;
import top.xym.community.app.module.service.model.dto.OrderPageRequest;
import top.xym.community.app.module.service.model.entity.ServiceCarts;
import top.xym.community.app.module.service.model.entity.ServiceMerchant;
import top.xym.community.app.module.service.model.entity.Services;
import top.xym.community.app.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceCartsService {

    private final ServiceCartsMapper serviceCartsMapper;
    private final ServicesMapper servicesMapper;
    private final ServiceMerchantMapper merchantMapper;

    // 添加到购物车
    public ServiceCarts addCarts(ServiceCarts carts) {
        Long userId = SecurityUtils.getCurrentUserId();
        // 检查是否存在同服务+规格的购物车项
        LambdaQueryWrapper<ServiceCarts> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceCarts::getUserId, userId)
               .eq(ServiceCarts::getServiceId, carts.getServiceId())
               .eq(ServiceCarts::getSpecId, carts.getSpecId())
               .eq(ServiceCarts::getDeleted, 0);
        ServiceCarts existCarts = serviceCartsMapper.selectOne(wrapper);

        if (existCarts != null) {
            // 存在则数量+1
            existCarts.setQuantity(existCarts.getQuantity() + carts.getQuantity());
            existCarts.setUpdateTime(LocalDateTime.now());
            serviceCartsMapper.updateById(existCarts);
            return existCarts;
        } else {
            // 不存在则新增
            carts.setUserId(userId);
            carts.setSelected(true); // 默认选中
            carts.setCreateTime(LocalDateTime.now());
            carts.setUpdateTime(LocalDateTime.now());
            carts.setDeleted(0);
            serviceCartsMapper.insert(carts);
            return carts;
        }
    }

    // 删除购物车项（逻辑删除）
    public void deleteCarts(Integer id) {
        ServiceCarts carts = new ServiceCarts();
        carts.setId(id);
        carts.setDeleted(1);
        carts.setUpdateTime(LocalDateTime.now());
        serviceCartsMapper.updateById(carts);
    }

    // 分页查询购物车列表
    public PageResponse<ServiceCarts> getCartPage(OrderPageRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<ServiceCarts> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceCarts::getUserId, userId)
               .eq(ServiceCarts::getDeleted, 0)
               .orderByDesc(ServiceCarts::getCreateTime);

        Page<ServiceCarts> page = new Page<>(request.getCurrent(), request.getSize());
        Page<ServiceCarts> resultPage = serviceCartsMapper.selectPage(page, wrapper);

        // 关联服务、商家信息
        List<ServiceCarts> records = resultPage.getRecords();
        for (ServiceCarts carts : records) {
            Services service = servicesMapper.selectById(carts.getServiceId());
            if (service != null) {
                carts.setServiceName(service.getName());
                carts.setBasePrice(service.getBasePrice());
            }
            ServiceMerchant merchant = merchantMapper.selectById(carts.getMerchantId());
            if (merchant != null) {
                carts.setMerchantName(merchant.getName());
            }
        }

        return new PageResponse<>(resultPage.getCurrent(), resultPage.getSize(),
                resultPage.getTotal(), resultPage.getPages(), records);
    }

    // 更新购物车数量/选中状态
    public void updateCarts(ServiceCarts carts) {
        carts.setUpdateTime(LocalDateTime.now());
        serviceCartsMapper.updateById(carts);
    }

}
