package com.dub.spring.services;

import java.util.List;

import com.dub.spring.domain.Category;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryService {

	public Flux<Category> findAllCategories();
	
	public Mono<Category> getCategory(String categorySlug);
	
}
