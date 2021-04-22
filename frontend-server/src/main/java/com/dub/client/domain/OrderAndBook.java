package com.dub.client.domain;

// wrapper class
public class OrderAndBook {

	String orderId;
	String bookId;
	
	public OrderAndBook() { }
	
	public OrderAndBook(String orderId, String bookId) {
		this.orderId = orderId;
		this.bookId = bookId;
	}
	
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getBookId() {
		return bookId;
	}
	public void setBookId(String bookId) {
		this.bookId = bookId;
	}
	
	
	
}
