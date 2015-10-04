package com.alibaba.middleware.race.rpc.model;

import java.io.Serializable;

public class RpcRequest implements Serializable{
	static private final long serialVersionUID = -4364536436151723421L;

	private String methodName;
	private Object[] parameters;
	private Object props;

	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
	public Object[] getParameters() {
		return parameters;
	}
	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}
	
	public Object getProps() {
		return props;
	}
	public void setProps(Object props) {
		this.props = props;
	}


}
