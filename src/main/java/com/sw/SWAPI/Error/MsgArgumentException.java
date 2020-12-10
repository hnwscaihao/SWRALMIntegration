package com.sw.SWAPI.Error;

/**
 * @author: liuxiaoguang
 * @Date: 2020/7/16 15:28
 * @Description: 异常信息类
 */
public class MsgArgumentException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    protected final String status;
    protected final String message;

    public MsgArgumentException(String status, String message) {
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