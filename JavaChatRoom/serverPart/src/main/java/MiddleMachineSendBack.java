import Common.User;

import java.io.ObjectInputStream;
import java.net.Socket;

/**********
 * This class is used to save onsite users with their sockets.
 */
public class MiddleMachineSendBack extends Thread{
    @Override
    public void run() {
        try {
            Socket Keep = serverMain.ChatSocket.accept();
            ObjectInputStream ois = new ObjectInputStream(Keep.getInputStream());
            User m = (User) ois.readObject();

            MiddleMachine.socketMap.put(m.getUserID(), Keep);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
