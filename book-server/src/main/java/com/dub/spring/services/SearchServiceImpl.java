package com.dub.spring.services;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.dub.spring.domain.Book;
import com.dub.spring.domain.BookDocument;
import com.dub.spring.domain.TagsAndBookId;
import com.dub.spring.repository.BookRepository;
import com.dub.spring.utils.BookUtils;

import reactor.core.publisher.Flux;

@Service
public class SearchServiceImpl implements SearchService {

	@Autowired
	private BookRepository bookRepository;
	
	@Autowired
	ReactiveMongoOperations reactiveMongoOperations;
		
	@Override
	public Flux<Book> searchByDescription(String searchString) {
		String[] tokens = searchString.trim().split(" ");
		//Flux<String> chose = Flux.fromArray(tokens);
		Flux<Book> books = Flux.fromArray(tokens)
				.flatMap(transformDescription);
		//books.subscribe(b -> System.err.println(b.getTitle()));
		return books;
	}
	
	
	@Override
	public Flux<Book> searchByTags(String searchString) {
	
		String[] tokens = searchString.trim().split(" ");
		//Flux<String> chose = Flux.fromArray(tokens);
		Flux<Book> books = Flux.fromArray(tokens).flatMap(transformTags);
		//books.subscribe(b -> System.out.println(b.getTitle()));
		return books;		
	}

	
	@Override
	public Flux<Book> searchByTitle(String searchString) {
	
		String[] tokens = searchString.trim().split(" ");
		//Flux<String> chose = Flux.fromArray(tokens);
		Flux<Book> books =  Flux.fromArray(tokens).flatMap(transformTitle);
		return books;
	}
	
	
	
	// utility functions
	
	Function<String, Flux<Book>> transformTitle = 
			tok -> {
				
				MatchOperation match1 = match(Criteria.where("title").regex("^.*" + tok + ".*$"));	
				Aggregation aggregation = Aggregation.newAggregation(match1);
				Flux<Book> bookFl = reactiveMongoOperations.aggregate(aggregation, "books", BookDocument.class)
										.map(BookUtils::documentToBook);	
				bookFl.subscribe(b -> System.out.println(b.getTitle()));
				return bookFl;
				
	};
	
	Function<String, Flux<Book>> transformDescription = 
			tok -> {
				
				
				MatchOperation match1 = match(Criteria.where("description").regex("^.*" + tok + ".*$"));	
				Aggregation aggregation = Aggregation.newAggregation(match1);
				Flux<Book> books = reactiveMongoOperations.aggregate(aggregation, "books", BookDocument.class)
										.map(BookUtils::documentToBook);	
				return books;
				
	};
	
	Function<String, Flux<Book>> transformTags = 
			tok -> {
			
				// tags is a List<String>, not a String, so an unwind is needed
				UnwindOperation unwind = unwind("tags");
				
				ProjectionOperation proj1 = project().andExpression("id").as("id").andExpression("tags").as("tags");
					
				MatchOperation match2 = Aggregation.match(Criteria.where("tags").regex("^.*" + tok + ".*$"));	
					
				Aggregation aggregation = newAggregation(proj1, unwind, match2);
							
				Flux<String> bookIds = reactiveMongoOperations.aggregate(aggregation, "books", TagsAndBookId.class)
						.map(res -> res.getId());	
						
				Flux<Book> books = bookRepository.findAllById(bookIds)
						.map(BookUtils::documentToBook);
					
				return books;
				
	};
	

	
}
