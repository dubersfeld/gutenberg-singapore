package com.dub.spring.web;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.dub.spring.domain.Review;
import com.dub.spring.domain.ReviewVote;
import com.dub.spring.services.ReviewService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ReviewHandler {
	
	@Value("${baseReviewsUrl}")
	private String baseReviewsURL;
	
	@Autowired
	private ReviewService reviewService;
	
	
	public Mono<ServerResponse> getReviewsByUserId(ServerRequest request) {
		
		
			
		final Mono<String> toto = request
				.bodyToMono(String.class);
		
	
		
		Mono<Flux<Review>> reviews = toto.flatMap(transformByUserId);
		
		return reviews
				.flatMap(searchSuccess)
				.onErrorResume(searchFallback);
	}
	
		
	public Mono<ServerResponse> getReviewsByBookId(ServerRequest request) {
	
		
		Mono<Flux<Review>> reviews = transformByBookId(
								request.pathVariable("bookId"),
								request.pathVariable("sort"));
		
		return reviews.flatMap(searchSuccess)
				.onErrorResume(searchFallback);
	}
	
	
	public Mono<ServerResponse> createReview(ServerRequest request) {
	
		final Mono<Review> toto = request
					.bodyToMono(Review.class);
					
		Mono<URI> location = toto.flatMap(transformCreate);
			
		return location
					.flatMap(finishCreate)
					.onErrorResume(searchFallback);
	}
	
	
	public Mono<ServerResponse> addVote(ServerRequest request) {
		
		
		final Mono<ReviewVote> toto = request
				.bodyToMono(ReviewVote.class);	
		
		Mono<Boolean> success = this.vote(toto, request.pathVariable("reviewId"));

		return success
				.flatMap(finishVote)
				.onErrorResume(searchFallback);
	}
	
	
	public Mono<ServerResponse> getReviewById(ServerRequest request) {
		
		String reviewId = request.pathVariable("reviewId");
		
		
		Mono<Review> review = transformByReviewId(reviewId);
		
		
		return review
				.flatMap(reviewSuccess)
				.onErrorResume(searchFallback);
	}
	
	
	public Mono<ServerResponse> getBookRating(ServerRequest request) {
		
		
		Mono<Double> rating = this.transformBookRating(request.pathVariable("bookId"));
		
		return rating
				.flatMap(bookRatingSuccess)
				.onErrorResume(searchFallback);	
	}
	
	
	// all utility methods
	private Mono<Boolean> vote(Mono<ReviewVote> reviewVote, String reviewId) {
		
		return reviewVote.flatMap((s) -> {
			try {
				return reviewService.voteHelpful(reviewId, s.getUserId(), s.isHelpful());
			} catch (Exception e) {
				e.printStackTrace();
				return Mono.error(new RuntimeException("SATOR"));
			}
		});		
				
	}
	
	
	private Function<Throwable, Mono<ServerResponse>> searchFallback = 
			error -> {
				
					return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.build();
	};
	
	private Function<Flux<Review>, Mono<ServerResponse>> searchSuccess = 
			reviews -> {
				
					return ServerResponse.ok()
							.contentType(MediaType.APPLICATION_JSON)
							.body(reviews, Review.class);
	};
				
	
	private Function<String, Mono<Flux<Review>>> transformByUserId = 
			userId -> {
				
					
				return Mono.just(reviewService.getReviewsByUserId(userId));
	};
	
	private Mono<Review> transformByReviewId(String reviewId) {
						
		return reviewService.getReviewById(reviewId);	
	}
			
	private Mono<Flux<Review>> transformByBookId(String bookId, String sort) {
		try {
			return Mono.just(reviewService.getReviewByBookId(bookId, sort));
			
			//.getReviewsByBookId(bookId, sort));
		} catch (Exception e) {
			// return custom exception wrapped in a Mono
			return Mono.error(new RuntimeException("SATOR"));
		}
	} 
	
	private Mono<Double> transformBookRating(String bookId) {
		try {
			return reviewService.getBookRating(bookId);
		} catch (Exception e) {
			// return custom exception wrapped in a Mono
			return Mono.error(new RuntimeException("SATOR"));
		}
	} 
	
	private Function<Review, Mono<URI>> transformCreate =
			review -> {
				
				try {
					Mono<String> newReviewId = reviewService.createReview(review)
							.map(r -> r.getId());
					return newReviewId
							.flatMap(s -> {
								try {
									String enclume = baseReviewsURL + "/reviewById/" + s;
									
									return Mono.just(new URI(baseReviewsURL + "/reviewById/" + s));
								} catch (URISyntaxException e) {
									e.printStackTrace();
									return Mono.error(new RuntimeException("SATOR"));
								}
							});
				} catch (Exception e) {
					// return custom exception wrapped in a Mono
					return Mono.error(new RuntimeException("SATOR"));
				}
	};
	
	private Function<URI, Mono<ServerResponse>> finishCreate =
			location -> {
				
				return ServerResponse.created(location).build();		
	};
	
	private Function<Boolean, Mono<ServerResponse>> finishVote =
			success -> {
				if (success) {
					return ServerResponse.ok().build();	
				} else {
					return ServerResponse.status(HttpStatus.CONFLICT).build();
				}		
	};
	
	private Function<Review, Mono<ServerResponse>> reviewSuccess =
			review -> {
				
				return ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(Mono.just(review), Review.class);	  			
	};
	
	private Function<Double, Mono<ServerResponse>> bookRatingSuccess =
			rating -> {
				
				
				return ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(Mono.justOrEmpty(rating), Double.class);	  
	};
	

	/** 
	 * Here I need to take into account the special case where no book rating was found.
	 * In this case the server should return a null.
	 * */
}
 
