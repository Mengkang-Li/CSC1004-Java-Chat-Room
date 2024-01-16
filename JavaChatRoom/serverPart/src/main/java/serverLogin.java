import Common.Message;
import Common.MessageType;
import Common.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**********
 * This part is used to log in.
 * It mainly includes:
 *      1. judge whether the user has logged in.
 *      2. judge whether the user is legal (at password and id).
 *      3. send back the message whether the user log in successfully.
 *      4. send back the user's information, such as user name and picture profile.
 */
public class serverLogin extends Thread {
    // used to set the storing message start after getting messages
    public static final Lock lock = new ReentrantLock();
    public static final Condition condition = lock.newCondition();

    public static boolean renew = false;

    public static User sender = new User();
    public Message msg = new Message();
    ServerSocket serverSocket;



    @Override
    public void run() {
        // 9999 used to receive login or register request
        try{
            serverSocket = new ServerSocket(9999);
        } catch (Exception e){
            e.printStackTrace();
        }
        while (true) {
            try {
                if (serverSocket.isClosed())
                    serverSocket = new ServerSocket(9999);
                boolean whetherLogin = false;
                // judge whether the user has been login successfully
                for (Map.Entry<String, Socket> entry : MiddleMachine.socketMap.entrySet()) {
                    if (entry.getKey().equals(sender.getUserID())) {
                        whetherLogin = true;
                        break;
                    }
                }

                // get the user's information
                Socket loginS = serverSocket.accept();
                ObjectInputStream ois = new ObjectInputStream(loginS.getInputStream());
                sender = (User) ois.readObject();
                // connect to the database and check whether the user is legal
                JdbcConnect jdbcConnect = new JdbcConnect();
                jdbcConnect.getConnection();
                String sql = "select * from `users` where ID = ? and pwd = ?";
                List<Object> params = new ArrayList<>();
                params.add(sender.getUserID());
                params.add(sender.getUserPwd());
                try {
                    Map<String, Object> map = jdbcConnect.findSimpleResult(sql, params);
                    if (map.isEmpty() || whetherLogin) {
                        msg.setMsgType(MessageType.MESSAGE_LOGIN_FAIL);
                    } else {
                        // this will execute if login successfully
                        msg.setMsgType(MessageType.MESSAGE_LOGIN_SUCCEED);
                        // launch the middle machine send back to save the socket with the corresponding user
                        MiddleMachineSendBack mms = new MiddleMachineSendBack();
                        mms.start();
                        serverChat chat = new serverChat();
                        chat.start();
                        // This thread is used to launch the thread that can send back the messages
                        Thread setting = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (true) {
                                    serverLogin.lock.lock();
                                    try {
                                        while (!serverLogin.renew) {
                                            serverLogin.condition.await();
                                        }
                                        if (serverLogin.renew) {
                                            MiddleMachine middleMachine = new MiddleMachine();
                                            middleMachine.start();
                                            // sleep for 100ms to avoid repeated GetM thread
                                            Thread.sleep(100);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        serverLogin.lock.unlock();
                                    }
                                }
                            }
                        });
                        setting.start();
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                // send back the message to inform whether the user login successfully
                ObjectOutputStream oos = new ObjectOutputStream(loginS.getOutputStream());
                oos.writeObject(msg);
                if (msg.getMsgType() == MessageType.MESSAGE_LOGIN_FAIL) {
                    ois.close();
                    oos.close();
                    loginS.close();
                    continue;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                oos.reset();
                // These codes are used to update the profile picture and the user's other info
                // For example, the user only type in id and password, the codes send back the corresponding information
                sql = "SELECT * FROM users WHERE ID = ?";
                PreparedStatement ps = jdbcConnect.getTheConnection().prepareStatement(sql);
                ps.setString(1, sender.getUserID());
                ResultSet rs = ps.executeQuery();
                // send back this user
                User user = new User();
                if (rs.next()) {
                    if (rs.getBytes("ProfilePicture") != null) {
                        byte[] mid = rs.getBytes("ProfilePicture");

                        user.setProfilePicture(mid);
                    }

                    user.setPort(sender.getPort());

                    user.setUserName(rs.getString("userName"));
                    user.setUserPhoNum(rs.getString("PhoNum"));
                    user.setUserID(rs.getString("ID"));
                    user.setUserPwd(rs.getString("pwd"));
                }
                Socket socket2 = serverSocket.accept();
                ObjectOutputStream oos2 = new ObjectOutputStream(socket2.getOutputStream());
                oos2.writeObject(user);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                oos2.close();
                ois.close();
                loginS.close();
                socket2.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // This method is used to test whether the port is available.
    boolean PortIsAvailable(int port) {
        boolean result = true;
        try {
            ServerSocket socket = new ServerSocket(port);
            socket.close();
        } catch (Exception e) {
            result = false;
        }
        return result;
    }
}
