package com.alibaba.middleware.race.rpc.api.impl;


import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;

public class RpcProviderConnectionFactory{

  private static final Logger LOG =
      Logger.getLogger(RpcProviderConnectionFactory.class.getName());

  private final int port;
  private final int backlog;
  private final InetAddress bindAddr;
  private final boolean delimited;
  private final ServerSocketFactory socketFactory;

  private volatile ServerSocket serverSocket = null;

  /**
   * @param port Port that this server socket will be started on.
   * @param delimited Use delimited communication mode.
   */
  public RpcProviderConnectionFactory(int port, boolean delimited) {
    this(port, 4096, null, delimited);
  }

  /**
   * @param port Port that this server socket will be started on.
   * @param backlog the maximum length of the queue. A value <=0 uses default
   *        backlog.
   * @param bindAddr the local InetAddress the server socket will bind to. A
   *        null value binds to any/all local IP addresses.
   * @param delimited Use delimited communication mode.
   */
  public RpcProviderConnectionFactory(int port, int backlog,
      InetAddress bindAddr, boolean delimited) {
    this(port, backlog, bindAddr, delimited, ServerSocketFactory.getDefault());
  }

  // Visible for testing
  RpcProviderConnectionFactory(int port, int backlog,
      InetAddress bindAddr, boolean delimited,
      ServerSocketFactory socketFactory) {
    this.port = port;
    this.backlog = backlog;
    this.bindAddr = bindAddr;
    this.delimited = delimited;
    this.socketFactory = socketFactory;
  }

  public RpcConnection createConnection() throws IOException {

    ServerSocket local = serverSocket;
    if (local == null) {
      local = initServerSocket();
    }
    // Thread blocks here waiting for requests
    return new RpcConnection(serverSocket.accept(), delimited);
  }

  private synchronized ServerSocket initServerSocket() throws IOException {
    ServerSocket local = serverSocket;
    if (local == null) {
      LOG.info("Listening for requests on port: " + port);
      serverSocket = local = socketFactory.createServerSocket(port, backlog,
          bindAddr);
    }
    return local;
  }

  public void close() throws IOException {
    ServerSocket local = serverSocket;
    if (local != null && !local.isClosed()) {
      local.close();
    }
  }
}
