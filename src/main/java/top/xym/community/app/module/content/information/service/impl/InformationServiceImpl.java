package top.xym.community.app.module.tenant.information.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import top.xym.community.app.module.tenant.information.mapper.InformationMapper;
import top.xym.community.app.module.tenant.information.model.entity.Information;
import top.xym.community.app.module.tenant.information.service.InformationService;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class InformationServiceImpl extends ServiceImpl<InformationMapper, Information>
        implements InformationService {

    private final InformationMapper informationMapper;

    /**
     * 前台分页查询已启用的资讯（支持地区匹配）
     */
    @Override
    public IPage<Information> frontPageList(Integer pageNum, Integer pageSize, String title,
                                            String province, String city, String district) {
        LambdaQueryWrapper<Information> wrapper = new LambdaQueryWrapper<>();

        // 只查询已启用的资讯
        wrapper.eq(Information::getStatus, 1)
                .eq(Information::getDeleted, 0);

        // 标题模糊搜索
        if (title != null && !title.isEmpty()) {
            wrapper.like(Information::getTitle, title);
        }

        // 构建地区匹配条件
        if (StringUtils.isNotBlank(province)) {
            // 创建一个要匹配的地区规则列表
            List<String> matchPatterns = new ArrayList<>();

            matchPatterns.add("全国");

            // 按用户位置精度拼接
            if (StringUtils.isNotBlank(district)) {
                matchPatterns.add(province + "-" + city + "-" + district);
            } else if (StringUtils.isNotBlank(city)) {
                matchPatterns.add(province + "-" + city);
            } else {
                matchPatterns.add(province);
            }

            // 拼接 OR 条件
            wrapper.and(w -> {
                for (String pattern : matchPatterns) {
                    w.or(i -> i.apply(
                            "JSON_CONTAINS(service_area, {0}, '$')",
                            "\"" + pattern + "\""
                    ));
                }
            });
        }

        // 按排序升序，创建时间倒序
        wrapper.orderByAsc(Information::getSort)
                .orderByDesc(Information::getCreateTime);

        Page<Information> page = new Page<>(pageNum, pageSize);
        return this.page(page, wrapper);
    }

    /**
     * 前台获取资讯详情（同时增加浏览量）
     */
    @Override
    public Information frontGetDetail(Integer id) {
        // 查询资讯（必须是已启用的）
        LambdaQueryWrapper<Information> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Information::getId, id)
                .eq(Information::getDeleted, 0)
                .eq(Information::getStatus, 1);

        Information information = this.getOne(wrapper);

        if (information != null) {
            // 增加浏览量
            informationMapper.incrementViewCount(id);
            // 返回更新后的浏览量
            information.setViewCount(information.getViewCount() + 1);
        }

        return information;
    }
}