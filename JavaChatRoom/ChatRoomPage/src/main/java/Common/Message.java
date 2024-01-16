package Common;

import java.io.Serializable;
import java.util.List;

/**********
 * This is message class. This is the main object transmitting in the chat process.
 * The message has several attributes
 *      1. sender: user object.
 *      2. getter: user object.
 *      3. sendTime: String.
 *      4. msgType: String, show what the message is used for.
 *      5. content: String, show the content of the message.
 *      6. picture: byte[], show the binary byte array of the picture.
 *      7. video: byte[], show the binary byte array of the video.
 *      8. history: including the history content if the type is history.
 */
public class Message implements Serializable {
    private User sender;
    private User getter;
    private String sendTime;
    private String msgType;
    private String content;
    private byte[] picture;
    private byte[] video;
    private List<Object> history;

    public List<Object> getHistory() {
        return history;
    }

    public void setHistory(List<Object> history) {
        this.history = history;
    }

    public byte[] getVideo() {
        return video;
    }

    public void setVideo(byte[] video) {
        this.video = video;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }
    public Message(byte[] b){
        this.picture = b;
    }


    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public Message(){
        msgType = MessageType.MESSAGE_DEFAULT;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public User getGetter() {
        return getter;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getSender() {
        return sender;
    }

    public void setGetter(User getter) {
        this.getter = getter;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }
}
