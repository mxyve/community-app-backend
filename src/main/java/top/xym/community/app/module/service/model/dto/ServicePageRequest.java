package top.xym.community.app.module.service.model.dto;

import lombok.Data;

/**
 * 生活助手服务分页请求参数
 */
@Data
public class ServicePageRequest {

    /**
     * 当前页码
     */
    private Long current = 1L;

    /**
     * 每页条数
     */
    private Long size = 10L;

    /**
     * 分类ID（筛选用）
     */
    private Integer categoryId;

    /**
     * 搜索关键词（服务名称）
     */
    private String keyword;
}