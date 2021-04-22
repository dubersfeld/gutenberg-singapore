package com.dub.spring.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.dub.spring.domain.MyUser;

import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<MyUser, String> {
	
	Mono<MyUser> findByUsername(String username);
}
