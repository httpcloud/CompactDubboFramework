package com.guoguo.dubbo.client.proxy;

import java.beans.PropertyChangeListenerProxy;
import java.lang.reflect.Proxy;

import com.guoguo.dubbo.bean.RpcRequest;
import com.guoguo.dubbo.client.registry.IServiceDiscovery;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.channel.socket.SocketChannel;

public class RpcClientProxy {
  private IServiceDiscovery serviceDiscovery;
  
  public RpcClientProxy(IServiceDiscovery serviceDiscovery) {
	  this.serviceDiscovery = serviceDiscovery;
  }
  
  @SuppressWarnings("unchecked")
  public <T> T create(final Class<T> interfaceClass) {
	  return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
			  new Class<?>[] {interfaceClass},(proxy,method,args)->{
				  RpcRequest request = new RpcRequest();
				  request.setClassName(method.getDeclaringClass().getName());
				  request.setMethodName(method.getName());
				  request.setTypes(method.getParameterTypes());
				  request.setParams(args);
				  
				  String serviceName = interfaceClass.getName();
				  String serviceAddress = serviceDiscovery.discover(serviceName);
				  String[] arrs = serviceAddress.split(":");
				  String host = arrs[0];
				  int port = Integer.parseInt(arrs[1]);
				  final RpcProxyHandler rpcProxyHandler = new RpcProxyHandler();
				  EventLoopGroup group = new NioEventLoopGroup();
				  try {					  
					  Bootstrap bootstrap = new Bootstrap();
					  bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
					  	 .handler(new ChannelInitializer<SocketChannel>() {
					  		 
							@Override
							protected void initChannel(SocketChannel sokcetChannel) throws Exception {
								ChannelPipeline pipeline = sokcetChannel.pipeline();
								pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
								pipeline.addLast("frameEncdoer", new LengthFieldPrepender(4));
								pipeline.addLast("encoder",new ObjectEncoder());
								pipeline.addLast("decoder",new io.netty.handler.codec.serialization.ObjectDecoder(Integer.MAX_VALUE,ClassResolvers.cacheDisabled(null)));
								
								//io process
								pipeline.addLast(rpcProxyHandler);		
	                        }		
		              });
					ChannelFuture future = bootstrap.connect(host,port).sync();
					future.channel().writeAndFlush(request);
					future.channel().closeFuture().sync();
					  	
				  }catch(Exception e) {
					  e.printStackTrace();
				  }finally {
					  group.shutdownGracefully();
				  }
				  return rpcProxyHandler.getResponse();

			  });
  }
}
