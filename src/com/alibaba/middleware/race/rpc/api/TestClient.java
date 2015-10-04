package com.alibaba.middleware.race.rpc.api;

import java.util.Map;


import com.alibaba.middleware.race.rpc.aop.ConsumerHook;
//import com.alibaba.middleware.race.rpc.demo.service.RaceConsumerHook;
//import com.alibaba.middleware.race.rpc.demo.service.RaceTestService;
import com.alibaba.middleware.race.rpc.api.impl.RpcConsumerImpl;
import com.alibaba.middleware.race.rpc.async.ResponseFuture;
import com.alibaba.middleware.race.rpc.demo.service.RaceConsumerHook;
import com.alibaba.middleware.race.rpc.demo.service.RaceDO;
import com.alibaba.middleware.race.rpc.demo.service.RaceException;
import com.alibaba.middleware.race.rpc.demo.service.RaceServiceListener;
import com.alibaba.middleware.race.rpc.demo.service.RaceTestService;



public class TestClient {
	 private static RpcConsumer consumer;
	    private static RaceTestService apiService;
	    static {
	        consumer = new RpcConsumerImpl();
	        apiService = (RaceTestService) consumer
	                .interfaceClass(RaceTestService.class)
	                .version("1.0.0.api")
	                .clientTimeout(3000)
	                .hook((ConsumerHook)new RaceConsumerHook()).instance();
	    }

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 //-----------------------------------------------
		// testException
//        try {
//            Integer result = apiService.throwException();
//        } catch (RaceException e) {
//            Assert.assertEquals("race", e.getFlag());
//            e.printStackTrace();
//        }
		
		System.out.println(apiService.getMap());
		System.out.println("this is a rpc framework".equals(apiService.getString()));
		System.out.println(apiService.getDO());

	     Map<String, Object> resultMap = apiService.getMap();
	     System.out.println(resultMap.containsKey("hook key"));
	     System.out.println(resultMap.containsValue("this is pass by hook"));
//	     
//
//
//	  //-----------------------------------------------
	     // testFutureCall
	     consumer.asynCall("getString");
         String nullValue = apiService.getString();
         try {
            String result = (String) ResponseFuture.getResponse(3000);
            System.out.println("this is a rpc framework asynCall" +result);
         } catch (InterruptedException e) {
            e.printStackTrace();
         } finally {
            consumer.cancelAsyn("getString");
         }
      //--------------------------------------
         // testTimeOut 
	     long beginTime = System.currentTimeMillis();
	     try {
	            boolean result = apiService.longTimeMethod();
	     } catch (Exception e) {
	            long period = System.currentTimeMillis() - beginTime;
	           
	            System.out.println("timeout succ");
	     }
//	 //--------------------------------------------
//	     // testAsynCall
//	     RaceServiceListener listener = new RaceServiceListener();
//	     consumer.asynCall("getDO", listener);
//	     RaceDO nullDo = apiService.getDO();
//	     Assert.assertEquals(null, nullDo);
//	     try {
//	         RaceDO resultDo = (RaceDO) listener.getResponse();
//	     } catch (InterruptedException e) {
//	     }
//	   
//	     System.out.println("Succ");
	}

}
