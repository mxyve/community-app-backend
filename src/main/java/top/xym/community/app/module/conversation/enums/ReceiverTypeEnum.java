package top.xym.community.app.module.conversation.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReceiverTypeEnum {
    USER(0, "用户"),
    MERCHANT(1, "商家");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    public static ReceiverTypeEnum fromCode(Integer code) {
        if (code == null) return null;
        for (ReceiverTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}