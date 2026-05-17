package top.xym.community.app.module.tenant.information.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import top.xym.community.app.module.tenant.information.model.entity.Information;

public interface InformationService extends IService<Information> {

    /**
     * 前台分页查询已启用的资讯（支持地区匹配）
     */
    IPage<Information> frontPageList(Integer pageNum, Integer pageSize, String title,
                                     String province, String city, String district);

    /**
     * 前台获取资讯详情（同时增加浏览量）
     */
    Information frontGetDetail(Integer id);
}