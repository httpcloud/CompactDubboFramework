package com.guoguo.dubbo.client.loadbalance;

import java.util.List;
import java.util.Random;

public class LoadBalanceImpl implements LoadBalance{

	public String select(List<String> repos) {
		int len = repos.size();
		if(len>0) {
			Random random = new Random();
			return repos.get(random.nextInt(len));
		}
		return null;
	}
	

}
