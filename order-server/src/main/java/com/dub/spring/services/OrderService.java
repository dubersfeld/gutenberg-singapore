package com.dub.spring.services;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.dub.spring.domain.Address;
import com.dub.spring.domain.EditCart;
import com.dub.spring.domain.Order;
import com.dub.spring.domain.OrderState;
import com.dub.spring.domain.PaymentMethod;
import com.dub.spring.domain.UserAndReviewedBooks;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {
	
	Flux<String> getBooksNotReviewed(UserAndReviewedBooks userAndReviewedBooks) 
											throws ParseException;
	
	Mono<Order> saveOrder(Order order, boolean creation);
	
	Mono<Order> getOrderById(String orderId);
	
	Mono<Order> getActiveOrder(String userId);// Not in PRE_SHIPPING or SHIPPED state

	Mono<Order> addBookToOrder(String orderId, String bookId);
	
	Mono<Order> editCart(EditCart editCart);
	
	Mono<Order> setOrderState(String orderId, OrderState state);
	
	Mono<Order> checkoutOrder(String orderId);
	
}
