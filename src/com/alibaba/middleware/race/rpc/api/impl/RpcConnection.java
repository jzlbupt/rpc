package com.alibaba.middleware.race.rpc.api.impl;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class RpcConnection{

  private final Socket socket;
  private final OutputStream out;
  private final InputStream in;
  private final ObjectOutputStream oout ;
  private final ObjectInputStream oin ;
  
  private final boolean delimited;

  RpcConnection(Socket socket, boolean delimited) throws IOException {
    this.socket = socket;
    
    this.delimited = delimited;

    // Create input/output streams
    try {
      out = socket.getOutputStream();
      in = socket.getInputStream();
      
      oout = new ObjectOutputStream(out);
      oin = new ObjectInputStream(in);
    } catch (IOException e) {
      // Cleanup and rethrow
      try {
        socket.close();
      } catch (IOException ioe) {
        // It's ok
      }
      throw e;
    }
  }

  public void sendMessage(Object message) throws IOException {
    // Write message
	
    if (delimited) {
      try {
        //–¥µΩout÷–
    	oout.writeObject(message);
        out.flush();
      } catch (IOException e) {
        // Cannot write anymore, just close socket
        socket.close();
        throw e;
      }
    } else {
      //message.writeTo(out);
      oout.writeObject(message);
      out.flush();
      socket.shutdownOutput();
    }
  }

  public Object receiveMessage() throws IOException {
    // Read message from in
	  try{
		  return oin.readObject();
	  }catch(IOException e){
		  throw e;
	  }
	  catch(Exception w){
		  ;
	  }
	  return null;
  }

  public void close() throws IOException {
    if (!socket.isClosed()) {
      socket.close();
    }
  }

  public boolean isClosed() {
    return socket.isClosed();
  }
}