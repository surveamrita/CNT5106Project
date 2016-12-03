import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class MessageBean implements Serializable{
	private String clientID;
	private String messageMode;
	private boolean isTextMessage;
	private boolean isFile;
	private String textMessage;
	private byte[] fileBytes;
	private String fileName;
	private ArrayList<String> clientList;
	
	public MessageBean(){}
	
	public MessageBean(String clientID, String messageMode,
			boolean isTextMessage, boolean isFile, String textMessage, byte[] fileBytes, String fileName) {
		super();
		this.clientID = clientID;
		this.messageMode = messageMode;
		this.isTextMessage = isTextMessage;
		this.isFile = isFile;
		this.textMessage = textMessage;
		this.fileBytes = fileBytes;
		this.fileName = fileName;
		clientList =  new ArrayList<String>();
	}
	public String getClientID() {
		return clientID;
	}
	public void setClientID(String clientID) {
		this.clientID = clientID;
	}
	public String getMessageMode() {
		return messageMode;
	}
	public void setMessageMode(String messageMode) {
		this.messageMode = messageMode;
	}
	public boolean isTextMessage() {
		return isTextMessage;
	}
	public void setTextMessage(boolean isTextMessage) {
		this.isTextMessage = isTextMessage;
	}
	public boolean isFile() {
		return isFile;
	}
	public void setFile(boolean isFile) {
		this.isFile = isFile;
	}
	public String getTextMessage() {
		return textMessage;
	}
	public void setTextMessage(String textMessage) {
		this.textMessage = textMessage;
	}
	public byte[] getFileBytes() {
		return fileBytes;
	}
	public void setFileBytes(byte[] fileBytes) {
		this.fileBytes = fileBytes;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public ArrayList<String> getClientList() {
		return clientList;
	}

	public void setClientList(ArrayList<String> clientList) {
		this.clientList = clientList;
	}
		
}
