package top.xym.community.app.module.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.xym.community.app.mapper.UserMapper;
import top.xym.community.app.model.dto.PageResponse;
import top.xym.community.app.model.entity.User;
import top.xym.community.app.module.service.mapper.ServiceCategoryMapper;
import top.xym.community.app.module.service.mapper.ServiceMerchantMapper;
import top.xym.community.app.module.service.mapper.ServicesMapper;
import top.xym.community.app.module.service.model.dto.ServicePageRequest;
import top.xym.community.app.module.service.model.entity.ServiceCategory;
import top.xym.community.app.module.service.model.entity.ServiceMerchant;
import top.xym.community.app.module.service.model.entity.Services;
import top.xym.community.app.utils.SecurityUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServicesService {

    private final ServiceCategoryMapper categoryMapper;
    private final ServicesMapper servicesMapper;
    private final ServiceMerchantMapper merchantMapper;
    private final UserMapper userMapper;

    /**
     * 服务分类列表接口
     */
    public List<ServiceCategory> getCategoryList() {
        LambdaQueryWrapper<ServiceCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceCategory::getDeleted, 0)
                .eq(ServiceCategory::getStatus, 1)
                .orderByAsc(ServiceCategory::getSort);
        return categoryMapper.selectList(wrapper);
    }

    /**
     * 生活助手服务列表（分页+地区+分类+关键词）
     */
    public PageResponse<Services> getServicePage(ServicePageRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userMapper.selectById(userId);

        LambdaQueryWrapper<Services> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Services::getDeleted, 0)
                .eq(Services::getStatus, 1);

        // 按用户地区筛选
        if (user.getProvince() != null && !user.getProvince().isEmpty()) {
            wrapper.eq(Services::getProvince, user.getProvince());
        }
        if (user.getCity() != null && !user.getCity().isEmpty()) {
            wrapper.eq(Services::getCity, user.getCity());
        }
        if (user.getDistrict() != null && user.getDistrict().isEmpty()) {
            wrapper.eq(Services::getDistrict, user.getDistrict());
        }

        // 分类筛选
        if (request.getCategoryId() != null) {
            wrapper.eq(Services::getCategoryId, request.getCategoryId());
        }

        // 关键词筛选
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.like(Services::getName, request.getKeyword());
        }

        wrapper.orderByDesc(Services::getCreateTime);

        Page<Services> page = new Page<>(request.getCurrent(), request.getSize());
        Page<Services> resultPage = servicesMapper.selectPage(page, wrapper);

        // 关联分类和商家名称
        List<Services> records = resultPage.getRecords();
        for (Services service : records) {
            // 关联分类名称
            ServiceCategory category = categoryMapper.selectById(service.getCategoryId());
            if (category != null) {
                service.setCategoryName(category.getName());
            }
            // 关联商家名称
            ServiceMerchant merchant = merchantMapper.selectById(service.getMerchantId());
            if (merchant != null) {
                service.setMerchantName(merchant.getName());
            }
        }

        return new PageResponse<>(
                resultPage.getCurrent(),
                resultPage.getSize(),
                resultPage.getTotal(),
                resultPage.getPages(),
                records
        );
    }

    /**
     * 服务详情接口
     */
    public Services getServiceDetail(Integer id) {
        Services service = servicesMapper.selectOne(
                new LambdaQueryWrapper<Services>()
                        .eq(Services::getId, id)
                        .eq(Services::getDeleted, 0)
                        .eq(Services::getStatus, 1)
        );
        if (service != null) {
            // 关联分类和商家名称
            ServiceCategory category = categoryMapper.selectById(service.getCategoryId());
            if (category != null) {
                service.setCategoryName(category.getName());
            }
            ServiceMerchant merchant = merchantMapper.selectById(service.getMerchantId());
            if (merchant != null) {
                service.setMerchantName(merchant.getName());
            }
        }
        return service;
    }


}
