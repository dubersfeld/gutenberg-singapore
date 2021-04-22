package com.dub.spring.services;

import com.dub.spring.domain.Review;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReviewService {

	Mono<Review> createReview(Review review);	
	
	Mono<Review> getReviewById(String reviewId);	
	
	Flux<Review> getReviewsByUserId(String userId);
	
	Flux<Review> getReviewByBookId(
								String bookId, 
								String sortBy);
	
	Mono<Double> getBookRating(String bookId);
	
	Mono<Boolean> voteHelpful(String reviewId, String userId, boolean helpful);

}
