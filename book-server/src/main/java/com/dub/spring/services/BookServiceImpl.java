package com.dub.spring.services;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.dub.spring.domain.Book;
import com.dub.spring.domain.BookCount;
import com.dub.spring.domain.CategoryDocument;
import com.dub.spring.domain.UserResult;
import com.dub.spring.exceptions.BookNotFoundException;
import com.dub.spring.repository.BookRepository;
import com.dub.spring.repository.CategoryRepository;
import com.dub.spring.utils.BookUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BookServiceImpl implements BookService {

	@Autowired
	private BookRepository bookRepository;
	
	@Autowired
	ReactiveMongoOperations reactiveMongoOperations;
	
	@Autowired
	private CategoryRepository categoryRepository;
		
	
	@Override
	public Mono<Book> getBookBySlug(String slug) {
		
		Mono<Book> book = bookRepository.findOneBySlug(slug)
							.map(BookUtils::documentToBook);
			
		return book.hasElement().flatMap(
				present -> {
					if (present) {
						return book;
					} else {
						return Mono.error(new BookNotFoundException());			
		}});
	}

	@Override
	public Mono<Book> getBookById(String bookId) {
		
		Mono<Book> book = bookRepository.findById(bookId)
							.map(BookUtils::documentToBook);
			
		return book.hasElement().flatMap(
				present -> {
					if (present) {
						return book;
					} else {
						return Mono.error(new BookNotFoundException());			
		}});
	
	}

	@Override
	public Flux<Book> allBooksByCategory(String categorySlug, String sortBy) {
		Mono<CategoryDocument> cat = categoryRepository.findOneBySlug(categorySlug);
		
		Mono<ObjectId> objId = cat.map(c -> c.getId())
							.map(c -> new ObjectId(c));
	
		
		Flux<Book> books = bookRepository.findByCategoryId(
				objId, Sort.by(Sort.Direction.ASC, sortBy))
					.map(BookUtils::documentToBook);

		return books;
	}

	@Override
	public Flux<Book> getBooksBoughtWith(String bookId, int limit) {
		
		
		MatchOperation match1 = match(Criteria.where("state").is("SHIPPED"));		
		ProjectionOperation proj1 = project("lineItems", "userId");
		UnwindOperation unwind = unwind("lineItems");
		MatchOperation match2 = match(Criteria.where("lineItems.bookId").is(bookId));	
		ProjectionOperation proj2 = project("userId");
				
		Aggregation aggregation = Aggregation.newAggregation(match1, proj1, unwind, match2, proj2);
			
		Flux<UserResult> results = reactiveMongoOperations.aggregate(aggregation, "orders", UserResult.class);
		
		
	
		// result contains all users who bought the book referenced by bookId
		Flux<ObjectId> userIds = results.map(ur -> ur.getUserId());
		
		// here I assume that userIds is correct and I transform it	
		
		// second aggregation: find all books bought by the users returned by the first aggregation
		
		Flux<Flux<BookCount>> bookCounts = forge(userIds, bookId);
		
				
		Flux<String> bookIds = Flux.concat(bookCounts)
				.map(b -> b.getBookId()).distinct();
		
		Flux<Book> books = bookRepository.findAllById(bookIds)
				.map(BookUtils::documentToBook);
			
		return books;
	}
	
	// utility methods
	Flux<Flux<BookCount>> forge(Flux<ObjectId> userIds, final String bookId) {
		
		// each ObjectId is transformed into a Flux<BookCount>
		
		Flux<Flux<BookCount>> temp = userIds.map(
				userId -> {
					
					MatchOperation match1 = match(Criteria.where("state").is("SHIPPED")
							.and("userId").is(userId));	
					GroupOperation group = group("bookId").count().as("count");
					
					ProjectionOperation proj2 = project("count").and("bookId").previousOperation();
					
					ProjectionOperation projAlias = project("userId")							
												.and("lineItems.bookId").as("bookId");
					
					MatchOperation match2 = match(Criteria.where("bookId").ne(bookId));
					UnwindOperation unwind = unwind("lineItems");
					SortOperation sort = sort(Sort.Direction.DESC, "count");
					LimitOperation limitOp = limit(10);
										
					Aggregation aggregation = newAggregation(match1, unwind, projAlias, match2, group, proj2, sort, limitOp);
			
					Flux<BookCount> bookCounts = reactiveMongoOperations.aggregate(
							aggregation, "orders", BookCount.class);

					return bookCounts;
				});
		return temp;
	}

}
