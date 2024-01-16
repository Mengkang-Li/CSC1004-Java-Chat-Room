package Common;

/**********
 * This is message type.
 */
public interface MessageType {
    String MESSAGE_LOGIN_SUCCEED = "1"; // 表示登录成功
    String MESSAGE_LOGIN_FAIL = "2"; // 表示登录失败
    String MESSAGE_MISS = "3"; // 表示用户不存在
    String MESSAGE_LOGIN = "L"; // 表示唤醒登录操作
    String MESSAGE_REGISTER = "R"; // 表示唤醒注册操作
    String MESSAGE_DEFAULT = "D"; // 表示未收到信息
    String MESSAGE_CONTENT ="T";
    String MESSAGE_PROFILEPICTURE = "P";
    String MESSAGE_SENDANIMAGE ="I";
    String MESSAGE_SENDAVIDEO = "S";
    String MESSAGE_HISTORY = "H";
}
