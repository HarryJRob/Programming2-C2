package common.communcation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Comms {

	public static int PORT_NUM = 2004;
	
	public static boolean sendMessage(Message message, Socket socket) {
		try {
			new ObjectOutputStream(socket.getOutputStream()).writeObject(message);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public static Message recieveMessage(Socket socket) {
		try {
			return (Message) new ObjectInputStream(socket.getInputStream()).readObject();
		} catch (ClassNotFoundException | IOException e) {
			return null;
		}
	}
}
