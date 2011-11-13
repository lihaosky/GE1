package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Client extends Remote {
	public byte[] uploadData() throws RemoteException;
	public void downloadResult() throws RemoteException;
}
