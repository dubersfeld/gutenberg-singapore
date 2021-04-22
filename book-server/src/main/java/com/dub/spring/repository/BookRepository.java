package com.dub.spring.repository;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.dub.spring.domain.Book;
import com.dub.spring.domain.BookDocument;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookRepository extends ReactiveMongoRepository<BookDocument, String> {

	public Mono<BookDocument> findOneBySlug(String slug);

	Flux<BookDocument> findByCategoryId(Mono<ObjectId> categoryId, Sort sort);
	
}
