package org.apache.atlas.common.exception;

/**
 * bridge的异常公共处理
 *
 * @author ZhangSanFeng 0051
 * @version 1.0.0
 * @company DTDream
 * @date 2015-11-04 14:13
 */

public class BridgeException extends RuntimeException {
    public BridgeException() {
    }

    public BridgeException(String message) {
        super(message);
    }

    public BridgeException(String message, Throwable cause) {
        super(message, cause);
    }

    public BridgeException(Throwable cause) {
        super(cause);
    }
}
