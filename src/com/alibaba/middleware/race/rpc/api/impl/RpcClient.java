package com.alibaba.middleware.race.rpc.api.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import com.alibaba.middleware.race.rpc.model.RpcRequest;
import com.alibaba.middleware.race.rpc.model.RpcResponse;

public class RpcClient extends ChannelInboundHandlerAdapter {

    private String host;
    private int port;
//    private RpcResponse response;
    EventLoopGroup group ;
    ChannelFuture future;
    private BlockingQueue<RpcResponse> resQueue = new LinkedBlockingQueue<RpcResponse>();
    
    //默认构造方法
    public RpcClient(EventLoopGroup group) {
        this.host = "127.0.0.1";
        this.port = 8888;
        this.group = group;
        try {
			init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    } 
    
    public RpcClient(String host, int port, EventLoopGroup group) {
        this.host = host;
        this.port = port;
        this.group = group;
        try {
			init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void init() throws Exception {
      try {
          Bootstrap bootstrap = new Bootstrap();
          bootstrap.group(group).channel(NioSocketChannel.class)
              .handler(new ChannelInitializer<SocketChannel>() {
                  @Override
                  public void initChannel(SocketChannel ch) throws Exception {
                  	ch.pipeline()
                          .addLast(new ObjectDecoder(1024, ClassResolvers.cacheDisabled(this.getClass().getClassLoader()))) // 将 RPC 请求进行编码 （为了发送请求）
                          .addLast(new ObjectEncoder()) // 将 RPC 响应进行解码（为了处理响应）
                          .addLast(RpcClient.this); // 使用 RpcClient 发送 RPC 请求
                  }
              })
              .option(ChannelOption.TCP_NODELAY, true);

          future = bootstrap.connect(host, port).sync();
//          if (response != null) {
//              future.channel().closeFuture().sync();
//          }
//          future.channel().close();
//          return response;
      } finally {
      	
//          group.shutdownGracefully();
      }
  }
    	
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception{
    		RpcResponse response = (RpcResponse)msg;
         
    	 resQueue.put(response);
	}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    public RpcResponse send(RpcRequest request) throws Exception {
            future.channel().writeAndFlush(request).sync();

            RpcResponse response = resQueue.take();
//            if (response != null) {
//                future.channel().closeFuture().sync();
//            }
//            future.channel().close();
            return response;
    }
}
