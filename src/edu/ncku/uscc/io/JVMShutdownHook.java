package edu.ncku.uscc.io;

import gnu.io.SerialPort;

class JVMShutdownHook extends Thread {
	private SerialPort serialPort;

	public JVMShutdownHook(SerialPort serialPort) {
		this.serialPort = serialPort;
	}

	public void run() {
		serialPort.close();
	}
}