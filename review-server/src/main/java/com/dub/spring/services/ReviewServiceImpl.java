package com.dub.spring.services;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.dub.spring.domain.BookRating;
import com.dub.spring.domain.Review;
import com.dub.spring.domain.ReviewDocument;
import com.dub.spring.exceptions.ReviewNotFoundException;
import com.dub.spring.repository.ReviewRepository;
import com.dub.spring.utils.ReviewUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ReviewServiceImpl implements ReviewService {

	@Autowired
	private ReviewRepository reviewRepository;
	
	@Autowired
	ReactiveMongoOperations reactiveMongoOperations;
	
	@Override
	public Mono<Review> createReview(Review review) {
		Mono<ReviewDocument> newReview  = reviewRepository.save(ReviewUtils.reviewToDocument(review));
		return newReview.map(ReviewUtils::documentToReview);
	}

	@Override
	public Mono<Review> getReviewById(String reviewId) {
		Mono<ReviewDocument> doc = reviewRepository.findById(reviewId);
		Mono<Boolean> hasElement = doc.hasElement();
		return hasElement.flatMap(b -> {
			if (b) {
				return doc.map(ReviewUtils::documentToReview);
			} else {
				return Mono.error(new ReviewNotFoundException());
			}
		});
		
	}

	@Override
	public Flux<Review> getReviewsByUserId(String userId) {
		
		Flux<Review> reviews = reviewRepository.findByUserId(new ObjectId(userId))
				.map(ReviewUtils::documentToReview);
		
		Mono<Boolean> hasElements = reviews.hasElements();
		
		hasElements.subscribe(System.err::println);
		return reviews;// may be empty
	}

	@Override
	public Flux<Review> getReviewByBookId(String bookId, String sortBy) {
	
		Flux<Review> reviews = reviewRepository.findByBookId(
													new ObjectId(bookId), 
													Sort.by(Sort.Direction.DESC, sortBy))
				.map(ReviewUtils::documentToReview);
			
		return reviews;
	}

	@Override
	public Mono<Double> getBookRating(String bookId) {
	
		GroupOperation group = group("bookId")
				.avg("rating").as("bookRating");

		MatchOperation match = match(new Criteria("bookId").is(new ObjectId(bookId)));

		// static method, not constructor
		Aggregation aggregation = newAggregation(match, group);
		Flux<BookRating> result = reactiveMongoOperations.aggregate(aggregation, "reviews", BookRating.class);

		Mono<Double> rat = result.next().map(r -> r.getBookRating());
		
	
		return rat;
	}

	@Override
	public Mono<Boolean> voteHelpful(String reviewId, String userId, boolean helpful) {
		// return true if vote was allowed, false in case of conflict 
		/** Here findAndModify is not correct 
		 * because the modification is allowed only 
		 * if the user has not voted yet
		 */
		Mono<ReviewDocument> review = this.reviewRepository.findById(reviewId);
		
		Mono<Boolean> hasElement = review.hasElement();
		
		Mono<Boolean> success = hasElement.flatMap(b -> {
			if (!b) {
				return Mono.error(new ReviewNotFoundException());
			} else {
				Mono<Boolean> hasVoted = this.hasVoted(review, userId);
				Mono<Boolean> ok = hasVoted.flatMap(hh -> {
					if (hh) {
						// userId has already voted, do nothing
						
					
						return Mono.just(false);
					} else {
						// actual vote
					
						Query query = new Query();
						Update update = new Update();
						query.addCriteria(Criteria.where("id").is(new ObjectId(reviewId)));
						update.inc("helpfulVotes", helpful ? 1 : 0);
						update.addToSet("voterIds", new ObjectId(userId));
									
						Mono<ReviewDocument> doc = reactiveMongoOperations.findAndModify(query, update, 
										new FindAndModifyOptions().returnNew(false), 
										ReviewDocument.class);
						
						doc.subscribe();// subscribe needed to force query execution
						
						
						return Mono.just(true);
					}
				});
				return ok;
			}
		});
		
		return success;
		
	}
	
	
	private Mono<Boolean>hasVoted(Mono<ReviewDocument> review, final String userId) {
		
		Mono<Boolean> hasVoted = review.map(rev -> {
			boolean match = false;
			for (ObjectId objectId : rev.getVoterIds()) {
				if (userId.equals(objectId.toString())) {
					match = true;
					break;
				}
			}
			return match;
		});
		
		return hasVoted;
	}
	

}
