package edu.ncku.uscc.io;

public interface SerialPortConnectListener {
	void onConnectEvent(String portName, int room);
}
