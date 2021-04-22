package com.dub.spring;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.Arrays;
import java.util.function.Predicate;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.dub.spring.domain.Book;
import com.dub.spring.domain.BookDocument;
import com.dub.spring.domain.BookSearch;
import com.dub.spring.domain.CategoryDocument;
import com.dub.spring.repository.BookRepository;
import com.dub.spring.repository.CategoryRepository;
import com.dub.spring.web.BookHandler;
import com.dub.spring.web.BookRouter;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"eureka.client.enabled=false"})
public class BookHandlerTest {
	
	@Autowired
	BookRouter bookRouter;
	
	@Autowired
	BookHandler bookHandler;
	
	@Autowired
	BookRepository bookRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	
	private Predicate<Book> testBoughtWith = 
			book -> {
				System.err.println(book.getSlug());
				boolean match = "raiders-pattern-3190".equals(book.getSlug());
					
				return match; 
	};	
	
	@BeforeEach
	public void setupDB() {
		
		CategoryDocument cat5 = new CategoryDocument();
		cat5.setId("59fd6b39acc04f10a07d1344");
		cat5.setSlug("fiction");
		cat5.setName("Fiction");
		cat5.setDescription("Most popular novels.");
		cat5.setParentId(new ObjectId("59fd6b39acc04f10a07d1340"));
		
		BookDocument comput1 = new BookDocument();
		comput1.setSlug("malware-begin-666");
		comput1.setId("5a28f2b0acc04f7f2e9740a0");
		comput1.setTitle("Malware for Beginners");
		comput1.setAuthors(Arrays.asList("Marc Dutroux", "Georges Besse"));
		comput1.setPublisher("O'Rourke");
		comput1.setDescription("How to crash your enterprise servers.");
		comput1.setPrice(3539);
		comput1.setCategoryId(new ObjectId("59fd6b39acc04f10a07d1343"));
		comput1.setTags(Arrays.asList("malware","system","blackhat"));
		
		BookDocument book1 = new BookDocument();
		book1.setSlug("emerald-ultimate-421");
		book1.setId("5a28f2b0acc04f7f2e9740a1");
		book1.setTitle("The Ultimate Emerald Reference");
		book1.setAuthors(Arrays.asList("Nivü Nikkonü"));
		book1.setPublisher("O'Rourke");
		book1.setDescription("Much easier to master and more efficient than Ruby.");
		book1.setPrice(3539);
		book1.setCategoryId(new ObjectId("59fd6b39acc04f10a07d1343"));
		book1.setTags(Arrays.asList("software","web","database"));
		
		BookDocument bio1 = new BookDocument();
		bio1.setSlug("jvonneumann-1945");
		bio1.setId("5a28f2b0acc04f7f2e9740a4");
		bio1.setTitle("The Incredible Life of John von Neumann");
		bio1.setAuthors(Arrays.asList("Albert Schweizer"));
		bio1.setPublisher("Grouble");
		bio1.setDescription("A founding father of computer science.");
		bio1.setPrice(4439);
		bio1.setCategoryId(new ObjectId("59fd6b39acc04f10a07d1342"));
		bio1.setTags(Arrays.asList("computer","system","mathematics"));
		
		BookDocument bio2 = new BookDocument();
		bio2.setSlug("heisenberg-1923");
		bio2.setId("5a28f2b0acc04f7f2e9740a5");
		bio2.setTitle("Heisenberg, a Life in Uncertainty");
		bio2.setAuthors(Arrays.asList("Isabel Spengler"));
		bio2.setPublisher("Grouble");
		bio2.setDescription("A founding father of quantum physics. His entire life he had to cope with uncertainty and most probably was not awarded the Nobel prize.");
		bio2.setPrice(4539);
		bio2.setCategoryId(new ObjectId("59fd6b39acc04f10a07d1342"));
		bio2.setTags(Arrays.asList("biography","science","history"));
		
		BookDocument bio3 = new BookDocument();
		bio3.setSlug("jsmouche-1900");
		bio3.setId("5a28f2b0acc04f7f2e9740a6");
		bio3.setTitle("Jean-Sebastien Mouche, from Paris with Love");
		bio3.setAuthors(Arrays.asList("André Malraux"));
		bio3.setPublisher("Grouble");
		bio3.setDescription("He created the popular Bateaux-Mouche where visitors from around the world can enjoy a romantic dinner on the river Seine.");
		bio3.setPrice(4539);
		bio3.setCategoryId(new ObjectId("59fd6b39acc04f10a07d1342"));
		bio3.setTags(Arrays.asList("biography","science","history"));
			
		BookDocument bio4 = new BookDocument();
		bio4.setSlug("marbront-1902");
		bio4.setId("5a28f2b0acc04f7f2e9740a7");
		bio4.setTitle("Eleanor Brontë and the Blank Page Challenge");
		bio4.setAuthors(Arrays.asList("Hu Xiao-Mei"));
		bio4.setPublisher("Spivakov");
		bio4.setDescription("The only Brontë sister who never wrote anything.");
		bio4.setPrice(2739);
		bio4.setCategoryId(new ObjectId("59fd6b39acc04f10a07d1342"));
		bio4.setTags(Arrays.asList("biography","literature","women"));
		
		BookDocument bio5 = new BookDocument();
		bio5.setSlug("nostradamus-42");
		bio5.setId("5a28f2b0acc04f7f2e9740a8");
		bio5.setTitle("Nostradamus");
		bio5.setAuthors(Arrays.asList("Helmut von Staubsauger"));
		bio5.setPublisher("Springfield");
		bio5.setDescription("Everybody has heard of him, now it's time to read about his true story.");
		bio5.setPrice(2739);
		bio5.setCategoryId(new ObjectId("59fd6b39acc04f10a07d1342"));
		bio5.setTags(Arrays.asList("biography","literature","medicine"));
		
		BookDocument fiction1 = new BookDocument();
		fiction1.setSlug("bourne-shell-1542");
		fiction1.setId("5a28f2b0acc04f7f2e9740a9");
		fiction1.setTitle("The Bourne Shell Legacy");
		fiction1.setAuthors(Arrays.asList("Robert Bedlam"));
		fiction1.setPublisher("MacNamara");
		fiction1.setDescription("A nail-biting thriller featuring JSON Bourne.");
		fiction1.setPrice(4539);
		fiction1.setCategoryId(new ObjectId("59fd6b39acc04f10a07d1344"));
		fiction1.setTags(Arrays.asList("thriller","crime","spying"));
		
		BookDocument fiction2 = new BookDocument();
		fiction2.setSlug("raiders-pattern-3190");
		fiction2.setId("5a28f2b0acc04f7f2e9740aa");
		fiction2.setTitle("Raiders of the Lost Pattern");
		fiction2.setAuthors(Arrays.asList("Evert Edepamuur"));
		fiction2.setPublisher("Atkinson-Wembley");
		fiction2.setDescription("Two geeks on the track of an elusive pattern that escaped the attention of the Gang of Four.");
		fiction2.setPrice(3539);
		fiction2.setCategoryId(new ObjectId("59fd6b39acc04f10a07d1344"));
		fiction2.setTags(Arrays.asList("thriller","crime","software"));
		
		BookDocument fiction3 = new BookDocument();
		fiction3.setSlug("dining-philosophers-1542");
		fiction3.setId("5a28f2b0acc04f7f2e9740ab");
		fiction3.setTitle("The Dining Philosophers");
		fiction3.setAuthors(Arrays.asList("Paul Enclume"));
		fiction3.setPublisher("Dyson");
		fiction3.setDescription("Five philosophers decide to have a dinner together. They have to cope with a lack of forks and knives.");
		fiction3.setPrice(3839);
		fiction3.setCategoryId(new ObjectId("59fd6b39acc04f10a07d1344"));
		fiction3.setTags(Arrays.asList("home","life","food"));
		
		BookDocument fiction4  = new BookDocument();
		fiction4.setSlug("walking-planck-3141");
		fiction4.setId("5a28f2b0acc04f7f2e9740ac");
		fiction4.setTitle("Walking the Planck Constant");
		fiction4.setAuthors(Arrays.asList("Laetitia Haddad"));
		fiction4.setPublisher("Hanning");
		fiction4.setDescription("A Caribbean pirate captain falls into a quantum entanglement. Only the Schroedinger Cat can rescue him. Is he dead or alive?");
		fiction4.setPrice(5339);
		fiction4.setCategoryId(new ObjectId("59fd6b39acc04f10a07d1344"));
		fiction4.setTags(Arrays.asList("piracy","science-fiction","gold"));
		
		BookDocument fiction5  = new BookDocument();
		fiction5.setSlug("apes-wrath-4153");
		fiction5.setId("5a28f2b0acc04f7f2e9740ad");
		fiction5.setTitle("Apes of Wrath");
		fiction5.setAuthors(Arrays.asList("Boris Cyrulnik"));
		fiction5.setPublisher("Butterworth");
		fiction5.setDescription("A gorilla keeper in San Diego Zoo struggles to keep his job during the Great Depression.");
		fiction5.setPrice(6839);
		fiction5.setCategoryId(new ObjectId("59fd6b39acc04f10a07d1344"));
		fiction5.setTags(Arrays.asList("apes","life","depression"));
		
		categoryRepository.deleteAll().subscribe();
		categoryRepository.save(cat5).subscribe();
		
		bookRepository.deleteAll().subscribe();
		
		bookRepository.save(book1).subscribe();
		
		bookRepository.save(comput1).subscribe();
		
		bookRepository.save(bio1).subscribe();	
		bookRepository.save(bio2).subscribe();
		bookRepository.save(bio3).subscribe();
		bookRepository.save(bio4).subscribe();
		bookRepository.save(bio5).subscribe();
		
		bookRepository.save(fiction1).subscribe();
		bookRepository.save(fiction2).subscribe();
		bookRepository.save(fiction3).subscribe();
		bookRepository.save(fiction4).subscribe();
		bookRepository.save(fiction5).subscribe();
		
	}
	
