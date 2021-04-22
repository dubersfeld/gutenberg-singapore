package com.dub.spring.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class CategoryRouter {

	@Bean
	public RouterFunction<ServerResponse> allCategoriesRoute(CategoryHandler categoryHandler) {

	    return RouterFunctions
	      .route(RequestPredicates.GET("/allCategories")
	      .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), categoryHandler::allCategories);  
	}
	
	@Bean
	public RouterFunction<ServerResponse> categoryBySlugRoute(CategoryHandler categoryHandler) {

	    return RouterFunctions
	      .route(RequestPredicates.GET("/category/{slug}")
	      .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), categoryHandler::categoryBySlug);  
	}
	
}
