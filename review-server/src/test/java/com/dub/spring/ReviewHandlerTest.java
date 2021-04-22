package com.dub.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.dub.spring.domain.Review;
import com.dub.spring.domain.ReviewVote;
import com.dub.spring.services.ReviewService;
import com.dub.spring.web.ReviewHandler;
import com.dub.spring.web.ReviewRouter;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"eureka.client.enabled=false"})
public class ReviewHandlerTest {

	@Autowired
	ReviewRouter reviewRouter;
	
	@Autowired
	ReviewHandler reviewHandler;
	
	@Autowired
	ReviewService reviewService;
	
	
	private Predicate<Review> reviewsByUserIdPred = 
			review -> review.getText().contains("everything HareFAQ");
				
	
	private Predicate<Review> reviewsByBookIdPred = 	
			review -> {
				System.err.println(review.getText());
				return review.getText().contains("the most controversial scientist");
					
					};
					
			
	private Predicate<Review> reviewByIdPred = 
			review -> "5a28f2b0acc04f7f2e97409f".equals(review.getBookId()) &&
					"5a28f306acc04f7f2e9740b3".equals(review.getUserId());

	private Predicate<Double> bookRatingPred = 
					rating -> (2.6 < rating && rating < 2.7);
						
				
	@Test
	void reviewsByUserId() {
		String userId = "5a28f306acc04f7f2e9740b3";
		WebTestClient
		.bindToRouterFunction(reviewRouter.route(reviewHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/reviewsByUserId")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(userId), String.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Review.class)
		.getResponseBody()// it is a Flux<Review>
		.as(StepVerifier::create)
		.expectNextMatches(reviewsByUserIdPred)
		.expectNextCount(3)
		.expectComplete()
		.verify();	
	}	
	
	
	@Test
	void reviewsByUserIdNotFound() {
		String userId = "5a28f306acc04f7f2e976666";
		WebTestClient
		.bindToRouterFunction(reviewRouter.route(reviewHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/reviewsByUserId")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(userId), String.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Review.class)
		.getResponseBody()// it is a Flux<Review>
		.as(StepVerifier::create)
		.expectComplete()// no reviews found with this userId
		.verify();	
	}	
	
	
	@Test
	void testByBookId() {
		WebTestClient
		.bindToRouterFunction(reviewRouter.route(reviewHandler))
		.build()
		.method(HttpMethod.GET)
		.uri("/reviewsByBookId/5a28f2b0acc04f7f2e9740a5/sort/rating")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Review.class)
		.getResponseBody()// it is a Flux<Review>
		.as(StepVerifier::create)
		.expectNextMatches(reviewsByBookIdPred)
		.expectNextCount(2)
		.expectComplete()
		.verify();	
	}	
	
	
	@Test
	void testCreateReview() {
		Review review = new Review();
		review.setBookId("5a28f2b0acc04f7f2e9740a9");
		review.setUserId("5a28f364acc04f7f2e9740b7");
		review.setText("Lorem ipsum");
		review.setRating(3);
		
		HttpHeaders headers = WebTestClient
		.bindToRouterFunction(reviewRouter.route(reviewHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/createReview")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(review), Review.class)
		.exchange()
		.expectStatus().isCreated()
		.returnResult(String.class)
		.getResponseHeaders();// HttpHeaders
		
		
		String location = headers.get("location").get(0);
		String patternString = "^http://localhost:8082/reviewById/[A-Za-z0-9_-]*$";  
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(location);
		assertEquals(true, matcher.matches());
	}	
	
	
	@Test
	void testVoteHelpful() {
		String reviewId = "5a28f366acc04f7f2e9740c6";
		String userId = "5a28f32dacc04f7f2e9740b4";
		ReviewVote reviewVote = new ReviewVote();
		reviewVote.setHelpful(true);
		reviewVote.setUserId(userId);
			
		WebTestClient
		.bindToRouterFunction(reviewRouter.route(reviewHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/addVote/" + reviewId)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(reviewVote), ReviewVote.class)
		.exchange()
		.expectStatus().isOk();
	}	
	
	
	@Test
	void testVoteHelpfulConflict() {
		String reviewId = "5a28f366acc04f7f2e9740ba";
		String userId = "5a28f306acc04f7f2e9740b3";
		ReviewVote reviewVote = new ReviewVote();
		reviewVote.setHelpful(true);
		reviewVote.setUserId(userId);
			
		WebTestClient
		.bindToRouterFunction(reviewRouter.route(reviewHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/addVote/" + reviewId)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(reviewVote), ReviewVote.class)
		.exchange()
		.expectStatus().isEqualTo(HttpStatus.CONFLICT);// userId has already voted
	
	}	
	
	
	@Test
	void testById() {
		String reviewId = "5a28f366acc04f7f2e9740b8";

		WebTestClient
		.bindToRouterFunction(reviewRouter.route(reviewHandler))
		.build()
		.method(HttpMethod.GET)
		.uri("/reviewById/" + reviewId)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Review.class)
		.getResponseBody()// it is a Flux<Review>
		.as(StepVerifier::create)
		.expectNextMatches(reviewByIdPred)
		.expectComplete()
		.verify();	
	}	
	
	
	@Test
	void testBookRating() {
		String bookId = "5a28f2b0acc04f7f2e9740a5";
		WebTestClient
		.bindToRouterFunction(reviewRouter.route(reviewHandler))
		.build()
		.method(HttpMethod.GET)
		.uri("/bookRating/" + bookId)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Double.class)
		.getResponseBody()// it is a Flux<Double>
		.as(StepVerifier::create)
		.expectNextMatches(bookRatingPred)
		.expectComplete()
		.verify();	
	}	
	
	
	@Test
	void testBookRatingNotFound() {
		String bookId = "5a28f2b0acc04f7f2e976666";
		WebTestClient
		.bindToRouterFunction(reviewRouter.route(reviewHandler))
		.build()
		.method(HttpMethod.GET)
		.uri("/bookRating/" + bookId)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Double.class)
		.getResponseBody()// it is a Flux<Double>
		.as(StepVerifier::create)
		.expectComplete()// rating not available
		.verify();	
	}
	
}
