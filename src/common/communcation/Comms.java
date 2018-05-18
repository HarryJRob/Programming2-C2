package common.communcation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

//Provides the ability to send messages between sockets (Really really inflexible though)
public abstract class Comms {

	public static int PORT_NUM = 2004;
	
	public synchronized static boolean sendMessage(Message message, Socket socket) {
		try {
			new ObjectOutputStream(socket.getOutputStream()).writeObject(message);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public synchronized static Message recieveMessage(Socket socket) {
		try {
			return (Message) new ObjectInputStream(socket.getInputStream()).readObject();
		} catch (ClassNotFoundException | IOException e) {
			try {
				socket.close();
			} catch (IOException e1) { }
			return null;
		}
	}
}
