package top.xym.community.app.module.conversation.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SessionStatusEnum {
    ACTIVE(0, "进行中"),
    USER_CLOSED(1, "用户关闭"),
    MERCHANT_CLOSED(2, "商家关闭");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    public static SessionStatusEnum fromCode(Integer code) {
        if (code == null) return null;
        for (SessionStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}