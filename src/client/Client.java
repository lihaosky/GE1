package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for master to download data and send result to client
 * @author lihao
 *
 */
public interface Client extends Remote {
	public byte[] uploadData() throws RemoteException;
	public int downloadResult() throws RemoteException;
}
