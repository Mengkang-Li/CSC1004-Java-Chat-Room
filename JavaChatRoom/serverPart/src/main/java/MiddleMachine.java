import Common.Message;
import Common.MessageType;

import java.io.*;
import java.sql.*;

import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**********
 * This is a middle process of the server. It is mainly used to get the message from users
 *  and hand out the message to the getter
 *  It contains:
 *      1. GetM: used to
 */
public class MiddleMachine extends Thread {
    /* 分发器
     * 每个chat线程接收到消息之后创建一个这个对象？或是调用一下这个方法 然后对得到的m的getter进行遍历 找到那个线程的socket 然后发送
     */

    // 创建一个映射，将socket和对应的用户id进行映射 然后通过id找socket就可以了
    // 找到对应的socket之后 将所有信息收集到这里 然后进行相关发送 发送采用一个方法 需要使用serverLogin中的线程设计启动理念

    // 直接新建serverSocket 每一个连上的 都放到socketMap中 登录成功就连接
    public static Map<String, Socket> socketMap = new HashMap<>(); // used to store users and their socket
    public static int messageNum = -1; // get the messages
    public static Message[] messages = new Message[1000000]; // store the messages, so that it's easy to transfer

    // the thread transfer GetM thread.
    // A lock is added, so when the server get a message, the GetM thread starts.
    @Override
    public void run() {

        serverLogin.lock.lock();
        try {
            while (!serverLogin.renew) {
                serverLogin.condition.await();
            }
            if (serverLogin.renew) {
                GetM getM = new GetM();
                getM.start();
                // avoid repeated messages
                Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            serverLogin.lock.unlock();
        }
    }

    // The GetM thread: 1. process history element and add it to the message that is looking up message history.
    //                  2. update the profile picture for users
    //                  3. hand out the message to the right person
    //                  4. add the message to the database
    class GetM extends Thread {
        @Override
        public void run() {

            Message m = messages[messageNum];
            // process history element and add it to the message that is looking up message history.
            if (m.getMsgType().equals(MessageType.MESSAGE_HISTORY)) {
                // store the message history in the list history
                List<Object> history = new ArrayList<>();
                // connect the database
                JdbcConnect jdbcConnect = new JdbcConnect();
                Connection connection = jdbcConnect.getConnection();

                try {
                    // get all messages
                    String exe = "SELECT * FROM allMessage";
                    Statement stm = connection.createStatement();
                    ResultSet rs = stm.executeQuery(exe);
                    while (rs.next()) {
                        // add the info to the list
                        history.add("Send Time: " + rs.getString("sendTime"));
                        history.add("sender: " + rs.getString("sender"));
                        history.add(rs.getString("content"));
                        if (rs.getBytes("picture") != null) {
                            byte[] picture = rs.getBytes("picture");
                            history.add(picture);
                        } else {
                            history.add(null);
                        }
                        if (!rs.getString("video").equals("")) {
                            File file = new File(rs.getString("video"));
                            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                            byte[] video = streamToByteArray(bis);
                            history.add(video);
                        } else {
                            history.add(null);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                m.setHistory(history); // set the history
            }

            // update the profile picture for users
            if (m.getMsgType().equals(MessageType.MESSAGE_PROFILEPICTURE)) {
                // if the msg type is update the profile picture, there's no need to send out and add into the database
                JdbcConnect jdbcConnect = new JdbcConnect();
                jdbcConnect.getConnection();
                try {
                    byte[] mid = m.getPicture();
                    // use form like the sql sentence, or blob data may be stored as  its address value
                    String updateSql = "UPDATE users SET ProfilePicture = ? WHERE ID = ?";
                    PreparedStatement updateStatement = jdbcConnect.getTheConnection().prepareStatement(updateSql);
                    updateStatement.setBytes(1, mid);
                    updateStatement.setString(2, m.getSender().getUserID());
                    updateStatement.executeUpdate();

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                // hand out the message to the right person
            } else if (m.getMsgType().equals(MessageType.MESSAGE_SENDANIMAGE) || m.getMsgType().equals(MessageType.MESSAGE_SENDAVIDEO) || true) {
                String getterId = m.getGetter().getUserID();
                // if they are joining the chat room
                if (getterId.equals("all")) {
                    // send to all people that are onsite
                    socketMap.forEach((String, Socket) -> {
                        Socket aim = Socket;
                        try {
                            ObjectOutputStream oos = new ObjectOutputStream(aim.getOutputStream());
                            oos.writeObject(m);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    // if they are chatting privately
                } else {
                    Socket aim = socketMap.get(getterId);
                    if (!getterId.equals(m.getSender().getUserID())) {
                        // send to themselves to show the message on their own screen
                        Socket self = socketMap.get(m.getSender().getUserID());
                        try {
                            ObjectOutputStream oos = new ObjectOutputStream(self.getOutputStream());
                            oos.writeObject(m);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(aim.getOutputStream());
                        oos.writeObject(m);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                serverLogin.renew = false;
            }
            addIntoMySQL(m);
        }
    }

    // add the message to the database
    void addIntoMySQL(Message m) {
        // get connection to the database
        JdbcConnect jdbcConnect = new JdbcConnect();
        jdbcConnect.getConnection();
        if (!m.getMsgType().equals(MessageType.MESSAGE_PROFILEPICTURE))
            try {
                // get the message table name, using their id, the sequence is determined by the string value
                String user1 = m.getSender().getUserID();
                String user2 = m.getGetter().getUserID();
                String tableName = "";
                if (user1.compareTo(user2) > 0) {
                    tableName = "Message" + user1 + user2;
                } else {
                    tableName = "Message" + user2 + user1;
                }
                // if send to all, store to all message
                if (m.getGetter().getUserID().equals("all")) {
                    tableName = "allMessage";
                }
                Statement create = jdbcConnect.getTheConnection().createStatement();
                String createTheTable = "CREATE TABLE IF NOT EXISTS " + tableName +
                        " (sendTime VARCHAR(255), " +
                        "Sender VARCHAR(255), " +
                        "Getter VARCHAR(255), " +
                        "Content VARCHAR(255), " +
                        "Picture LONGBLOB, " +
                        "Video VARCHAR(255));";
                create.executeUpdate(createTheTable);
                String time = m.getSendTime();
                String sender = m.getSender().getUserName();
                String getter = m.getGetter().getUserName();
                String content = m.getContent();
                byte[] picture = m.getPicture();
                // the video is stored as the address on the local device (String)
                byte[] video = m.getVideo();
                String FileName = "";
                if (m.getVideo() != null) {
                    UUID uuid = UUID.randomUUID();
                    FileName = "./JavaChatRoom/serverPart/Videos/" + uuid + ".mp4";
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(FileName));
                    bos.write(video);
                    bos.close();
                }
                String add = "INSERT INTO " + tableName + " (sendTime, Sender, Getter, Content, Picture, Video) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement statement = jdbcConnect.getTheConnection().prepareStatement(add);
                statement.setString(1, time);
                statement.setString(2, sender);
                statement.setString(3, getter);
                statement.setString(4, content);
                statement.setBytes(5, picture);
                statement.setString(6, FileName);
                statement.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    // use the method to change an InputStream to a byte[] to store or deliver the video or pictures
    byte[] streamToByteArray(InputStream is) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int len;
        while ((len = is.read(b)) != -1) {
            bos.write(b, 0, len);
        }
        byte[] array = bos.toByteArray();
        bos.close();
        return array;
    }
}