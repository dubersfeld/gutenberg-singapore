package com.dub.spring;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.dub.spring.domain.Book;
import com.dub.spring.domain.BookDocument;
import com.dub.spring.domain.CategoryDocument;
import com.dub.spring.exceptions.BookNotFoundException;
import com.dub.spring.repository.BookRepository;
import com.dub.spring.repository.CategoryRepository;
import com.dub.spring.services.BookService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ActiveProfiles("test")
@SpringBootTest(properties = {"eureka.client.enabled=false"})
public class BookServiceTest {

	@Autowired
	BookService bookService;
	
	@Autowired
	BookRepository bookRepository;
	
	
	@Autowired
	CategoryRepository categoryRepository;
						
	private Predicate<Book> testById = 
			book -> {
				boolean match = "emerald-ultimate-421".equals(book.getSlug());
				return match;
	};
			
	private Predicate<Book> testBySlug = 
			book -> {
				System.err.println(book.getId());
				boolean match = "5a28f2b0acc04f7f2e9740a1".equals(book.getId());
						
				return match; 
	};	
			
	private Predicate<Book> testBoughtWith = 
			book -> {
				System.err.println(book.getSlug());
				boolean match = "raiders-pattern-3190".equals(book.getSlug());
					
				return match; 
	};	
	
	
	@Test
	void testBookBySlug() {	     
		Mono<Book> book = bookService.getBookBySlug("emerald-ultimate-421");
		
		StepVerifier.create(book.log())
					.expectNextMatches(testBySlug)
					.verifyComplete();
		
	}
	
	
	@Test
	void testBookBySlugFail() {	     
		Mono<Book> book = bookService.getBookBySlug("emerold-ultimate-421");
		
		StepVerifier.create(book.log())
					.expectError(BookNotFoundException.class)
					.verify();
	}
	
	
	@Test
	void testBookById() {	     
		Mono<Book> book = bookService.getBookById("5a28f2b0acc04f7f2e9740a1");
		
		StepVerifier.create(book.log())
					.expectNextMatches(testById)
					.verifyComplete();
	}
	
	
	@Test
	void testBookByIdFail() {	     
		Mono<Book> book = bookService.getBookById("666");
		
		StepVerifier.create(book.log())
					.expectError(BookNotFoundException.class)
					.verify();
	}
		
		
	@Test
	void testAllBooksByCategory() {
	     Flux<Book> books = bookService.allBooksByCategory("fiction", "ASC");
	         
	     StepVerifier.create(books.log())
	     			.expectNextCount(5)
	     			.verifyComplete();

	}
	
	
	@Test
	void testBooksBoughtWithNull() {
		Flux<Book> books = bookService.getBooksBoughtWith("12", 10);//.getCategory("fiction");
	
		StepVerifier.create(books.log())
					.verifyComplete();
	}	
	
	
	@Test
	void testBooksBoughtWith() {
		
	
		Flux<Book> books = bookService.getBooksBoughtWith("5a28f2b0acc04f7f2e9740a1", 10);
		
		List<Book> list = books.collectList().block();
		

	    StepVerifier.create(books.log())
	     			.expectNextMatches(testBoughtWith)
	     			.expectNextCount(2)
	     			.verifyComplete(); 
	
	}
		
}