	@Test
	void testBookBySlug() {
		WebTestClient
		.bindToRouterFunction(bookRouter.route(bookHandler))		
		.build()
		.method(HttpMethod.GET)
		.uri("/books/emerald-ultimate-421")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isOk()
		.expectBody()
		.jsonPath("$.id").isEqualTo("5a28f2b0acc04f7f2e9740a1")
		.jsonPath("$.title").isEqualTo("The Ultimate Emerald Reference");
	}
	
	
	@Test
	void testAllBooksByCategory() {
		WebTestClient
		.bindToRouterFunction(bookRouter.route(bookHandler))
		.build()
		.method(HttpMethod.GET)
		.uri("/books/fiction/sort/ASC")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Book.class)
		.getResponseBody()// it is a Flux<Book>
		.as(StepVerifier::create)
		.expectNextCount(5)
		.expectComplete()
		.verify();	
	}	
	
	
	@Test
	void testBookByid() {
		WebTestClient
		.bindToRouterFunction(bookRouter.route(bookHandler))		
		.build()
		.method(HttpMethod.GET)
		.uri("/bookById/5a28f2b0acc04f7f2e9740a1")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isOk()
		.expectBody()
		.jsonPath("$.slug").isEqualTo("emerald-ultimate-421")
		.jsonPath("$.title").isEqualTo("The Ultimate Emerald Reference");
	}
	
	
	/*
	@Test
	void testBooksBoughtWith() {
		WebTestClient
		.bindToRouterFunction(bookRouter.route(bookHandler))
		.build()
		.method(HttpMethod.GET)
		.uri("/booksBoughtWith/5a28f2b0acc04f7f2e9740a1/outLimit/10")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Book.class)
		.getResponseBody()// it is a Flux<Book>
		.as(StepVerifier::create)
		.expectNextMatches(testBoughtWith)
		.expectNextCount(2)
		.expectComplete()
		.verify();
			
	}	
	*/

	
	@Test
	void searchByTitle() {
		BookSearch bookSearch = new BookSearch();
		bookSearch.setSearchString("Wrath Life");
		WebTestClient
		.bindToRouterFunction(bookRouter.route(bookHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/searchByTitle")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(bookSearch), BookSearch.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Book.class)
		.getResponseBody()// it is a Flux<Book>
		.as(StepVerifier::create)
		.expectNextCount(3)
		.expectComplete()
		.verify();	
	}	
	
	
	@Test
	void searchByDescription() {
		BookSearch bookSearch = new BookSearch();
		bookSearch.setSearchString("gorilla quantum pattern captain");
		WebTestClient
		.bindToRouterFunction(bookRouter.route(bookHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/searchByDescription")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(bookSearch), BookSearch.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Book.class)
		.getResponseBody()// it is a Flux<Book>
		.as(StepVerifier::create)
		.expectNextCount(5)
		.expectComplete()
		.verify();	
	}	
	
	
	@Test
	void searchByTags() {
		BookSearch bookSearch = new BookSearch();
		bookSearch.setSearchString("biography system");
		WebTestClient
		.bindToRouterFunction(bookRouter.route(bookHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/searchByTags")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(bookSearch), BookSearch.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Book.class)
		.getResponseBody()// it is a Flux<Book>
		.as(StepVerifier::create)
		.expectNextCount(6)
		.expectComplete()
		.verify();	
	}	

}
