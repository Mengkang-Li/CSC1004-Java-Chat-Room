public class User {
    private String userID;
    private String userName;
    private String userPwd;
    private String userPhoNum;
    public User(String userID, String userName, String userPwd, String userPhoNum){
        this.userID = userID;
        this.userName = userName;
        this.userPwd = userPwd;
        this.userPhoNum = userPhoNum;
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
}
