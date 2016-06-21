
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.bukkit.ChatColor;

public class AcceptedSocketConnection extends Thread{
	Socket sock;
	Main plugin;
	public AcceptedSocketConnection(Socket sock, Main plugin) {
		// TODO Auto-generated constructor stub
		this.sock = sock;
		this.plugin = plugin;
	}
	@Override
	public void run() {
		try {
			plugin.logConsole(ChatColor.GOLD + "*****Accepted connection of " + sock.getInetAddress() + "*****");
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			
			String s;
	        int counterr = 0, contentLength = 0;
	        //boolean gotEmptyLine = false;//TODO Remember why I did this line lol
	        while (!(s = in.readLine()).equals("")) {
	            plugin.logConsole(s);
	            if(counterr == 0 && s.equalsIgnoreCase(Main.closeConnection)){
	            	out.close();
	    	        in.close();
	    	        sock.close();
	    	        
	    	        plugin.logConsole(ChatColor.BLUE + "*****Ended connection of " + sock.getInetAddress() + "*****");
	    	        plugin.logConsole(ChatColor.DARK_GREEN + "- ASC_THREAD - Closed listening web server successfully!");
	    	        return;
	            }
	            if(s.startsWith("Content-Length: ")){
	            	contentLength = Integer.parseInt(s.split("Length: ")[1]);
	            }
	            counterr++;
	        }
	        
	        String finalString = "";
	        for(int i = 0; i < contentLength; i++){
	        	finalString += (char) in.read();
	        }
	        plugin.logCon(finalString);//>Request from client is logged on console
	        //This section is the response to the clients request, the web page:
	        out.write("HTTP/1.0 200 OK\r\n");
	        out.write("Date: Fri, 31 Dec 1999 23:59:59 GMT\r\n");
	        out.write("Server: Apache/0.8.4\r\n");
	        out.write("Content-Type: text/html\r\n");
	        out.write("Content-Length: " + 36 + "\r\n");
	        out.write("Expires: Sat, 01 Jan 2000 00:59:59 GMT\r\n");
	        out.write("Last-modified: Fri, 09 Aug 1996 14:21:40 GMT\r\n");
	        out.write("\r\n");
	        //Content of returned html page is here:
	        out.write("<h1>Successful</h1>");
	        out.write("<br><h2>Nice</h2>");
	        
	        out.close();
	        in.close();
	        sock.close();
	        
	        plugin.logConsole(ChatColor.BLUE + "*****Ended connection of " + sock.getInetAddress() + "*****");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
