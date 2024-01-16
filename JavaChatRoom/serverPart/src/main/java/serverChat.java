import Common.Message;
import Common.User;

import java.io.ObjectInputStream;

import java.net.ServerSocket;
import java.net.Socket;

/**********
 * This thread is used to get messages and provide the messages to middle machine.
 */
public class serverChat extends Thread {
    public Message m;

    @Override
    public void run() {
        User sender = serverLogin.sender; // get the certain port to send back the message
        int portSender = sender.getPort();
        try {
            ServerSocket senderServer = new ServerSocket(portSender);

            // This thread is used to get messages from users, before the middle machine get the message
            Thread send = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true)
                        try {
                            // set up the communication socket
                            Socket keep = senderServer.accept();
                            ObjectInputStream ois = new ObjectInputStream(keep.getInputStream());
                            m = (Message) ois.readObject();

                            System.out.println(m.getSender().getUserID() + "said: ");
                            System.out.println("        " + m.getContent());
                            System.out.println("to" + m.getGetter().getUserID() + "at" + m.getSendTime());

                            serverLogin.lock.lock();
                            MiddleMachine.messageNum++;
                            MiddleMachine.messages[MiddleMachine.messageNum] = m;
                            try {
                                serverLogin.renew = true;
                                serverLogin.condition.signal();
                            } finally {
                                serverLogin.lock.unlock();
                            }
                            ois.close();
                            // now, middle machine can get the message using static variable and save the message
                            // then the messages can be sent to the getter
                        } catch (Exception e) {
                            System.out.println("Nothing found.");
                            e.printStackTrace();
                        }
                }
            });
            send.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}