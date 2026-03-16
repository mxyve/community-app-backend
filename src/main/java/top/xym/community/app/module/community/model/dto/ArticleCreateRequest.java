package top.xym.community.app.module.community.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "ArticleCreateRequest", description = "创建文章请求")
public class ArticleCreateRequest {

    @NotBlank(message = "文章标题不能为空")
    @Schema(description = "文章标题")
    private String title;

    @NotBlank(message = "文章内容不能为空")
    @Schema(description = "文章内容")
    private String content;

    @Schema(description = "图片URL，多个用逗号分隔")
    private String img;

    @Schema(description = "标签ID")
    private Integer tagId;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "区域")
    private String area;
}