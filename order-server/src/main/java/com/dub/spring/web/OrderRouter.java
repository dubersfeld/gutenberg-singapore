package com.dub.spring.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class OrderRouter {
	
	@Bean
	public RouterFunction<ServerResponse> route(OrderHandler orderHandler) {

		// only one GET request all others are POST
		return RouterFunctions
		      .route(RequestPredicates.GET("/orderById/{orderId}")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::getOrderById)    
		      .andRoute(RequestPredicates.POST("/createOrder")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::createOrder)    
		      .andRoute(RequestPredicates.POST("/getActiveOrder")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::getActiveOrder)    
		      .andRoute(RequestPredicates.POST("/getActiveOrder")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::getActiveOrder)   
		      .andRoute(RequestPredicates.POST("/setOrderState")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::setOrderState)    
		      .andRoute(RequestPredicates.POST("/checkoutOrder")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::checkoutOrder)   
		      .andRoute(RequestPredicates.POST("/updateOrder")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::updateOrder)    
		      .andRoute(RequestPredicates.POST("/addBookToOrder")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::addBookToOrder) 
		      .andRoute(RequestPredicates.POST("/getBooksNotReviewed")
		    		  .and(RequestPredicates.accept(MediaType.TEXT_EVENT_STREAM)), orderHandler::getBooksNotReviewed)    
		      .andRoute(RequestPredicates.POST("/editCart")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::editCart);    

	}	
	
	
	/*
	@Bean
	public RouterFunction<ServerResponse> createOrder(OrderHandler orderHandler) {

	 return RouterFunctions
		      .route(RequestPredicates.POST("/createOrder")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::createOrder);    
	}
	*/
	
	/*
	@Bean
	public RouterFunction<ServerResponse> editCart(OrderHandler orderHandler) {

	 return RouterFunctions
		      .route(RequestPredicates.POST("/editCart")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::editCart);    
	}
	*/
	
	/*
	@Bean
	public RouterFunction<ServerResponse> getActiveOrderRoute(OrderHandler orderHandler) {

	 return RouterFunctions
		      .route(RequestPredicates.POST("/getActiveOrder")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::getActiveOrder);    
	}
	*/	
	
	/*
	@Bean
	public RouterFunction<ServerResponse> setOrderStateRoute(OrderHandler orderHandler) {

	 return RouterFunctions
		      .route(RequestPredicates.POST("/setOrderState")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::setOrderState);    
	}
	*/	
	
	/*
	@Bean
	public RouterFunction<ServerResponse> checkoutOrderRoute(OrderHandler orderHandler) {

	 return RouterFunctions
		      .route(RequestPredicates.POST("/checkoutOrder")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::checkoutOrder);    
	}
	*/	
	
	/*
	@Bean
	public RouterFunction<ServerResponse> updateOrderRoute(OrderHandler orderHandler) {

	 return RouterFunctions
		      .route(RequestPredicates.POST("/updateOrder")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::updateOrder);    
	}	
	*/
	/*
	@Bean
	public RouterFunction<ServerResponse> routeAddToCart(OrderHandler orderHandler) {

	 return RouterFunctions
		      .route(RequestPredicates.POST("/addBookToOrder")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::addBookToOrder);    
	}
	*/	
	/*
	@Bean
	public RouterFunction<ServerResponse> route(OrderHandler orderHandler) {

	 return RouterFunctions
		      .route(RequestPredicates.POST("/getBooksNotReviewed")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::getBooksNotReviewed);    
	}
	*/		    

}
