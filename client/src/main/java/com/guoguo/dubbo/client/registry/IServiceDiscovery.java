package com.guoguo.dubbo.client.registry;

public interface IServiceDiscovery {
  
	//obtained service URL by serviceName
	String discover(String serviceName);
}
