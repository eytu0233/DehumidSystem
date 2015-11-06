package edu.ncku.uscc.process;

import java.io.OutputStream;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.util.DataStoreManager;

public abstract class Command {

	private static int TIME_OUT = 400;

	public static final int UNACK = -1;
	public static final int SKIP = -1;

	private Object referenceLock;

	protected DehumidRoomController controller;
	protected DataStoreManager dataStoreManager;
	private Command preCommand;
	private Command subCommand;

	private int init_tolerance = 2;
	private int err_tolerance = init_tolerance;
	private byte txBuf;
	private byte rxBuf;

	private boolean ack;

	/**
	 * Base constructor
	 * 
	 * @param controller
	 */
	public Command(DehumidRoomController controller) {
		super();
		this.controller = controller;
		this.referenceLock = controller.getLock();
		this.dataStoreManager = controller.getDataStoreManager();
		this.init();
	}

	/**
	 * Advance constructor which can set tolerance initial value
	 * 
	 * @param controller
	 * @param tolerance the initial value for err_tolerance field
	 */
	public Command(DehumidRoomController controller, int tolerance) {
		super();
		this.controller = controller;
		this.referenceLock = controller.getLock();
		this.dataStoreManager = controller.getDataStoreManager();
		this.init_tolerance = tolerance;
		this.init();
	}

	/**
	 * Advance constructor which can set preCommand  field
	 * 
	 * @param controller
	 * @param preCommand the command that would start before this command
	 */
	public Command(DehumidRoomController controller, Command preCommand) {
		super();
		this.controller = controller;
		this.referenceLock = controller.getLock();
		this.dataStoreManager = controller.getDataStoreManager();
		this.preCommand = preCommand;
		this.init();
	}

	/*
	 * Setter and getter
	 * */
	public void setPreCommand(Command preCommand) {
		this.preCommand = preCommand;
	}

	public void setSubCommand(Command subCommand) {
		this.subCommand = subCommand;
	}

	public byte getTxBuf() {
		return txBuf;
	}

	public void setRxBuf(byte rxBuf) {
		this.rxBuf = rxBuf;
	}

	public boolean isAck() {
		return ack;
	}	

	/**
	 * Starts this command
	 * 
	 * @throws Exception
	 */
	public final void start() throws Exception {

		/* run the command first before this command */
		if (preCommand != null) {
			preCommand.start();

			if (!preCommand.isAck() && preCommand.getTxBuf() != SKIP) {
				if (preCommand.overTolerance()) {
					finishHandler();
				}
				return;
			}
		}

		rxBuf = UNACK;
		txBuf = requestHandler();

		/* when skip flag is true, it won't emit data and handle reply */
		if (txBuf != SKIP) {

			/* emit the txBuf data */
			emit();

			/* the hook method which handles reply */
			ack = replyHandler(rxBuf);

			/* when unack, it means timeout */
			if (!ack) {
				timeout();
				if (overTolerance()) {
					init();
					timeoutHandler();
				}
				return;
			}
		}

		/*
		 * if this command acks or skip flag is true, it will start subCommand(if exists) and
		 * finishCommandHandler hook method
		 */
		if (ack || txBuf == SKIP) {
			init();
			if (subCommand != null) {
				subCommand.start();
				if (!subCommand.isAck() && subCommand.getTxBuf() != SKIP) {
					if (subCommand.overTolerance()) {
						finishHandler();
					}
					return;
				}
			}
			finishHandler();
		}
	}

	/**
	 * Initial method
	 */
	private void init() {
		this.ack = false;
		this.err_tolerance = init_tolerance;
		this.subCommand = null;
	}

	/**
	 * Handles the process that transmits data to the outputStream, and it would
	 * wait until timeout
	 * 
	 * @throws Exception
	 */
	private void emit() throws Exception {
		synchronized (referenceLock) {
			OutputStream output = controller.getOutputStream();
			if (output != null) {
				output.write(txBuf);
			} else {
				// throw new NullOutputSreamException();
			}
			referenceLock.wait(TIME_OUT);
		}
	}
	
	/**
	 * Set this command timeout
	 */
	private void timeout() {
		--err_tolerance;
	}

	/**
	 * 
	 * @return does this command over timeout tolerance
	 */
	private boolean overTolerance() {
		return err_tolerance <= 0;
	}

	/**
	 * Sets the request command
	 * 
	 * @return the command data of the protocol
	 * @throws Exception
	 */
	abstract protected byte requestHandler() throws Exception;

	/**
	 * Handles the result of this command
	 * 
	 * @param rxBuf
	 *            the result of this command
	 * @return whether this command acked or not
	 * @throws Exception
	 */
	abstract protected boolean replyHandler(byte rxBuf) throws Exception;

	/**
	 * Handles how this command finishes
	 * 
	 * @throws Exception
	 */
	abstract protected void finishHandler() throws Exception;

	/**
	 * Handles how this command
	 * 
	 * @throws Exception
	 */
	abstract protected void timeoutHandler() throws Exception;

}
