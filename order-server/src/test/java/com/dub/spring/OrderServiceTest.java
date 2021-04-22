package com.dub.spring;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.dub.spring.domain.EditCart;
import com.dub.spring.domain.Item;
import com.dub.spring.domain.Order;
import com.dub.spring.domain.OrderState;
import com.dub.spring.domain.UserAndReviewedBooks;
import com.dub.spring.services.OrderService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ActiveProfiles("test")
@SpringBootTest(properties = {"eureka.client.enabled=false"})
public class OrderServiceTest {
	
	@Autowired
	private OrderService orderService;
	
	private Predicate<Order> orderByIdPred = 
			order -> ("SHIPPED".equals(order.getState().toString()));

	private Predicate<String> booksNotReviewedPred = 
					string -> {
						
						return "5a28f2b0acc04f7f2e9740a1".equals(string);
					};

			
	@Test
	void testById() {
		String orderId = "5a28f366acc04f7f2e9740cc";
		Mono<Order> user = this.orderService.getOrderById(orderId);
		StepVerifier.create(user.log())
		.expectNextMatches(orderByIdPred)
		.verifyComplete();
	}
	
	
	@Test
	void testCreateOrder() {
		Order newOrder = new Order();	
		newOrder.setDate(LocalDateTime.now());
		newOrder.setState(OrderState.CART);
		newOrder.setUserId("5a28f364acc04f7f2e9740b7");
		
		// actual creation
		Order checkOrder = this.orderService.saveOrder(newOrder, true).block();
	
		assertTrue(OrderState.CART.equals(checkOrder.getState()));
	}
	
	
	@Test
	void testActiveOrder() {
		String userId = "5a28f364acc04f7f2e9740b7";	
		
		Order activeOrder = this.orderService.getActiveOrder(userId).block();
		assertTrue(activeOrder != null);
		assertTrue(OrderState.CART.equals(activeOrder.getState()) && activeOrder.getLineItems().size() == 1);
		
	}
	
	
	@Test
	void testAddBookToOrder() {
		String orderId = "5a28f366acc04f7f2e9740da";
		String bookId = "5a28f2b0acc04f7f2e97409f";
		
		Order activeOrder = this.orderService.addBookToOrder(orderId, bookId).block();
		
		List<Item> items = activeOrder.getLineItems();
		
		assertTrue(itemMatch(items, bookId));
		
		
	}
	
	
	@Test
	void testEditCart() {
		List<Item> items = new ArrayList<Item>();
		items.add(new Item("5a28f2b0acc04f7f2e9740a2", 2));
		EditCart editCart = new EditCart();
		editCart.setOrderId("5a28f366acc04f7f2e9740db");
		editCart.setItems(items);
			
		Order activeOrder = this.orderService.editCart(editCart).block();
		
		List<Item> checkItems = activeOrder.getLineItems();
		
		System.err.println(checkItems.size() + checkItems.get(0).getBookId());
	
		assertTrue(checkItems.size() == 1);
		assertTrue("5a28f2b0acc04f7f2e9740a2".equals(checkItems.get(0).getBookId()));
	}
	
	
	@Test
	void testSetOrderState() {
		String orderId = "5a28f366acc04f7f2e9740dc";
		OrderState state = OrderState.PRE_SHIPPING;
		
		Order checkOrder = this.orderService.setOrderState(orderId, state).block();
		
		assertTrue(OrderState.PRE_SHIPPING.equals(checkOrder.getState()));
	}
	
	
	@Test
	void testCheckoutOrder() {
		String orderId = "5a28f366acc04f7f2e9740dd";
		OrderState state = OrderState.PRE_AUTHORIZE;
		
		Order checkOrder = this.orderService.setOrderState(orderId, state).block();
		
		assertTrue(OrderState.PRE_AUTHORIZE.equals(checkOrder.getState()));
	}
		
	
	
	@Test
	void testBooksNotReviewed() throws ParseException {
		UserAndReviewedBooks urb = new UserAndReviewedBooks();  
		urb.setUserId("5a28f2b9acc04f7f2e9740b1");
		urb.setReviewedBookIds(Arrays.asList("5a28f2b0acc04f7f2e9740ac"));
		urb.setOutLimit(10);
		Flux<String> bookIds = this.orderService.getBooksNotReviewed(urb);
		StepVerifier.create(bookIds.log())
		.expectNextMatches(booksNotReviewedPred)
		.expectNextCount(1)
		.verifyComplete();		
	}
	
	private boolean itemMatch(List<Item> items, String bookId) {
		
		boolean match = false;
		for (Item item : items) {
			if (bookId.equals(item.getBookId())) {
				match = true;
				break;
			}
		}
		return match;
	}
	
}
