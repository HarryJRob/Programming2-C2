package common.communcation;

import java.io.Serializable;

//A message used to communicate between the client and server
public class Message implements Serializable {

	private static final long serialVersionUID = -5972560872137084464L;
	
	private String messageType;
	private Object messageContents;
	
	public Message(String messageType, Object messageContents) {
		this.messageType = messageType;
		this.messageContents = messageContents;
	}
	
	public String getMessageType() {
		return messageType;
	}
	
	public Object getMessageContents() {
		return messageContents;
	}
}