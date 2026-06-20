package com.YD0304.sushi_shop.dto;

public class StatusResponse {
    int code;
    String msg;

    public StatusResponse(int code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public int getCode() { return code; }
    public String getMsg() { return msg; }


}
