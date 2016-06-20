package edu.ncku.uscc.io;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusCoupler;
import com.ghgande.j2mod.modbus.net.ModbusTCPListener;
import com.ghgande.j2mod.modbus.procimg.*;

public class ModbusTCPSlave {

	private int num_registers;
	private int port = Modbus.DEFAULT_PORT;
	
	private SimpleProcessImage dataStore;
	private ModbusTCPListener slaveListener;

	public ModbusTCPSlave(int num_registers) {
		super();
		this.num_registers = num_registers;
	}

	public ModbusTCPSlave(int num_registers, int port) {
		super();
		this.num_registers = num_registers;
		this.port = port;
	}

	public synchronized void setRegister(int addr, int value)
			throws IllegalAddressException {
		this.dataStore.setRegister(addr, new SimpleRegister(value));
	}

	public synchronized int getResgister(int addr)
			throws IllegalAddressException {
		return this.dataStore.getRegister(addr).toUnsignedShort();
	}	

	@SuppressWarnings("deprecation")
	public void initialize() throws Exception {
		// 2. Prepare a process image
		dataStore = new SimpleProcessImage();
		dataStore.addDigitalOut(new SimpleDigitalOut(true));
		dataStore.addDigitalOut(new SimpleDigitalOut(false));
		dataStore.addDigitalIn(new SimpleDigitalIn(false));
		dataStore.addDigitalIn(new SimpleDigitalIn(true));
		dataStore.addDigitalIn(new SimpleDigitalIn(false));
		dataStore.addDigitalIn(new SimpleDigitalIn(true));

		for (int i = 0; i < num_registers; i++) {
			dataStore.addRegister(new SimpleRegister(0));
		}		

		// 3. Set the image on the coupler
		ModbusCoupler.getReference().setProcessImage(dataStore);
		ModbusCoupler.getReference().setMaster(false);
		ModbusCoupler.getReference().setUnitID(15);

		if(null != slaveListener && slaveListener.isListening()){
			slaveListener.stop();
		}
		slaveListener = new ModbusTCPListener(25);
		slaveListener.setPort(port);
		slaveListener.start();
	}
	
//	public void setModifiedEventListener(ModifiedEventListener listener){
//		slaveListener.addModifiedEventListener(listener);
//	}

}
