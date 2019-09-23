package com.guoguo.dubbo.server.register;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

public class RegisterCenterImpl implements IRegiserCenter {
	
	private CuratorFramework curatorFramework;
	public  RegisterCenterImpl() {		
		curatorFramework = CuratorFrameworkFactory.builder().connectString(ZkConfig.CONNECTION_URL).sessionTimeoutMs(5000)
				.retryPolicy(new ExponentialBackoffRetry(100, 10)).build();
		curatorFramework.start();
	}

	public void register(String serviceName, String serviceAddress) {
		String servicePath = ZkConfig.ZK_REGISTER_PATH+"/"+serviceName;
		try {
			if(curatorFramework.checkExists().forPath(servicePath) == null) {
				curatorFramework.create().creatingParentsIfNeeded()
				  .withMode(CreateMode.PERSISTENT).forPath(servicePath,"0".getBytes());
			}
			// /registry/com.guoguo.service.IHello/serverAddress
			
			String addressPath = servicePath + "/" + serviceAddress;
			String rsNode = curatorFramework.create().withMode(CreateMode.EPHEMERAL)
					 .forPath(addressPath,"0".getBytes());
			System.out.println("servcie register successfully! "+ rsNode);
			
		}catch (Exception e) {
			e.printStackTrace();
		}

	}

}
