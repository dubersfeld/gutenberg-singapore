package com.dub.spring;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.dub.spring.domain.EditCart;
import com.dub.spring.domain.Item;
import com.dub.spring.domain.Order;
import com.dub.spring.domain.OrderAndBook;
import com.dub.spring.domain.OrderAndState;
import com.dub.spring.domain.OrderState;
import com.dub.spring.domain.UserAndReviewedBooks;
import com.dub.spring.services.OrderService;
import com.dub.spring.web.OrderHandler;
import com.dub.spring.web.OrderRouter;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


@ActiveProfiles("test")
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"eureka.client.enabled=false"})
public class OrderHandlerTest {

	@Autowired
	OrderRouter orderRouter;
	
	@Autowired
	OrderHandler orderHandler;
	
	Order newOrder = new Order();
	
	//DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
	
	private Predicate<Order> createOrderPred = 
			checkOrder -> 
				"CART".equals(checkOrder.getState().toString())
						&& "5a28f2b0acc04f7f2e9740ae".equals(checkOrder.getUserId());
				
	private Predicate<Order> orderByIdPred = 
			order -> { 
				System.err.println(order.getUserId());
				return "5a28f32dacc04f7f2e9740b4".equals(order.getUserId()) &&
					OrderState.SHIPPED.equals(order.getState());
			};
		
	private Predicate<Order> editCartPred =
					checkOrder -> {
						
						return this.matches(checkOrder.getLineItems(), new Item("5a28f2b0acc04f7f2e9740a2",3));				
	};
	
	private Predicate<Order> getActiveOrderPred =
			checkOrder -> {
				//System.err.println(checkOrder.getLineItems().size());
				return OrderState.CART.equals(checkOrder.getState())
						&& "5a28f364acc04f7f2e9740b4".equals(checkOrder.getUserId());				
	};
	
	private Predicate<Order> setOrderStatePred =
			checkOrder -> {
				System.err.println(checkOrder.getUserId());
				return OrderState.PRE_SHIPPING.equals(checkOrder.getState())
						&& "5a28f366acc04f7f2e9740e0".equals(checkOrder.getId());				
	};
	
	private Predicate<Order> checkoutOrderPred =
			checkOrder -> {
				
				return OrderState.PRE_AUTHORIZE.equals(checkOrder.getState())
						&& "5a28f364acc04f7f2e9740b8".equals(checkOrder.getUserId());				
	};
	
	private Predicate<Order> addBookToOrderPred =
			checkOrder -> {
				
				return ("CART".equals(checkOrder.getState().toString()))
						&& "5a28f364acc04f7f2e9740b9".equals(checkOrder.getUserId())
						&& this.matches(checkOrder.getLineItems(), new Item("5a28f2b0acc04f7f2e9740a8",1));
	};
	
	private Predicate<String> booksNotReviewedPred = 
			string -> {
				
				return "5a28f2b0acc04f7f2e9740a1".equals(string);
	};
		
	
	@Test
	void getBooksNotReviewedTest() {
		UserAndReviewedBooks urb = new UserAndReviewedBooks();  
		urb.setUserId("5a28f2b9acc04f7f2e9740b1");
		urb.setReviewedBookIds(new ArrayList<>());
		//urb.setReviewedBookIds(Arrays.asList("5a28f2b0acc04f7f2e9740ac"));
		urb.setOutLimit(10);
			
		WebTestClient
		.bindToRouterFunction(orderRouter.route(orderHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/getBooksNotReviewed")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(urb), UserAndReviewedBooks.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(String.class)
		.getResponseBody()// it is a Flux<Order>	
		.as(StepVerifier::create)
		//.expectNextMatches(booksNotReviewedPred)
		.expectNextCount(3)
		.expectComplete()
		.verify();	
	}
	
	
	private boolean matches(List<Item> list, Item check) {
		boolean match = false;
		for (Item item : list) {
			if (item.getBookId().equals(check.getBookId()) &&
					item.getQuantity() == check.getQuantity()) {
				match = true;
				break;
			}
		}
		return match;
	}
	
	
}
