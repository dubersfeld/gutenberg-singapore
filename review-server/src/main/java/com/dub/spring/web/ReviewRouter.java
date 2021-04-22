package com.dub.spring.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class ReviewRouter {
	
	@Bean
	public RouterFunction<ServerResponse> route(ReviewHandler reviewHandler) {

	    return RouterFunctions
	      .route(RequestPredicates.POST("/reviewsByUserId")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), reviewHandler::getReviewsByUserId)  
	      .andRoute(RequestPredicates.GET("/reviewsByBookId/{bookId}/sort/{sort}")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), reviewHandler::getReviewsByBookId)  
	      .andRoute(RequestPredicates.POST("/createReview")
	    	      .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), reviewHandler::createReview)  
	      .andRoute(RequestPredicates.POST("/addVote/{reviewId}")
	    	      .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), reviewHandler::addVote) 
	      .andRoute(RequestPredicates.GET("/reviewById/{reviewId}")
	    	      .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), reviewHandler::getReviewById)  
	      .andRoute(RequestPredicates.GET("/bookRating/{bookId}")
	    	      .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), reviewHandler::getBookRating);     
	}
}
