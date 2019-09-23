package com.guoguo.dubbo.server;

import java.io.IOException;

import com.guoguo.dubbo.server.register.IRegiserCenter;
import com.guoguo.dubbo.server.register.RegisterCenterImpl;
import com.guoguo.dubbo.service.HelloImpl;
import com.guoguo.dubbo.service.IHello;

/**
 * Hello world!
 *
 */
public class App 
{

	public static void main( String[] args ) throws IOException
    {
        IRegiserCenter regiserCenter = new RegisterCenterImpl();
        //regiserCenter.register("com.guoguo.service", "127.0.0.1:8989");
        //System.in.read();
        
        RpcServer rpcServer = new RpcServer(regiserCenter, "127.0.0.1:8989");
        IHello hello = new HelloImpl();
        rpcServer.bind(hello);
        rpcServer.publisher();
        
     
        
    }
}
