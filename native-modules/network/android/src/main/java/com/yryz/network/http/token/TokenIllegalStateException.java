package com.yryz.network.http.token;

public class TokenIllegalStateException extends IllegalStateException {

    private String code;

    private Object object;

    public TokenIllegalStateException(String s, String code) {
        super(s);
        this.code = code;
    }

    public TokenIllegalStateException(String s, String code, Object object) {
        super(s);
        this.code = code;
        this.object = object;
    }

    public String getCode() {
        return code;
    }

    public Object getObject() {
        return object;
    }
}
