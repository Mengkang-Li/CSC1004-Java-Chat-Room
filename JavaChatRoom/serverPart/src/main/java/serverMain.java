import Common.Message;
import Common.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**********
 * This class is the main function of the server part. It is the entrance of the server.
 * It mainly starts register and login server part.
 */
public class serverMain {
    public static ServerSocket serverSocket; // Use 5210 to accept the message that symbol either login or register.
    public static ServerSocket ChatSocket; // Use 9876 to accept the 'keep' socket to keep the communication.

    static {
        try {
            ChatSocket = new ServerSocket(9876);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            serverSocket = new ServerSocket(5210);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
        // initialize the database ( create the user sheet )
        JdbcConnect jdbcConnect = new JdbcConnect();
        Connection connection = jdbcConnect.getConnection();
        Statement statement = connection.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS `users` (" +
                "  `ID` varchar(255) NOT NULL," +
                "  `userName` varchar(255) DEFAULT NULL," +
                "  `pwd` varchar(255) DEFAULT NULL," +
                "  `PhoNum` varchar(255) DEFAULT NULL," +
                "`Age` varchar(255) DEFAULT NULL," +
                "`Address` varchar(255) DEFAULT NULL," +
                "  `profilepicture` longblob," +
                "  PRIMARY KEY (`ID`)" +
                ")";
        statement.executeUpdate(sql);
        // start the register and login server.
        serverRegister serverRegister = new serverRegister();
        serverRegister.start();

        serverLogin serverLogin = new serverLogin();
        serverLogin.start();
        // accept the direction message
        while (true) {
            Socket socket = serverSocket.accept();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Message msg = (Message) ois.readObject();

            // This method is used to add different operations when receiving different directions.
            Thread entrance = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        if (msg.getMsgType().equals(MessageType.MESSAGE_REGISTER) ) {

                        }
                        if (msg.getMsgType().equals(MessageType.MESSAGE_LOGIN)) {

                        }
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            if (!msg.getMsgType().equals(MessageType.MESSAGE_DEFAULT)) {
                entrance.start();
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                msg.setMsgType(MessageType.MESSAGE_DEFAULT);
            }
        }

    }
}

/*  Wrong trial.
    private class MiddleThread extends Thread {
        // 每产生一个新的m 就要刷新一下m 但是不想再产生新线程
        // 写一个传输m的方法 一旦m不一样 getM 这样的话 主页面就要到这个里了 因为需要和聊天的线程在一块儿
        private Message m;

        public MiddleThread(Message m) {
            this.m = m;
        }

        @Override
        public void run() {
            // 写在方法内 不断调用getM 如果不同 就进行发送
            int middlePort = 9988;
            try {
                ServerSocket Middle = new ServerSocket(middlePort);
                // 一个ServerSocket 可以和多个socket建立连接
                while (true) {
                    Socket s = Middle.accept();
                    ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                    oos.writeObject(m);
                    Thread.sleep(100);
                    oos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/
