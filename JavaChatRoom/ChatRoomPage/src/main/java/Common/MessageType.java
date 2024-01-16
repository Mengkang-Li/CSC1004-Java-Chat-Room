package Common;

/**********
 * This is message type.
 */
public interface MessageType {
    String MESSAGE_LOGIN_SUCCEED = "1";
    String MESSAGE_LOGIN_FAIL = "2";
    String MESSAGE_MISS = "3";
    String MESSAGE_LOGIN = "L"; // launch the login thread in the server
    String MESSAGE_REGISTER = "R"; // launch the register thread in the server
    String MESSAGE_DEFAULT = "D";
    String MESSAGE_CONTENT = "M";
    String MESSAGE_PROFILEPICTURE = "P";
    String MESSAGE_SENDANIMAGE ="I";
    String MESSAGE_SENDAVIDEO = "S";
    String MESSAGE_HISTORY = "H";
}
