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

import com.dub.spring.domain.Category;
import com.dub.spring.domain.CategoryDocument;
import com.dub.spring.repository.CategoryRepository;
import com.dub.spring.services.CategoryService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ActiveProfiles("test")
@SpringBootTest(properties = {"eureka.client.enabled=false"})
public class CategoryServiceTest {

	@Autowired
	CategoryService categoryService;
	
	@Autowired
	CategoryRepository categoryRepository;
	
	
	private Predicate<Category> testBySlug = 
			cat -> {
				System.err.println(cat.getId());
				boolean match = "59fd6b39acc04f10a07d1344".equals(cat.getId());	
					
				return match; };
			
	@Test
	void testFindAll() {
	     Flux<Category> cats = categoryService.findAllCategories();
  
	     StepVerifier.create(cats.log())
	     			.expectNextCount(5)
	     			.verifyComplete();
	    
	}
	
	
	@Test
	void testBySlug() {
		Mono<Category> cat = categoryService.getCategory("fiction");
	
		StepVerifier.create(cat.log())
					.expectNextMatches(testBySlug)
					.verifyComplete();
	}
	
}
