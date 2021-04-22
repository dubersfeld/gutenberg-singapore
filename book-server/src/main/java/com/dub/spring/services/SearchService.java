package com.dub.spring.services;

import java.io.IOException;

import com.dub.spring.domain.Book;

import reactor.core.publisher.Flux;

public interface SearchService {

	public Flux<Book> searchByDescription(String searchString);
	public Flux<Book> searchByTags(String searchString);
	public Flux<Book> searchByTitle(String searchString);
	
	//public Flux<Book> searchByTitleAlt(String searchString);

}
