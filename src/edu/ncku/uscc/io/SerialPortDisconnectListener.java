package edu.ncku.uscc.io;

public interface SerialPortDisconnectListener {
	void onDisconnectEvent(String portName, int room);
}
