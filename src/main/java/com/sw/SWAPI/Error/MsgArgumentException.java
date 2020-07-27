package com.sw.SWAPI.Error;

public class MsgArgumentException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    protected final String status;
    protected final String message;

    public MsgArgumentException(String message,String status) {
        this.message = message;
        this.status = status;
    }

    public MsgArgumentException(String message,String status, Throwable e) {
        super(message, e);
        this.message = message;
        this.status = status;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

}