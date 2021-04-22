package com.dub.spring.services;


import com.dub.spring.domain.Book;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookService {
	
	Mono<Book> getBookBySlug(String slug);
	
	Mono<Book> getBookById(String bookId);
		
	Flux<Book> allBooksByCategory(String categorySlug, String sortBy);
	
	// more advanced methods

	Flux<Book> getBooksBoughtWith(String bookId, int limit);

}