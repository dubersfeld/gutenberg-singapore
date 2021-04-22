package com.dub.spring.repository;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.dub.spring.domain.ReviewDocument;

import reactor.core.publisher.Flux;

public interface ReviewRepository extends ReactiveMongoRepository<ReviewDocument, String> {
	
	Flux<ReviewDocument> findByBookId(ObjectId bookId);
	
	Flux<ReviewDocument> findByBookId(ObjectId bookId, Sort sort);
	
	Flux<ReviewDocument> findByUserId(ObjectId userId);

}
