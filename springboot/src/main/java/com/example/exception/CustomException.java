package com.example.exception;

public class CustomException extends RuntimeException {
    private String msg;

    public CustomException(String msg) {
        super(msg);  // 添加这行
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
