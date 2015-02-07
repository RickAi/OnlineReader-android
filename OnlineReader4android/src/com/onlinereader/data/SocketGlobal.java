package com.onlinereader.data;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketGlobal {
	public static Socket socket;
	public static ObjectOutputStream toServer;
	public static ObjectInputStream fromServer;
}
