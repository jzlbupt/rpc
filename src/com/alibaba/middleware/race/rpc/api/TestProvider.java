package com.alibaba.middleware.race.rpc.api;

//import com.alibaba.middleware.race.rpc.demo.service.RaceTestService;
//import com.alibaba.middleware.race.rpc.demo.service.RaceTestServiceImpl;
import com.alibaba.middleware.race.rpc.api.impl.RpcProviderImpl;
import com.alibaba.middleware.race.rpc.demo.service.RaceTestService;
import com.alibaba.middleware.race.rpc.demo.service.RaceTestServiceImpl;

public class TestProvider {
	static{
		RpcProvider rpcProvider = null;
	    rpcProvider = new RpcProviderImpl();
	    rpcProvider.serviceInterface(RaceTestService.class)
	            .impl(new RaceTestServiceImpl())
	            .version("1.0.0.api")
	            .timeout(3000)
	            .serializeType("java").publish();
	
	    
	    try {
	    	System.out.println("quit");
	        Thread.sleep(Integer.MAX_VALUE);
	    } catch (InterruptedException e) {
	        // ignore
	    }
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
