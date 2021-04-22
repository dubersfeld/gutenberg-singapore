package com.dub.spring.services;


import static com.dub.spring.controller.DateCorrect.correctDate;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.dub.spring.domain.BookDocument;
import com.dub.spring.domain.BookUser;
import com.dub.spring.domain.EditCart;
import com.dub.spring.domain.Item;
import com.dub.spring.domain.Order;
import com.dub.spring.domain.OrderDocument;
import com.dub.spring.domain.OrderState;
import com.dub.spring.domain.UserAndReviewedBooks;
import com.dub.spring.exceptions.OrderException;
import com.dub.spring.exceptions.OrderNotFoundException;
import com.dub.spring.repository.BookRepository;
import com.dub.spring.repository.OrderRepository;
import com.dub.spring.utils.OrderUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
public class OrderServiceImpl implements OrderService {

	@Value("${baseBooksUrl}")
	private String baseBooksURL;
	
	@Autowired
	private BookRepository bookRepository;
	
	@Autowired
	private OrderRepository orderRepository;
		
	@Autowired
	private ReactiveMongoOperations reactiveMongoOperations;
	
	//private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public Flux<String> getBooksNotReviewed(UserAndReviewedBooks userAndReviewedBooks) throws ParseException {
		
	
		// preparing an aggregation on orders collection
		
		UnwindOperation unwind = unwind("lineItems");
		
		// set up limit date in application.properties or better add an admin page
		LocalDateTime limitDate = LocalDateTime.of(2017, 
                Month.APRIL, 24, 19, 30, 40);
		
		/** Here I want all orders shipped to the given user */
		MatchOperation match1 = match(Criteria.where("state").is("SHIPPED")
											.and("date").gte(limitDate)
											.and("userId").is(new ObjectId(userAndReviewedBooks.getUserId())));
			
		/** Here I want all books not already reviewed by the given user */
		MatchOperation match2 = match(Criteria.where("bookId").nin(userAndReviewedBooks.getReviewedBookIds()));
		
		GroupOperation group = group("bookId").last("userId").as("userId");			
		
		ProjectionOperation proj1 = project("lineItems", "userId");
		ProjectionOperation proj2 = project("userId").and("bookId").previousOperation();
		ProjectionOperation projAlias = project("userId")							
					.and("lineItems.bookId").as("bookId");
		
		LimitOperation limitOp = limit(userAndReviewedBooks.getOutLimit());
			
		Aggregation aggregation = newAggregation(match1, proj1, unwind, projAlias, group, proj2, match2, limitOp);
	
		Flux<BookUser> bookUsers = reactiveMongoOperations.aggregate(
				aggregation, "orders", BookUser.class);
			
		Flux<String> bookIds = bookUsers.map(b -> b.getBookId());
		
	
		
