package com.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import javax.swing.InputMap;

import com.game.State;
import com.util.Log;

public class TcpClient {

	Socket socket = null;
	InputStream in = null;
	BufferedReader read = null;
	OutputStream out = null;
	Game game = null;

	public void conn(String args[]) throws IOException {
		// conn
		State.isRegistered=false;
		int i=888;
		while (State.isRegistered==false&&(i-->0)) {
			// sleep exception
			try {
				// socket exception
				try {
					// start socket
					SocketAddress serveraddress = new InetSocketAddress(
							args[0], Integer.parseInt(args[1]));
					SocketAddress hostaddress = new InetSocketAddress(args[2],
							Integer.parseInt(args[3]));
					socket = new Socket();
					socket.setReuseAddress(true);
					socket.bind(hostaddress);
					socket.connect(serveraddress);
					State.isRegistered=true;
					//System.out.println("register succeed");
				} catch (Exception e) {
					//System.out.println("register failed and try");
					Thread.sleep(3000);
				}
			} catch (Exception e) {
				//System.out.println("sleep failed");
				//e.printStackTrace();
			}
		}
	}

	public void register(String args[]) throws IOException {

		try {
			conn(args);
			in = socket.getInputStream();
			read = new BufferedReader(new InputStreamReader(in));
			out = socket.getOutputStream();
			// register
			game.reg(out);
			// game round
			game.start(read, out);
			// finish
			socket.close();
		} catch (IOException e) {
			// System.out.println("IO wrong");
			if (in != null)
				in.close();
			if (out != null)
				out.close();
			if (socket != null)
				socket.close();
			e.printStackTrace();
		} finally {
			// System.out.println("close");
			if (in != null)
				in.close();
			if (out != null)
				out.close();
			if (socket != null)
				socket.close();
		}
	}

	public static void main(String args[]) throws IOException,
			InterruptedException {
		TcpClient client = new TcpClient();
		client.game = new Game(args[4], args[5]);
		client.register(args);

	}
}