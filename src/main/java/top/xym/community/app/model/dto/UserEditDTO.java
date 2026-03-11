package top.xym.community.app.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户修改dto")
public class UserEditDTO {
    @Schema(description = "主键")
    private Integer userId;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "性别")
    private String gender;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "区县")
    private String district;

}
