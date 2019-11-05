package client;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.text.SimpleDateFormat;

import org.jacoco.core.tools.ExecDumpClient;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.IncompatibleExecDataVersionException;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

public final class ExecutionDataClient {

	/**
	 * Starts the execution data request.
	 *
	 * @param args
	 * @throws IOException
	 */
	public static void main(final String[] args) {

		final String ADDRESS = args[0];
		final int PORT = Integer.parseInt(args[1]);
		final String filePath = args[2];
		final int periodicS = Integer.parseInt(args[3]);
		final int periodicMS = periodicS * 1000;

		String resetCommand = args[4];

		final boolean reset;
		String resetOption = "reset";

		if (resetCommand.equals(resetOption)) {
			reset = true;
		} else {
			reset = false;
		}

		Timer timer = new Timer();
		TimerTask sometask = new TimerTask() {

			@Override
			public void run() {

				clientConnect(ADDRESS, PORT, filePath, reset);

			}

		};

		timer.schedule(sometask, 0l, periodicMS);

	}

	public static void clientConnect(String ADDRESS, int PORT, String filePath, Boolean reset) {

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		String tsText = sdf.format(date);


		String DESTFILE = filePath + "jacoco-" + tsText + ".exec";


		System.out.println("server address : " + ADDRESS + " port : " + PORT + " file : " + DESTFILE);

		FileOutputStream localFile = null;

		try {
			localFile = new FileOutputStream(DESTFILE);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ExecutionDataWriter localWriter = null;
		try {
			localWriter = new ExecutionDataWriter(localFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(" Open a socket to the coverage agent..");
		Socket socket = null;

		try {
			socket = new Socket(InetAddress.getByName(ADDRESS), PORT);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RemoteControlWriter writer = null;

		try {
			writer = new RemoteControlWriter(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		RemoteControlReader reader = null;

		try {
			reader = new RemoteControlReader(socket.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reader.setSessionInfoVisitor(localWriter);
		reader.setExecutionDataVisitor(localWriter);

		// 
		System.out.println(" Send a dump command and read the response...");
		try {
			writer.visitDumpCommand(true, reset);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			if (!reader.read()) {
				throw new IOException("Socket closed unexpectedly.");
			}
		} catch (IncompatibleExecDataVersionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			localFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(" session dumped successfully. ");

	}

}
