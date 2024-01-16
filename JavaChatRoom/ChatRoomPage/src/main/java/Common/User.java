package Common;

import java.io.Serializable;

/**********
 * This is user class.
 * It includes several attributes
 *      1. userID: String.
 *      2. userName: String, used as the nickname.
 *      3. userPwd: String, used as password.
 *      4. userPhoNum: String, used as to record user's gender.
 *      5. port: int, used to record the port.
 *      6. ProfilePicture: byte[] used to record the profile picture.
 *      7. Address: String.
 *      8. Age: String.
 */
public class User implements Serializable {
    private static final long serialVersionUID = -66249083649811247L;
    private String userID;
    private String userName;
    private String userPwd;
    private String userPhoNum;
    private int port;
    private byte[] ProfilePicture;
    private String Age;
    private String Address;

    public void setAddress(String address) {
        Address = address;
    }

    public void setAge(String age) {
        Age = age;
    }

    public String getAddress() {
        return Address;
    }

    public String getAge() {
        return Age;
    }

    public void setProfilePicture(byte[] profilePicture) {
        ProfilePicture = profilePicture;
    }

    public byte[] getProfilePicture() {
        return ProfilePicture;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public User(){
        this.userID = null;
        this.userPwd = null;
        this.userName = null;
        this.userPhoNum = null;
    }
    public User(String userID, String userPwd){
        this.userID = userID;
        this.userPwd = userPwd;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserPhoNum(String userPhoNum) {
        this.userPhoNum = userPhoNum;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPhoNum() {
        return userPhoNum;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public User(String userID, String userName, String userPwd, String userPhoNum){
        this.userID = userID;
        this.userPwd = userPwd;
        this.userName = userName;
        this.userPhoNum = userPhoNum;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }
}
