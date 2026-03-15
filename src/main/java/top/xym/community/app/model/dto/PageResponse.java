package top.xym.community.app.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "通用分页响应DTO")
public class PageResponse<T> {

    @Schema(description = "当前页码（从1开始）")
    private Long current;

    @Schema(description = "每页条数")
    private Long size;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "总页数")
    private Long pages;

    @Schema(description = "当前页数据列表")
    private List<T> records;

    /**
     * 快速构建分页响应（适配 MyBatis-Plus 的 Page 对象）
     */
    public static <T> PageResponse<T> build(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page) {
        return new PageResponse<>(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getPages(),
                page.getRecords()
        );
    }

}
