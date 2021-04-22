package com.dub.spring.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;


@Configuration
public class BookRouter {

	@Bean
	public RouterFunction<ServerResponse> route(BookHandler bookHandler) {

	    return RouterFunctions
	      .route(RequestPredicates.GET("/books/{bookSlug}")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), bookHandler::getBookBySlug)
	      .andRoute(RequestPredicates.GET("/books/{categorySlug}/sort/{sortBy}")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), bookHandler::allBooksByCategory)  
	      .andRoute(RequestPredicates.GET("/bookById/{id}")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), bookHandler::bookById)
	      .andRoute(RequestPredicates.GET("/booksBoughtWith/{bookId}/outLimit/{outLimit}")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), bookHandler::booksBoughtWith)  
	      .andRoute(RequestPredicates.POST("/searchByTitle")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)).and(RequestPredicates.contentType(MediaType.APPLICATION_JSON)), bookHandler::searchByTitle)   
	      .andRoute(RequestPredicates.POST("/searchByDescription")
	    	      .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)).and(RequestPredicates.contentType(MediaType.APPLICATION_JSON)), bookHandler::searchByDescription)  
	      .andRoute(RequestPredicates.POST("/searchByTags")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)).and(RequestPredicates.contentType(MediaType.APPLICATION_JSON)), bookHandler::searchByTags);  
	  	
	}
	
}
	