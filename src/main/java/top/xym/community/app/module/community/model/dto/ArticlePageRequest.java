package top.xym.community.app.module.community.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ArticlePageRequest", description = "文章分页查询请求")
public class ArticlePageRequest {

    @Schema(description = "标签ID（可选）")
    private Integer tagId;

    @Schema(description = "当前页码，默认1")
    private Long current = 1L;

    @Schema(description = "每页条数，默认10")
    private Long size = 10L;
}