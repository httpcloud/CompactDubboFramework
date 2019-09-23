package com.guoguo.dubbo.client.registry;

import java.awt.TexturePaint;
import java.util.ArrayList;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.google.common.collect.ImmutableBiMap.Builder;
import com.guoguo.dubbo.client.loadbalance.LoadBalance;
import com.guoguo.dubbo.client.loadbalance.LoadBalanceImpl;

public class ServiceDiscoveryImpl implements IServiceDiscovery{
	
	List<String> repos = new ArrayList<String>();
	
	private CuratorFramework curatorFramework;
    public ServiceDiscoveryImpl() {
		
		curatorFramework = CuratorFrameworkFactory.builder()
				.connectString(ZkConfig.CONNECTION_URL)
				.sessionTimeoutMs(5000)
	            .retryPolicy(new ExponentialBackoffRetry(1000,10)).build();
		
		curatorFramework.start();
	}
	
	public String discover(String serviceName) {
		
		String pathString = ZkConfig.ZK_REGISTER_PATH+"/"+serviceName;
		try {
			repos = curatorFramework.getChildren().forPath(pathString);
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
		//monitor node change
		regiserWatch(pathString);
		
		LoadBalance loadBalance = new LoadBalanceImpl();
		return loadBalance.select(repos);
		
	}
	
	private void regiserWatch(final String path) {
		PathChildrenCache childrenCache = new PathChildrenCache(curatorFramework, path, true);
		
		PathChildrenCacheListener pathChildrenCacheListener = new PathChildrenCacheListener() {
		    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
		    	repos=curatorFramework.getChildren().forPath(path);				
			}
		};
		
		childrenCache.getListenable().addListener(pathChildrenCacheListener);
		try {
			childrenCache.start();
		}catch (Exception e) {
			throw new RuntimeException("regist PathChild Watcher Exception!");
		}
	
	}
		
}
