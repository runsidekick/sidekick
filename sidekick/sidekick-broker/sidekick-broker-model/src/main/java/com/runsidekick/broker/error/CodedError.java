package com.runsidekick.broker.error;

/**
 * @author serkan.ozal
 */
public class CodedError {

    private final int code;
    private final String messageTemplate;

    public CodedError(int code, String messageTemplate) {
        this.code = code;
        this.messageTemplate = messageTemplate;
    }

    public int getCode() {
        return code;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public String formatMessage(Object... args) {
        return String.format(messageTemplate, args);
    }

}
