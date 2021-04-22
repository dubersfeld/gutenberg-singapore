package com.dub.client.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.dub.client.domain.Address;
import com.dub.client.domain.EditCart;
import com.dub.client.domain.Item;
import com.dub.client.domain.Order;
import com.dub.client.domain.OrderAndBook;
import com.dub.client.domain.OrderAndState;
import com.dub.client.domain.OrderState;
import com.dub.client.domain.PaymentMethod;
import com.dub.client.exceptions.OrderNotFoundException;
import com.dub.client.exceptions.UnknownServerException;

import reactor.core.publisher.Mono;


/**
 * Try to reduce code duplication by creating a unique function and call it in flatMap
 * */

@Service
public class OrderServiceImpl implements OrderService {

	private static final String UPDATE_ORDER = "/updateOrder"; 
	private static final String CREATE_ORDER = "/createOrder"; 
	private static final String EDIT_CART = "/editCart"; 
	private static final String ORDER_BY_ID = "/orderById/"; 
	private static final String ADD_BOOK_TO_ORDER = "/addBookToOrder"; 
	private static final String GET_ACTIVE_ORDER = "/getActiveOrder"; 
	private static final String CHECKOUT_ORDER = "/checkoutOrder"; 
	private static final String SET_ORDER_STATE = "/setOrderState"; 
	
	
	@Autowired
	private WebClient orderClient;
		
	
	@Override
	public Order saveOrder(Order order) {
		// take this implementation as a reference
		
		WebClient.ResponseSpec enclume = orderClient
				.method(HttpMethod.POST)
				.uri(UPDATE_ORDER)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.body(Mono.just(order), Order.class)
				.retrieve();
		Mono<ResponseEntity<Order>> forge = enclume.toEntity(Order.class);
			
		Mono<Order> grunge = forge.flatMap(catchErrorsAndTransform2);
	
		return grunge.block();
	}

	
	@Override
	public Order createOrder(Order order) {
		
		WebClient.ResponseSpec enclume = orderClient
						.method(HttpMethod.POST)
						.uri(CREATE_ORDER)
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.body(Mono.just(order), Order.class)
						.retrieve();
				
		return enclume
				.toEntity(Order.class)
				.flatMap(catchErrorsAndTransform2)
				.block();	
	}

	
	@Override
	public Order addBookToOrder(String orderId, String bookId) {
				
		OrderAndBook orderAndBook = new OrderAndBook(orderId, bookId);
				
		WebClient.ResponseSpec enclume = orderClient
						.method(HttpMethod.POST)
						.uri(ADD_BOOK_TO_ORDER)
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.body(Mono.just(orderAndBook), OrderAndBook.class)
						.retrieve();
				
		return enclume
				.toEntity(Order.class)
				.flatMap(catchErrorsAndTransform2)
				.block();	
	}

	
	/** caution: getActiveOrder may return null initially */
	@Override
	public Optional<Order> getActiveOrder(String userId) {
		
		WebClient.ResponseSpec enclume = orderClient
				.method(HttpMethod.POST)
				.uri(GET_ACTIVE_ORDER)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
				.body(Mono.just(userId), String.class)
				.retrieve();
		
		Order order = enclume.bodyToMono(Order.class).block();
		return Optional.ofNullable(order);
	}

	@Override
	public Order checkoutOrder(String orderId) {
			
		WebClient.ResponseSpec enclume = orderClient
				.method(HttpMethod.POST)
				.uri(CHECKOUT_ORDER)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
				.body(Mono.just(orderId), String.class)
				.retrieve();
		
		return enclume
				.toEntity(Order.class)
				.flatMap(catchErrorsAndTransform2)
				.block();	
	}

	
	@Override
	public Order setCart(String orderId) {
		
		OrderAndState orderAndState = new OrderAndState(orderId, OrderState.CART);
			
		WebClient.ResponseSpec enclume = orderClient
				.method(HttpMethod.POST)
				.uri(SET_ORDER_STATE)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
				.body(Mono.just(orderAndState), OrderAndState.class)
				.retrieve();	
		
		return enclume
				.toEntity(Order.class)
				.flatMap(catchErrorsAndTransform2)
				.block();	
	}
	
	@Override
	public Order getOrderById(String orderId) {
			
		WebClient.ResponseSpec enclume = orderClient
				.method(HttpMethod.GET)
				.uri(ORDER_BY_ID + orderId)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.retrieve();
		
		return enclume
				.toEntity(Order.class)
				.flatMap(catchErrorsAndTransform2)
				.block();	
	}

	@Override
	public Order editOrder(String orderId, List<Item> items) {
			
		// encapsulation
		EditCart editCart = new EditCart(orderId, items);
			
		WebClient.ResponseSpec enclume = orderClient
				.method(HttpMethod.POST)
				.uri(EDIT_CART)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.body(Mono.just(editCart), EditCart.class)
				.retrieve();
			
		return enclume
				.toEntity(Order.class)
				.flatMap(catchErrorsAndTransform2)
				.block();	
	}
		
	
	@Override
	public Order setOrderState(String orderId, OrderState state) {
				
		OrderAndState orderAndState = new OrderAndState(orderId, state);
	
		WebClient.ResponseSpec enclume = orderClient
				.method(HttpMethod.POST)
				.uri(SET_ORDER_STATE)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.body(Mono.just(orderAndState), OrderAndState.class)
				.retrieve();
					
		return enclume
				.toEntity(Order.class)
				.flatMap(catchErrorsAndTransform2)
				.block();	
	}
		
	
	@Override
	public Order finalizeOrder(Order order, Address shippingAddress, PaymentMethod payMeth) {
		// not an HTTP request  
		order.setDate(LocalDateTime.now());
		order.setState(OrderState.PRE_SHIPPING);
		order.setPaymentMethod(payMeth);
		order.setShippingAddress(shippingAddress);
		
		return order;
	}
	
	
	Function<ResponseEntity<Order>, Mono<Order>> catchErrorsAndTransform2 = 
			(ResponseEntity<Order> clientResponse) -> {
						
				if (clientResponse.getStatusCode().is5xxServerError()) {
					throw new UnknownServerException();
				} else if (clientResponse.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
					throw new OrderNotFoundException();
				} else {
					return Mono.just(clientResponse.getBody());
				}
				
	};
	
}
