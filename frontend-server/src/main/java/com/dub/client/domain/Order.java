package com.dub.client.domain;

import java.io.Serializable;
//import java.time.Date;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;


/** 
 * In the frontend all database details should be hidden 
 * */
// POJO, not document
public class Order implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2623188979726019146L;

	private String id;
	
	private String userId;
	
	private OrderState state;
	private List<Item> lineItems;
	private Address shippingAddress;
	private PaymentMethod paymentMethod;
	private int subtotal;
	
	
	//@Field(type = FieldType.Date, store = true,
	//		format = DateFormat.custom, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS")// used for create and read
	//@JsonFormat(shape = JsonFormat.Shape.STRING, pattern ="yyyy-MM-dd'T'HH:mm:ss.SSS")
	
	private LocalDateTime date;
	
	public Order() {
		this.lineItems = new ArrayList<>();
		this.shippingAddress = new Address();
		this.paymentMethod = new PaymentMethod();
	}

	
	public int getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(int subtotal) {
		this.subtotal = subtotal;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setState(OrderState state) {
		this.state = state;
	}

	public OrderState getState() {
		return state;
	}


	public List<Item> getLineItems() {
		return lineItems;
	}


	public void setLineItems(List<Item> lineItems) {
		this.lineItems = lineItems;
	}

	public Address getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(Address shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}


	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}


	public LocalDateTime getDate() {
		return date;
	}


	public void setDate(LocalDateTime date) {
		this.date = date;
	}


	

}
