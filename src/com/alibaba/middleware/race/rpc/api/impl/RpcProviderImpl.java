package com.alibaba.middleware.race.rpc.api.impl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.middleware.race.rpc.api.RpcProvider;
import com.alibaba.middleware.race.rpc.context.RpcContext;
import com.alibaba.middleware.race.rpc.demo.service.RaceException;
import com.alibaba.middleware.race.rpc.model.RpcRequest;
import com.alibaba.middleware.race.rpc.model.RpcResponse;

public class RpcProviderImpl extends RpcProvider {
	private static final Logger LOG = Logger.getLogger(RpcProviderImpl.class
			.getName());

	private String version;
	private int timeout;
	private Class<?> interfaceclazz;
	private Object interfaceimpl;
	// private final RpcProviderConnectionFactory pFactory;
	// private final ExecutorService executor;
	// private final ServerThread serverThread;
	private final boolean waitForCallback;

	// private final Map<String, Class<?> > serviceMap;
	public RpcProviderImpl() {
		// version = null;
		// this.pFactory = new RpcProviderConnectionFactory(8888, true);
		// this.executor = Executors.newFixedThreadPool(10);
		// this.serverThread = new ServerThread();
		// this.serverThread.setDaemon(true);
		// this.serviceMap = new HashMap<String, Class<?> >();
		this.waitForCallback = false;
	}

	public void bind(int port) throws Exception {
		LOG.info("Starting RPC server");
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workeGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()*2);
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workeGroup)
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 128)
					.childHandler(new ChildChannelHandler());

			ChannelFuture future = b.bind(port).sync();
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("line : 57 bind() Exception" + e);
		} finally {
			bossGroup.shutdownGracefully();
			workeGroup.shutdownGracefully();
		}
	}

	private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel arg0) throws Exception {
			// 添加对象解码器， 负责对序列化对象进行解码 设置对象序列化长度最大长度是1M 防止内存溢出
			arg0.pipeline().addLast(
					new ObjectDecoder(1024 * 1024, ClassResolvers
							.weakCachingConcurrentResolver(this.getClass()
									.getClassLoader())));
			//对象编码器
			arg0.pipeline().addLast(new ObjectEncoder());
			arg0.pipeline().addLast(new ConnectionHandler());
		}

	}

	/**
	 * init Provider
	 */
	private void init() {
		// TODO
	}

	/**
	 * set the interface which this provider want to expose as a service
	 * 
	 * @param serviceInterface
	 */
	@Override
	public RpcProvider serviceInterface(Class<?> serviceInterface) {
		// TODO
		interfaceclazz = serviceInterface;
		return this;
	}

	/**
	 * set the version of the service
	 * 
	 * @param version
	 */
	@Override
	public RpcProvider version(String version) {
		// TODO
		this.version = version;
		return this;
	}

	/**
	 * set the instance which implements the service's interface
	 * 
	 * @param serviceInstance
	 */
	@Override
	public RpcProvider impl(Object serviceInstance) {
		// TODO
		interfaceimpl = serviceInstance;
		return this;
	}

	/**
	 * set the timeout of the service
	 * 
	 * @param timeout
	 */
	@Override
	public RpcProvider timeout(int timeout) {
		// TODO
		this.timeout = timeout;
		return this;
	}

	/**
	 * set serialize type of this service
	 * 
	 * @param serializeType
	 */
	@Override
	public RpcProvider serializeType(String serializeType) {
		// TODO
		return this;
	}

	/**
	 * publish this service if you want to publish your service , you need a
	 * registry server. after all , you cannot write servers' ips in config file
	 * when you have 1 million server. you can use ZooKeeper as your registry
	 * server to make your services found by your consumers.
	 */
	@Override
	public void publish() {
		// TODO
		try {
			bind(8888);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("line : 160 pubish bind() exception" + e);
		}
	}

	class ConnectionHandler extends ChannelInboundHandlerAdapter {
		
		@Override
	    public void channelRead(ChannelHandlerContext ctx, Object msg)
	            throws Exception{
			RpcRequest req = (RpcRequest) msg;

			RpcContext.props.putAll((Map) req.getProps());
			forwardBlockingRpc(ctx, req);
			ctx.flush();
		}
		
		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) {
	        ctx.flush();
	    }
		
		@Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	        cause.printStackTrace();
	        ctx.close();
	    }
//
//		@Override
//		public void run() {
//			try {
//				// Parse request
//				RpcRequest req = (RpcRequest) connection.receiveMessage();
//
//				RpcContext.props.putAll((Map) req.getProps());
//				if (waitForCallback) {
//					forwardAsynRpc(req);
//				} else {
//					forwardBlockingRpc(req);
//				}
//			} catch (IOException e) {
//				sendResponse(handleError("Bad request data from client", e));
//			}
//		}



		private void forwardBlockingRpc(ChannelHandlerContext ctx, RpcRequest rpcRequest) {
			// Forward request
			try {
				RpcResponse rpcResponse = doBlockingRpc(ctx, rpcRequest);
				sendResponse(ctx, rpcResponse);
			} catch (Exception e) {
				RaceException reException = (RaceException) ((InvocationTargetException)e).getTargetException();
				sendResponse(ctx, handleError(reException.getMessage(), reException.getCause()));
			}
		}

		private RpcResponse doBlockingRpc(ChannelHandlerContext ctx, RpcRequest rpcRequest)
				throws InterruptedException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			// TODO Auto-generated method stub
			Method method;
			RpcResponse response;
			try {
				method = interfaceimpl.getClass().getDeclaredMethod(
						rpcRequest.getMethodName());
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			Object resp;
				method.setAccessible(true);
				resp = method.invoke(interfaceimpl, rpcRequest.getParameters());
//			} catch (Throwable e) {
//				// �����쳣
//				LOG.info("����˷����쳣�� ");
//				sendResponse(ctx, (InvocationTargetException)e);
//				return null;
//			}
			response = new RpcResponse();
			// response.setErrorMsg("");
			response.setAppResponse(resp);
			return response;
		}

		private void sendResponse(ChannelHandlerContext ctx, Object rpcResponse) {
			ctx.writeAndFlush(rpcResponse);

		}

		private RpcResponse handleError(String msg, Throwable throwable) {
			RpcResponse resp = new RpcResponse();
			resp.setErrorMsg(msg);
			return resp;
		}
	}
}
