package com.guoguo.dubbo.server;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.guoguo.dubbo.bean.RpcRequest;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.AsciiHeadersEncoder.NewlineType;

public class RpcServerHandler extends ChannelInboundHandlerAdapter {

	private Map<String, Object> handleMap = new HashMap<String, Object>();
	
	public RpcServerHandler(Map<String, Object> handleMap) {
		this.handleMap = handleMap;
	}
	
	//ctx write,msg read
	public void channelRead(ChannelHandlerContext ctx,Object msg) throws Exception{
		RpcRequest rpcRequest = (RpcRequest)msg;
		Object result = new  Object();
		if(handleMap.containsKey(rpcRequest.getClassName())) {
			Object clazz = handleMap.get(rpcRequest.getClassName());
			Method  method = clazz.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getTypes());
			result = method.invoke(clazz, rpcRequest.getParams());
		
		}
		ctx.write(result);
		ctx.flush();
		ctx.close();
		
	}
	
}
