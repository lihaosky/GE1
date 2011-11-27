package slave;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Heartbeat extends Thread {
	public void run() {
		try {
			ServerSocket ss = new ServerSocket(common.Parameters.HBport);
			while (true) {
				Socket s = ss.accept();
				HeartbeatHandler hbHandler = new HeartbeatHandler(s);
				hbHandler.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
