package com.guoguo.dubbo.client.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class RpcProxyHandler extends ChannelInboundHandlerAdapter {
	
	private Object response;
	public Object getResponse() {
		return response;
	}
	
	//ctx write to server,msg read from server
	public void channelRead(ChannelHandlerContext ctx,Object msg) throws Exception{
		response = msg;
	}

}
