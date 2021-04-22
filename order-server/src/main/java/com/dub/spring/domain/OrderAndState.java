package com.dub.spring.domain;

//wrapper class
public class OrderAndState {

	String orderId;
	OrderState state;
	
	public OrderAndState() { }
	
	public OrderAndState(String orderId, OrderState state) {
		this.orderId = orderId;
		this.state = state;
	}
	
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public OrderState getState() {
		return state;
	}
	public void setState(OrderState state) {
		this.state = state;
	}
	
	
	
}
