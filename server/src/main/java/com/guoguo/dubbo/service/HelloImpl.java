package com.guoguo.dubbo.service;

import com.guoguo.dubbo.server.register.RpcAnnotation;

@RpcAnnotation(IHello.class)
public class HelloImpl implements IHello {

	public String sayHello(String str) {
		return "Hello,"+str;
	}
	

}
