package com.dub.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.dub.spring.domain.Review;
import com.dub.spring.domain.ReviewDocument;
import com.dub.spring.services.ReviewService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ActiveProfiles("test")
@SpringBootTest(properties = {"eureka.client.enabled=false"})
public class ReviewServiceTest {
	
	@Autowired
	private ReviewService reviewService;
	
	private Predicate<Review> testByUserId = 
			review -> {
				System.err.println(review.getText());
				return review.getText().contains("everything HareFAQ");
	};
			
	private Predicate<Review> testByBookId = 
			review -> {
				System.err.println(review.getText());
				return review.getText().contains("the most controversial scientist");
	};
		
	
	@Test
	void testCreateReview() {	     
		Review review = new Review();
		review.setBookId("5a28f2b0acc04f7f2e97409f");
		review.setUserId("5a28f2b9acc04f7f2e9740b1");
		review.setText("Lorem ipsum");
		review.setRating(3);
		
		// actual creation
		Mono<Review> check = this.reviewService.createReview(review);
		Review checkReview = check.block(); 
		
		assertEquals("5a28f2b0acc04f7f2e97409f", checkReview.getBookId());
		assertEquals("5a28f2b9acc04f7f2e9740b1", checkReview.getUserId());
		assertEquals("Lorem ipsum", checkReview.getText());
		assertEquals(3, checkReview.getRating());
	}
	

	@Test
	void testReviewById() {
		String reviewId = "5a28f366acc04f7f2e9740b8";
		Review review = this.reviewService.getReviewById(reviewId).block();
		assertTrue(review.getText().contains("everything HareFAQ"));		
	}
	
	
	@Test
	void testReviewsByUserId() {
		String userId = "5a28f306acc04f7f2e9740b3";
		Flux<Review> reviews = this.reviewService.getReviewsByUserId(userId);
		StepVerifier.create(reviews.log())
		.expectNextMatches(testByUserId)
		.expectNextCount(3)
		.verifyComplete();		
	}
	
	
	@Test
	void testReviewsByBookId() {
		String bookId = "5a28f2b0acc04f7f2e9740a5";
		Flux<Review> reviews = this.reviewService.getReviewByBookId(bookId, "rating");
		StepVerifier.create(reviews.log())
		.expectNextMatches(testByBookId)
		.expectNextCount(2)
		.verifyComplete();		
		
	}
	

	@Test
	void testBookRating() {
		String bookId = "5a28f2b0acc04f7f2e9740a5";
		Double rating = this.reviewService.getBookRating(bookId).block();
		//rating may be null
		assertTrue(rating < 2.7 && rating > 2.6);	
	}
	

	@Test
	void testBookRatingEmpty() {
		String bookId = "5a28f2b0acc04f7f2e976666";
		Double rating = this.reviewService.getBookRating(bookId).block();
		assertTrue(rating == null);	
	}

	
	@Test
	void testVoteHelpful() {
		String reviewId = "5a28f366acc04f7f2e9740b9";
		String userId = "5a28f306acc04f7f2e9740b3";
			
		Mono<Boolean> status = this.reviewService.voteHelpful(reviewId, userId, true);
			
		assertTrue(status.block());
		
	}


	@Test
	void testVoteHelpfulConflict() {
		String reviewId = "5a28f366acc04f7f2e9740ba";
		String userId = "5a28f306acc04f7f2e9740b3";
			
		Mono<Boolean> status = this.reviewService.voteHelpful(reviewId, userId, true);
			
		assertFalse(status.block());
		
	}
	
}
