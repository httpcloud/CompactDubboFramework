package com.guoguo.dubbo.client;

import com.guoguo.dubbo.client.proxy.RpcClientProxy;
import com.guoguo.dubbo.client.registry.IServiceDiscovery;
import com.guoguo.dubbo.client.registry.ServiceDiscoveryImpl;
import com.guoguo.dubbo.service.IHello;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	IServiceDiscovery serviceDiscovery = new ServiceDiscoveryImpl();
    	String url = serviceDiscovery.discover("com.guoguo.dubbo.service.IHellos");    	
    	System.out.println(url==null?"error,not find the node!":url);
    	
    	RpcClientProxy rpcClientProxy = new RpcClientProxy(serviceDiscovery);
        IHello hello = rpcClientProxy.create(IHello.class);
        String result = hello.sayHello("guoguo");
    	System.out.println(result);
    }
}
