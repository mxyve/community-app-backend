package top.xym.community.app.module.conversation.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MsgTypeEnum {
    TEXT(0, "文本"),
    IMAGE(1, "图片"),
    VOICE(2, "语音"),
    VIDEO(3, "视频");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    public static MsgTypeEnum fromCode(Integer code) {
        if (code == null) return null;
        for (MsgTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}