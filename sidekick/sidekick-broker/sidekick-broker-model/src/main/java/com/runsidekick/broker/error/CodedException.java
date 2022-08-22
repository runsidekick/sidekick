package com.runsidekick.broker.error;

/**
 * @author ozge.lule
 */
public class CodedException extends RuntimeException {

    private final int code;

    public CodedException(int code, String message) {
        super(message);
        this.code = code;
    }

    public CodedException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public CodedException(CodedError codedError, Object... args) {
        super(codedError.formatMessage(args));
        this.code = codedError.getCode();
    }

    public CodedException(CodedError codedError, Throwable cause, Object... args) {
        super(codedError.formatMessage(args), cause);
        this.code = codedError.getCode();
    }

    public int getCode() {
        return code;
    }

}
