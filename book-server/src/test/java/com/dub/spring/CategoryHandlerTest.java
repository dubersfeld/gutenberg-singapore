package com.dub.spring;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Arrays;
import java.util.List;

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

import com.dub.spring.domain.Category;
import com.dub.spring.domain.CategoryDocument;
import com.dub.spring.repository.CategoryRepository;
import com.dub.spring.web.CategoryHandler;
import com.dub.spring.web.CategoryRouter;

import reactor.test.StepVerifier;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"eureka.client.enabled=false"})
public class CategoryHandlerTest {
	
	@Autowired
	CategoryRouter categoryRouter;
	
	@Autowired
	CategoryHandler categoryHandler;
	
	//@Autowired
	//CategoryRepository categoryRepository;
	
	
	@Test
	void testAllCategories() {

		WebTestClient
		.bindToRouterFunction(categoryRouter.allCategoriesRoute(categoryHandler))
		.build()
		.method(HttpMethod.GET)
		.uri("/allCategories")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Category.class)
		.getResponseBody()// it is a Flux<Category>
		.as(StepVerifier::create)
		.expectNextCount(5)
		.expectComplete()
		.verify();	
	}
	
	@Test
	void testCategoryBySlug() {

		WebTestClient
		.bindToRouterFunction(categoryRouter.categoryBySlugRoute(categoryHandler))
		.build()
		.method(HttpMethod.GET)
		.uri("/category/fiction")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isOk()
		.expectBody()
		.jsonPath("$.id").isEqualTo("59fd6b39acc04f10a07d1344")
		.jsonPath("$.name").isEqualTo("Fiction");
		
		
	}
		
}
