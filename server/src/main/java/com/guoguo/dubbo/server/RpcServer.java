package com.guoguo.dubbo.server;

import java.util.HashMap;
import java.util.Map;

import com.guoguo.dubbo.server.register.IRegiserCenter;
import com.guoguo.dubbo.server.register.RpcAnnotation;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class RpcServer {
	
	private IRegiserCenter registerCenter;
	private String serviceAddress;
	private Map<String, Object> handleMap = new HashMap<String, Object>();
		
	public RpcServer(IRegiserCenter registerCenter,String serviceAddress) {
		this.registerCenter=registerCenter;
		this.serviceAddress=serviceAddress;
	}
	
	public void publisher() {
	
		for(String serviceName:handleMap.keySet()) {
			registerCenter.register(serviceName, serviceAddress);
		}
		
		
		try {
			EventLoopGroup bossGroup = new NioEventLoopGroup();
			EventLoopGroup workGroup = new NioEventLoopGroup();
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workGroup);
			bootstrap.channel(NioServerSocketChannel.class);
			
			/*
			bootstrap.childHandler(initChannel(channel)->{
				ChannelPipeline pipeline = channel.pipeline();
				pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4));
				pipeline.addLast("frameEncdoer", new LengthFieldPrepender(4));
				pipeline.addLast("encoder",new ObjectEncoder());
				pipeline.addLast("decoder",new io.netty.handler.codec.serialization.ObjectDecoder(Integer.MAX_VALUE,null));
				
			});
			*/
			
			bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel channel) throws Exception {
					ChannelPipeline pipeline = channel.pipeline();
					pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
					pipeline.addLast("frameEncdoer", new LengthFieldPrepender(4));
					pipeline.addLast("encoder",new ObjectEncoder());
					pipeline.addLast("decoder",new io.netty.handler.codec.serialization.ObjectDecoder(Integer.MAX_VALUE,ClassResolvers.cacheDisabled(null)));
					
					//io procress
					pipeline.addLast(new RpcServerHandler(handleMap));					
				}
				
			}).option(ChannelOption.SO_BACKLOG,128).childOption(ChannelOption.SO_KEEPALIVE,true);
			
			String[] addrs = serviceAddress.split(":");
			String ip = addrs[0];
			int port = Integer.parseInt(addrs[1]);
			ChannelFuture future = bootstrap.bind(ip,port).sync();
			System.out.println("netty servier start successfully! wait for client connecting ...");

			future.channel().closeFuture().sync();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void bind(Object...services) {
		for(Object service:services) {
			RpcAnnotation annotation = service.getClass().getAnnotation(RpcAnnotation.class);
			if (annotation==null){
                continue;
            }
            String serviceName = annotation.value().getName();
            handleMap.put(serviceName,service);			
		}
	}

}
