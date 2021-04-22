package com.dub.spring.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.dub.spring.domain.CategoryDocument;

import reactor.core.publisher.Mono;

public interface CategoryRepository extends ReactiveMongoRepository<CategoryDocument, String> {

	Mono<CategoryDocument> findOneBySlug(String slug);
}
