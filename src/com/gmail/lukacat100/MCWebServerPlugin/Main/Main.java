
/*MIT License

Copyright (c) 2016 Tom Adler

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.*/

package com.gmail.lukacat100.MCWebServerPlugin.Main;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	private boolean debug;// received from config. Shows debug messages if true.
	// ^START PROTOCOL^
	public static String closeConnection = "!Close Connection!";
	// ^END PROTOCOL^
	private int listeningport;// received from config

	private Main m = this;// Passed to necessary classes constructor

	private Thread acceptor;
	private boolean acceptorRunning;// As long as this variable is true, the
									// acceptor thread will keep on getting
									// connections from the php web server

	private ServerSocket ss;
	// private List<Integer> li = new ArrayList<Integer>();//should be used to
	// store data from receiving thread. not used for now.

	/*
	 * public synchronized void add(int id) {li.add(id); }//should be used to
	 * store data from receiving thread. not used for now.
	 * 
	 */

	public synchronized void logConsole(String s) {
		if (debug)
			getServer().getConsoleSender().sendMessage(s);
	}
	public synchronized void logCon(String s) {//Non-debug messages
		
			getServer().getConsoleSender().sendMessage(s);
	}
	private synchronized boolean getAcceptorRunning() {
		return acceptorRunning;
	}

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		this.reloadConfig();
		debug = getConfig().getBoolean("debug");
		if (getConfig().isSet("listeningport")) {// Value set by user-set
													// config.yml
			logCon(ChatColor.GREEN + "Found a listening port for the registeration changes.");
			try {
				listeningport = getConfig().getInt("listeningport");
				ss = new ServerSocket(listeningport);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {// Value NOT set by user-set config.yml
			if (getConfig().contains("listeningport")) {// defaults exists
				logCon(
						ChatColor.YELLOW + "Listening port for registeration changes NOT FOUND! Using default value!");
				try {
					listeningport = getConfig().getInt("listeningport");
					ss = new ServerSocket(listeningport);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {// default doesnt exist. Shutting down.
				logCon(ChatColor.DARK_RED + "Plugin disabled! NO VALUE WAS FOUND FOR LISTENING PORT!");
				Bukkit.getPluginManager().disablePlugin(this);
				return;
			}
		}
		// logConsole(ChatColor.ITALIC + "Started server on " +
		// ss.getInetAddress() == null ? "NULL" : ss.getInetAddress() + ":" +
		// ss.getLocalPort() == null ? "NULL" : ss.getLocalPort() + "!");
		acceptorRunning = true;
		acceptor = new Thread(new Runnable() {

			@Override
			public void run() {
				Socket sock;
				logConsole(ChatColor.AQUA + "-----Accepting connections-----");

				while (getAcceptorRunning()) {
					try {
						sock = ss.accept();
						new AcceptedSocketConnection(sock, m).start();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				logConsole(ChatColor.LIGHT_PURPLE + "-----Done accepting connections-----");
			}
		});
		acceptor.start();
		super.onEnable();
	}

	@Override
	public void onDisable() {
		acceptorRunning = false;
		Socket sockCloser;
		try {
			sockCloser = new Socket("localhost", listeningport);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sockCloser.getOutputStream()));
			out.write(Main.closeConnection);
			out.close();
			sockCloser.close();
			logConsole(ChatColor.DARK_GREEN + "Closed listening web server successfully!");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			ss.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onDisable();
	}
}
