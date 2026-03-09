package top.xym.community.app.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "微信登录")
public class WxLoginDTO {
    @Schema(description = "微信登录凭证")
    private String code;
    @Schema(description = "加密数据")
    private String encryptedData;
    @Schema(description = "偏移量")
    private String iv;
}
