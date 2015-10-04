package com.alibaba.middleware.race.rpc.api.impl;


import java.io.IOException;
import java.net.Socket;

import javax.net.SocketFactory;

public class RpcConsumerConnectionFactory{

  private final String host;
  private final int port;
  private final SocketFactory socketFactory;
  private final boolean delimited;

  /**
   * Constructor to create sockets the given host/port.
   */
  public RpcConsumerConnectionFactory(String host, int port, boolean delimited) {
    this(host, port, SocketFactory.getDefault(), delimited);
  }

  RpcConsumerConnectionFactory(String host, int port,
      SocketFactory socketFactory, boolean delimited) {
    this.host = host;
    this.port = port;
    this.socketFactory = socketFactory;
    this.delimited = delimited;
  }

  public RpcConnection createConnection(int timeout) throws IOException {
    // Open socket
    Socket socket = socketFactory.createSocket(host, port);
    socket.setSoTimeout(timeout);
    return new RpcConnection(socket, delimited);
  }
}