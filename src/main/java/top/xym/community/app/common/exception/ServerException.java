package top.xym.community.app.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;
@Getter
public class ServerException extends RuntimeException{

    @Serial
    private static final long serialVersionUID = 1L;

    private final int code;

    // 快速抛出「默认错误码 + 自定义提示信息」的异常
    public ServerException(String message) {
        super(message);
        this.code = ResultCode.FAIL.getCode();
    }

    // 抛出「自定义错误码 + 自定义提示信息」的异常
    public ServerException(int code, String message) {
        super(message);
        this.code = code;
    }

    // 抛出「枚举定义的标准错误码 + 标准提示信息」的异常
    public ServerException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }
}
