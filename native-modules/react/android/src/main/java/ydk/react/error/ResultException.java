package ydk.react.error;

public class ResultException extends Exception {
    private String code;

    private Object object;

    public ResultException(String code, String message) {
        super(message);
        this.code = code;
    }

    public ResultException(String code, String message, Object object) {
        super(message);
        this.code = code;
        this.object = object;
    }

    public Object getObject() {
        return object;
    }

    public String getCode() {
        return code;
    }
}
