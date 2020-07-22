package info.joyc.core.exception;

/**
 * info.joyc.core.exception.CustomException.java
 * ==============================================
 * Copy right 2015-2018 by http://www.joyc.info
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : qiuzh
 * @version : v1.0.0
 * @desc : 自定义异常类
 * @since : 2018-02-24 15:14
 */
public class CustomException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -9088550254960091164L;

	/**
     * 自定义业务编码
     */
    private String code;

    /**
     * 自定义业务异常信息
     */
    private String Message;

    public CustomException(String code, String Message) {
        this.code = code;
        this.Message = Message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        this.Message = message;
    }
}
