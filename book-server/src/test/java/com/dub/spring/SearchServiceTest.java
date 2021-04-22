package com.dub.spring;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Arrays;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.dub.spring.domain.Book;
import com.dub.spring.domain.BookDocument;
import com.dub.spring.repository.BookRepository;
import com.dub.spring.services.SearchService;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ActiveProfiles("test")
@SpringBootTest(properties = {"eureka.client.enabled=false"})
public class SearchServiceTest {

	@Autowired
	SearchService searchService;
	
	@Autowired 
	BookRepository bookRepository;
		
	@BeforeEach
   	public void setupDb() {
		
		BookDocument book1 = new BookDocument();
		book1.setId("5a28f2b0acc04f7f2e97409f");
		book1.setSlug("mess-harefaq-1542");
		book1.setCategoryId(new ObjectId("59fd6b39acc04f10a07d1343"));
		book1.setTitle("Messaging with HareFAQ");
		book1.setPublisher("Gutenberg");
		book1.setAuthors(Arrays.asList("Paul Bunyan"));
		book1.setDescription("A new enterprise messaging implementation");
		book1.setPrice(2339);
		book1.setTags(Arrays.asList("java","spring","messaging"));
		
		BookDocument book2 = new BookDocument();
		book2.setId("5a28f2b0acc04f7f2e9740a0");
		book2.setSlug("malware-begin-666");
		book2.setCategoryId(new ObjectId("59fd6b39acc04f10a07d1343"));
		book2.setTitle("Malware for beginners");
		book2.setPublisher("O'Rourke");
		book2.setAuthors(Arrays.asList("Marc Dutroux","George Besse"));
		book2.setDescription("How to crash your enterprise servers");
		book2.setPrice(3339);
		book2.setTags(Arrays.asList("malware","system","blackhat"));
	
		BookDocument book3 = new BookDocument();
		book3.setId("5a28f2b0acc04f7f2e9740a4");
		book3.setSlug("jvonneumann-1945");
		book3.setCategoryId(new ObjectId("59fd6b39acc04f10a07d1342"));
		book3.setTitle("The Incredible Life of John von Neumann");
		book3.setPublisher("Grouble");
		book3.setAuthors(Arrays.asList("Albert Schweizer"));
		book3.setDescription("A founding father of computer science");
		book3.setPrice(4439);
		book3.setTags(Arrays.asList("computer","system","mathematics"));
	
		BookDocument book4 = new BookDocument();
		book4.setId("5a28f2b0acc04f7f2e9740a5");
		book4.setSlug("heisenberg-1923");
		book4.setCategoryId(new ObjectId("59fd6b39acc04f10a07d1342"));
		book4.setTitle("Heisenberg, a Life in Uncertainty");
		book4.setPublisher("Grouble");
		book4.setAuthors(Arrays.asList("Isabel Spengler"));
		book4.setDescription("A founding father of quantum physics. His entire life he had to cope with uncertainty and most probably was not awarded the Nobel prize.");
		book4.setPrice(4539);
		book4.setTags(Arrays.asList("biography","science","history"));
	
		BookDocument book5 = new BookDocument();
		book5.setId("5a28f2b0acc04f7f2e9740ad");
		book5.setSlug("apes-wrath-4153");
		book5.setCategoryId(new ObjectId("59fd6b39acc04f10a07d1344"));
		book5.setTitle("Heisenberg, a Life in Uncertainty");
		book5.setPublisher("Butterworth");
		book5.setAuthors(Arrays.asList("Boris Cyrulnik"));
		book5.setDescription("A gorilla keeper in San Diego Zoo struggles to keep his job during the Great Depression.");
		book5.setPrice(4539);
		book5.setTags(Arrays.asList("apes","life","depression"));
	
		BookDocument book6 = new BookDocument();
		book6.setId("5a28f2b0acc04f7f2e9740aa");
		book6.setSlug("raiders-pattern-3190");
		book6.setCategoryId(new ObjectId("59fd6b39acc04f10a07d1344"));
		book6.setTitle("Raiders of the Lost Pattern");
		book6.setPublisher("Atkinson-Wembley");
		book6.setAuthors(Arrays.asList("Evert Edepamuur"));
		book6.setDescription("Two geeks on the track of an elusive pattern that escaped the attention of the Gang of Four.");
		book6.setPrice(4539);
		book6.setTags(Arrays.asList("thriller","crime","software"));
	
		BookDocument book7 = new BookDocument();
		book7.setId("5a28f2b0acc04f7f2e9740ac");
		book7.setSlug("walking-planck-3141");
		book7.setCategoryId(new ObjectId("59fd6b39acc04f10a07d1344"));
		book7.setTitle("Walking the Planck Constant");
		book7.setPublisher("Hanning");
		book7.setAuthors(Arrays.asList("Laeticia Haddad"));
		book7.setDescription("A Caribbean pirate captain falls into a quantum entanglement. Only the Schroedinger Cat can rescue him. Is he dead or alive?");
		book7.setPrice(5339);
		book7.setTags(Arrays.asList("piracy","science-fiction","gold"));
	
		bookRepository.deleteAll().subscribe();
			
		bookRepository.save(book1).subscribe();
		bookRepository.save(book2).subscribe();
		bookRepository.save(book3).subscribe();
		bookRepository.save(book4).subscribe();
		bookRepository.save(book5).subscribe();
		bookRepository.save(book6).subscribe();
		bookRepository.save(book7).subscribe();
		
    }
	
	
	@Test
	void testSearchByTags() {
		
		Flux<Book> books = searchService.searchByTags("biography system");
		StepVerifier.create(books.log())
		.expectNextCount(3)
		.verifyComplete();			
	}
	

	@Test
	void testSearchByTitle() {
		
		Flux<Book> books = searchService.searchByTitle("Wrath Life");
		StepVerifier.create(books.log())
		.expectNextCount(3)
		.verifyComplete();
		
	}
	

	@Test
	void testSearchByDescription() {
		
		Flux<Book> books = searchService.searchByDescription("gorilla quantum pattern captain");
		StepVerifier.create(books.log())
		.expectNextCount(5)
		.verifyComplete();
		
	}
	
	
}
