package group.bison.data.data_layer.soul_api.domain.model;

import java.io.Serializable;

public class CommonDataResult implements Serializable {
    private static final long serialVersionUID = -1L;

    private Integer errorCode;

    private String errorMsg;

    private Object content;

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
