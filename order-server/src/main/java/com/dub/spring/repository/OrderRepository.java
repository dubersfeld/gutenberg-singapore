package com.dub.spring.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.dub.spring.domain.OrderDocument;

public interface OrderRepository extends ReactiveMongoRepository<OrderDocument, String> {
	
}
