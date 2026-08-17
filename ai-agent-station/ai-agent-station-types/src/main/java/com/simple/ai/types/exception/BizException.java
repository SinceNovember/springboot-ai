package com.simple.ai.types.exception;

import java.io.Serial;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务异常
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/9/2 07:10
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BizException extends RuntimeException{

    @Serial
    private static final long serialVersionUID = 5317680961212299217L;

    /** 异常码 */
    private String code;

    /** 异常信息 */
    private String info;

    public BizException(String code) {
        this.code = code;
    }

    public BizException(String code, Throwable cause) {
        this.code = code;
        super.initCause(cause);
    }

    public BizException(String code, String message) {
        this.code = code;
        this.info = message;
    }

    public BizException(String code, String message, Throwable cause) {
        this.code = code;
        this.info = message;
        super.initCause(cause);
    }

    @Override
    public String toString() {
        return "com.simple.ai.types.exception.BizException{" +
                "code='" + code + '\'' +
                ", info='" + info + '\'' +
                '}';
    }
    
}
