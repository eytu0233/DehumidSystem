package edu.ncku.uscc.process;

import java.io.OutputStream;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.util.DataStoreManager;

public abstract class Command {

	private static int TIME_OUT = 600;

	public static final int UNACK = -1;
	public static final int SKIP = -1;

	private Object referenceLock;

	protected DehumidRoomController controller;
	protected DataStoreManager dataStoreManager;
	private Command preCommand;

	private int init_tolerance = 2;
	private int err_tolerance = init_tolerance;
	private byte txBuf;

	private boolean ack;
	private boolean preAck;
	private boolean follow;

	private IPreCMDNotifier notifyListener;

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
	 * @param tolerance
	 *            the initial value for err_tolerance field
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
	 * Advance constructor which can set preCommand field
	 * 
	 * @param controller
	 * @param preCommand
	 *            the command that would start before this command
	 */
	public Command(DehumidRoomController controller, Command preCommand) {
		super();
		this.controller = controller;
		this.referenceLock = controller.getLock();
		this.dataStoreManager = controller.getDataStoreManager();
		this.preCommand = preCommand;
		this.preCommand.setNotifyListener(new IPreCMDNotifier(){

			@Override
			public void notifyMainCMD(boolean ack) {
				// TODO Auto-generated method stub
				preAck = ack;
			}
			
		});
		this.init();
	}

	/*
	 * Setter and getter
	 */
	public void setPreCommand(Command preCommand) {
		this.preCommand = preCommand;
	}

	public byte getTxBuf() {
		return txBuf;
	}

	public boolean isAck() {
		return ack;
	}

	private void setNotifyListener(IPreCMDNotifier listener) {
		// TODO Auto-generated method stub
		this.notifyListener = listener;
	}

	/**
	 * Starts this command
	 * 
	 * @throws Exception
	 */
	public final void start() throws Exception {

		// Run the command first before this command 
		if (preCommand != null) {
			preCommand.start();

			// If preCommand(like NotifyDeviceIDCmd) is not ack, main command is over.
			if (!preAck) {
				return;
			}
		}
		
		controller.setRxBuf((byte)UNACK);
		txBuf = requestHandler();

		// When skip flag is true, it won't emit data and handle reply 
		if (txBuf != SKIP) {

			emitAndReceiveData();

			if(follow) {
				init();
				return;
			}

			/* When UNACK, it means timeout */
			if (!ack) {
				timeout();
				if (overTolerance()) {
					if(notifyListener != null) notifyListener.notifyMainCMD(false);
					init();
					timeoutHandler();
				}
				return;
			}
		}

		/*
		 * If this command ACK or SKIP flag is true, it will start
		 * subCommand(if exists) and finishHandler hook method
		 */
		if (ack || txBuf == SKIP) {
			if(notifyListener != null) notifyListener.notifyMainCMD(true);
			init();
			finishHandler();
		}
	}
	
	protected final void followCmd(Command flwCmd, Command cmd){
		follow = true;
		controller.followCmd(flwCmd, cmd);
	}

	/**
	 * Initial method
	 */
	private void init() {
		this.ack = false;
		this.follow = false;
		this.err_tolerance = init_tolerance;
	}

	/**
	 * Handles the process that transmits data to the outputStream, and it would
	 * wait until timeout
	 * 
	 * @throws Exception
	 */
	private void emitAndReceiveData() throws Exception {
		synchronized (referenceLock) {
			OutputStream output = controller.getOutputStream();
			if (output != null) {
				output.write(txBuf);
			} else {
				 throw new NullPointerException("OutputSream is null");
			}
			referenceLock.wait(TIME_OUT);
			
			/* The hook method which handles reply */
			ack = replyHandler(controller.getRxBuf());
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
	
	/**
	 * This interface is used to pass the ACK of preCommand to main command
	 * 
	 * @author steve chen
	 *
	 */
	private interface IPreCMDNotifier {

		public void notifyMainCMD(boolean ack);
		
	}

}
