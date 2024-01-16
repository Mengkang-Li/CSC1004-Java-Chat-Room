import Common.Message;
import Common.MessageType;
import Common.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**********
 * This class is the register server part.
 * It mainly includes:
 *      1. Receive user's information in the register part.
 *      2. Judge whether it's valid to add into the database.
 *      3. Add the valid user info into the database.
 *      4. Send back the message to say whether it registers successfully.
 */


public class serverRegister extends Thread {
    public Message msg = new Message();
    ServerSocket serverSocket; // Use 9998 to receive the register information.

    {
        try {
            serverSocket = new ServerSocket(9998);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // This thread
    @Override
    public void run() {
        while (true) {
            try {
                if (serverSocket.isClosed()) {
                    serverSocket = new ServerSocket(9998);
                }
                // receive the user information of register part
                Socket registerS = serverSocket.accept();
                ObjectInputStream ois = new ObjectInputStream(registerS.getInputStream());
                User u = (User) ois.readObject();
                List<Object> params = new ArrayList<>();
                params.add(u.getUserID());
                params.add(u.getUserName());
                params.add(u.getUserPwd());
                params.add(u.getUserPhoNum());
                params.add(u.getAge());
                params.add(u.getAddress());
                // add the user information into the database
                JdbcConnect jdbcConnect = new JdbcConnect();
                jdbcConnect.getConnection();
                String sql = "insert into `users`(ID, userName, pwd, PhoNum, Age, Address) " +
                        "values(?, ?, ?, ?, ?, ?)";

                // Test whether the user information is valid to add. (avoid repeated id and can be added to the database)
                boolean b;
                try {
                    b = RepeatedUserTest(u) && jdbcConnect.updateByPreparedStatement(sql, params);
                } catch (Exception e){
                    b = false;
                }
                // set the msg type
                if (b) {
                    msg.setMsgType(MessageType.MESSAGE_LOGIN_SUCCEED);
                } else {
                    msg.setMsgType(MessageType.MESSAGE_LOGIN_FAIL);
                }
                // send the message back to say whether the register is successful
                ObjectOutputStream oos = new ObjectOutputStream(registerS.getOutputStream());
                oos.writeObject(msg);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ois.close();
                oos.close();
                registerS.close();
                serverSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // This function is used to test whether the id has been registered.
    boolean RepeatedUserTest(User u) {
        boolean b = true;
        JdbcConnect jdbcConnect = new JdbcConnect();
        jdbcConnect.getConnection();
        String sql = "select * from `users` where ID = ? and pwd = ?";
        List<Object> list = new ArrayList<>();
        list.add(u.getUserID());
        list.add(u.getUserPwd());
        try {
            Map<String, Object> m = jdbcConnect.findSimpleResult(sql, list);
            if (!m.isEmpty()) {
                b = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return b;
    }
}