		return bookIds;
		
	}

	@Override
	public Mono<Order> saveOrder(Order order, boolean creation) {
		if (!creation) {
			//check for presence if not creation
			this.getRawOrder(order.getId());
			
			Mono<OrderDocument> doc = 
					orderRepository.save(OrderUtils.orderToDocument(order));	
			return doc.map(OrderUtils::documentToOrder);
		} else {
			// creation
			Mono<OrderDocument> newDoc = 
				orderRepository.save(OrderUtils.orderToDocument(order));
			return newDoc.map(OrderUtils::documentToOrder);
		}
	}

	@Override
	public Mono<Order> getOrderById(String orderId) {
		
		Mono<OrderDocument> order = orderRepository.findById(orderId);
		
		return order.hasElement().flatMap(p -> {
			if (p) {
				return this.recalculateTotalAlt2(order).map(OrderUtils::documentToOrder);
			} else {
				return Mono.error(new OrderNotFoundException());
			}
		});
	}

	@Override
	public Mono<Order> getActiveOrder(String userId) {
	
		ObjectId userObj = new ObjectId(userId);
		
		Query query = new Query();
		query.addCriteria(Criteria.where("userId").is(userObj)
											.and("state").nin("PRE_SHIPPING", "SHIPPED"));
		// can be null
		Mono<OrderDocument> doc = reactiveMongoOperations.findOne(query, OrderDocument.class);
		
		return doc.map(OrderUtils::documentToOrder);
	}

	@Override
	public Mono<Order> addBookToOrder(String orderId, String bookId) {

	
		
		Mono<OrderDocument> oldOrder = this.orderRepository.findById(orderId);
			
	
	
		Mono<OrderDocument> upOrder = oldOrder.map(ord -> {
			List<Item> items = ord.getLineItems();
			
			
			// check if bookId already present
			boolean present = false;
			
			for (Item item : items) {
				if (item.getBookId().equals(bookId)) {
					present = true;
					item.setQuantity(item.getQuantity()+1);
				}
			}
			
			if (!present)  {
			
				// add a new Item
				items.add(new Item(bookId, 1));
			}
			
			ord.setLineItems(items);
			
			
			return ord;
		});
		
		// then recalculate
		Mono<OrderDocument> upOrder2 = recalculateTotalAlt2(upOrder);
				
		// finally save order
		return this.orderRepository.saveAll(upOrder2).next()
				.map(OrderUtils::documentToOrder);
	}

	@Override
	public Mono<Order> editCart(EditCart editCart) {
		
		
		List<Item> items = editCart.getItems();
		String orderId = editCart.getOrderId();
		
		
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(orderId));
		
		Update update = new Update();
		update.set("lineItems", items);
				
		Mono<OrderDocument> doc = reactiveMongoOperations.findAndModify(query, update,
				new FindAndModifyOptions().returnNew(true),
				OrderDocument.class);
		
	
			
		doc.hasElement().subscribe(b -> System.err.println("doc hasElem " + b));
		// recalculate needed after change
		Mono<OrderDocument> order = this.recalculateTotalAlt2(doc);
		
		order.subscribe(or -> System.err.println("TENET " + or.getLineItems().size()));
				
		return order.map(OrderUtils::documentToOrder);
		
	}

	@Override
	public Mono<Order> setOrderState(String orderId, OrderState state) {
		
		FindAndModifyOptions options = new FindAndModifyOptions();
		options.returnNew(true);
		
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(orderId));
											
		Update update = new Update();
		update.set("state", state);
		
		Mono<Order> oldOrder = this.getOrderById(orderId);
		
		Mono<Order> grunge = oldOrder.flatMap(ord -> {
			
			OrderState oldState = ord.getState();
			// legal transitions only
			switch (state) {
			case CART:
				if (oldState.equals(OrderState.SHIPPED) ||
						oldState.equals(OrderState.PRE_SHIPPING)) {
					return Mono.error(new OrderException());
				}
				break;
			case PRE_AUTHORIZE:
				if (oldState.equals(OrderState.SHIPPED) ||
						oldState.equals(OrderState.PRE_SHIPPING)) {
					//throw new OrderException();
					return Mono.error(new OrderException());
				}
				break;
			case PRE_SHIPPING:
				if (oldState.equals(OrderState.SHIPPED)) {
					//throw new OrderException();
					return Mono.error(new OrderException());
				}
				break;
			default:
				return Mono.error(new OrderException());
				//throw new OrderException();// should not be here
			}
			
			// legal case
			Mono<OrderDocument> doc = reactiveMongoOperations.findAndModify(query, update, options, OrderDocument.class);
			
			return doc.map(OrderUtils::documentToOrder);
		
		});
		
		return grunge;
	}

	@Override
	public Mono<Order> checkoutOrder(String orderId) {
		
		Query query = new Query();	
		query.addCriteria(Criteria.where("id").is(orderId).and("state").is(OrderState.CART));
				
		Update update = new Update();
		update.set("state", OrderState.PRE_AUTHORIZE);
				
		Mono<OrderDocument> doc = reactiveMongoOperations.findAndModify(query, update, new FindAndModifyOptions().returnNew(true), OrderDocument.class);
		
		
		
		return doc.map(OrderUtils::documentToOrder);		
	}

	
	// utility methods
	private Mono<Order> getRawOrder(String orderId) {
		Mono<OrderDocument> order = orderRepository.findById(orderId);
		
		//Mono<Boolean> isPresent = order.hasElement();
		
		return order.hasElement().flatMap(p -> {
				return p ? 
					order.map(OrderUtils::documentToOrder) :
					Mono.error(new OrderNotFoundException());
		});
	}
	
	
	/** Rewrite more compact before next release */
	private Mono<OrderDocument> recalculateTotalAlt2(Mono<OrderDocument> order) {
		
		Mono<Integer> total = order.flatMap(ord -> {
			Flux<Item> items = Flux.fromIterable(ord.getLineItems());
			
			Flux<String> bookIds = items.map(it -> {
			
			
				return it.getBookId();
			}); 
			
			Flux<BookDocument> books = bookRepository.findAllById(bookIds);
			 
			Flux<Integer> prices = books.map(book -> {
				return book.getPrice();});

			Flux<Integer> quantities = items.map(it -> {
				return it.getQuantity();});

			Flux<Tuple2<Integer, Integer>> grunges = Flux.zip(prices, quantities);

			Flux<Integer> groubles = grunges.map(gr -> gr.getT1() * gr.getT2());
					
			return groubles.reduce(0, (x1, x2) -> x1 + x2);
			
		});
		
		Mono<Tuple2<OrderDocument,Integer>> fourbi = Mono.zip(order, total);
		
		return fourbi.map(t -> {
			OrderDocument ord = t.getT1();
			ord.setSubtotal(t.getT2());
			
			
			return ord;
		});		
	}
	
	
}
