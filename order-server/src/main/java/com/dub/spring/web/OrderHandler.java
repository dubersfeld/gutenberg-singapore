package com.dub.spring.web;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.dub.spring.domain.EditCart;
import com.dub.spring.domain.Order;
import com.dub.spring.domain.OrderAndBook;
import com.dub.spring.domain.OrderAndState;
import com.dub.spring.domain.UserAndReviewedBooks;
import com.dub.spring.exceptions.OrderNotFoundException;
import com.dub.spring.services.OrderService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class OrderHandler {
	
	@Autowired 
	private OrderService orderService;
	

	public Mono<ServerResponse> getOrderById(ServerRequest request) {
				
		
		final Mono<Order> upOrder = this.transformById(request.pathVariable("orderId"));
		
		return upOrder
				.flatMap(orderSuccess)
				.onErrorResume(orderFallback);
	}
	
	
	public Mono<ServerResponse> createOrder(ServerRequest request) {
		
		final Mono<Order> order = request
				.bodyToMono(Order.class);
				
		
		return order
			.flatMap(createTransform)
			.flatMap(orderSuccess)
			.onErrorResume(orderFallback);	
	}
	
	
	public Mono<ServerResponse> editCart(ServerRequest request) {
		
		
		
		final Mono<EditCart> toto = request.bodyToMono(EditCart.class);
				
	
		return toto
				.flatMap(transformEditCart)
				.flatMap(orderSuccess)
				.onErrorResume(orderFallback);
	}
	
	
	// activeOrder may be null initially
	public Mono<ServerResponse> getActiveOrder(ServerRequest request) {
				
		final Mono<String> toto = request.bodyToMono(String.class);
		
		return toto
				.flatMap(transformGetActiveOrder)
				.flatMap(orderSuccess)
				.onErrorResume(activeOrderFallback);
	}
	
	
	public Mono<ServerResponse> setOrderState(ServerRequest request) {
		
		
		final Mono<OrderAndState> toto = request.bodyToMono(OrderAndState.class);
		
		return toto
				.flatMap(transformSetOrderState)
				.flatMap(orderSuccess)
				.onErrorResume(orderFallback);
	}
	
	
	public Mono<ServerResponse> checkoutOrder(ServerRequest request) {
		
		final Mono<String> toto = request.bodyToMono(String.class);
		
		return toto
				.flatMap(transformCheckoutOrder)
				.flatMap(orderSuccess)
				.onErrorResume(orderFallback);
	}
	
	
	public Mono<ServerResponse> addBookToOrder(ServerRequest request) {
				
		final Mono<OrderAndBook> toto = request.bodyToMono(OrderAndBook.class);
		
		
		return toto
				.flatMap(transformAddBookToOrder)
				.flatMap(orderSuccess)
				.onErrorResume(orderFallback);
	}
	
	
	public Mono<ServerResponse> updateOrder(ServerRequest request) {
		
	
		final Mono<Order> toto = request
									.bodyToMono(Order.class);
	
		return toto
				.flatMap(transformUpdateOrder)
				.flatMap(orderSuccess)
				.onErrorResume(orderFallback); 
	}
	
	
	public Mono<ServerResponse> getBooksNotReviewed(ServerRequest request) {
		
	
		final Mono<UserAndReviewedBooks> toto = request
				.bodyToMono(UserAndReviewedBooks.class);
		
		
		Mono<Flux<String>> bookIds = toto.flatMap(transformBooksNotReviewed);
		
		
		return bookIds
				.flatMap(notReviewedSuccess)
				.onErrorResume(orderFallback);
	}
	
	
	// All utility methods
	
	private Function<UserAndReviewedBooks, Mono<Flux<String>>> transformBooksNotReviewed =
			s -> {
				
				try {
					
					
					return Mono.just(orderService.getBooksNotReviewed(s));// Mono<Flux<String>>
				
				} catch (Exception e) {
					e.printStackTrace();
					
					
					return Mono.error(new RuntimeException("SATOR"));
				}
			};

			
	
	private Function<OrderAndBook, Mono<Order>> transformAddBookToOrder =
		s -> {
			try {
				
				Mono<Order> enclume = orderService.addBookToOrder(s.getOrderId(), s.getBookId());
				
				return enclume.map(ord -> {
					
					return ord;
				});
			
			} catch (Exception e) {
				return Mono.error(new RuntimeException("SATOR"));
			}
		};	
			
	
	private Function<String, Mono<Order>> transformCheckoutOrder =
			s -> {
				try {
					
					Mono<Order> order = orderService.checkoutOrder(s);
					
					return order;
				} catch (Exception e) {
					return Mono.error(new RuntimeException("SATOR"));
				}
			};
	
	
	private Function<OrderAndState, Mono<Order>> transformSetOrderState =
			s -> {
				try {
					return orderService.setOrderState(s.getOrderId(), s.getState());			
				} catch (Exception e) {
					return Mono.error(new RuntimeException("SATOR"));
				}
			};
	
	
	private Function<Order, Mono<Order>> createTransform =
			order -> {
				try {
					return orderService.saveOrder(order, true);		
				} catch (Exception e) {
					e.printStackTrace();
					return Mono.error(new RuntimeException("SATOR"));				
				}
	};
	
	
	private Function<EditCart, Mono<Order>> transformEditCart =
			editCart -> {
				try {
					
					return orderService.editCart(editCart);
				} catch (Exception e) {	
					return Mono.error(new RuntimeException());
				}
	};
	
	
	private Function<String, Mono<Order>> transformGetActiveOrder =
			userId -> {
				try {
					// may be null after a payment
					return orderService.getActiveOrder(userId);
				} catch (OrderNotFoundException e) {
					return Mono.error(e);
				} catch (Exception e) {
					return Mono.error(new RuntimeException("SATOR"));
				}
	};
	
	
	private Function<Order, Mono<Order>> transformUpdateOrder =
			order -> {
				try {
					
					return orderService.saveOrder(order, false);
				} catch (Exception e) {
					return Mono.error(new RuntimeException("SATOR"));
				}
			};
	
			
	private Mono<Order> transformById(String orderId) {
		try {
			
			return orderService.getOrderById(orderId);
		} catch (Exception e) {
			return Mono.error(new RuntimeException());
		}
	}
	
	
	private Function<Throwable, Mono<ServerResponse>> orderFallback =
			error -> {
				
				return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)				
						.build();	  			
	};
	
	
	private Function<Order, Mono<ServerResponse>> orderSuccess =
			order -> {
			
			
				return ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(Mono.just(order), Order.class);	  			
	};
	
	
	private Function<Throwable, Mono<ServerResponse>> activeOrderFallback =
			error -> {
				if (OrderNotFoundException.class == error.getClass()) {
					
					
					
					return ServerResponse.status(HttpStatus.NOT_FOUND)
							.build();
				} else {
					
					
				
					return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.build();
				}
						
	};

	
	private Function<Flux<String>, Mono<ServerResponse>> notReviewedSuccess = 
			s -> {
				
					return ServerResponse.ok()
							.contentType(MediaType.TEXT_EVENT_STREAM)
							//.contentType(MediaType.APPLICATION_JSON)
							.body(s, String.class);
	};
	

}
