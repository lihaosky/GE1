package slave;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import common.command.Command;

public class HeartbeatHandler extends Thread {
	private Socket s;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	
	public HeartbeatHandler(Socket s) {
		this.s = s;
		try {
			oos = new ObjectOutputStream(s.getOutputStream());
			ois = new ObjectInputStream(s.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		try {
			while (true) {
					Command cmd = (Command)ois.readObject();
					if (cmd.commandID == Command.PingCommand) {
						oos.writeObject(new Command(Command.PingAck));
					}
			}
		} catch (SocketException e) {
			System.out.println("Server closed socket!");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				ois.close();
				oos.close();
				s.close();
				System.out.println("Finish heartbeat!");
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
