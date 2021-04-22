package com.dub.spring.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.dub.spring.domain.AddressOperations;
import com.dub.spring.domain.MyUser;
import com.dub.spring.domain.PaymentOperations;
import com.dub.spring.domain.Primary;
import com.dub.spring.exceptions.DuplicateUserException;
import com.dub.spring.exceptions.UserNotFoundException;
import com.dub.spring.services.UserService;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

/**
 * Never use try-catch, always use onErrorResume 
 * */

@Component
public class UserHandler {
	
	@Autowired
	UserService userService;
	
	@Value("${baseUsersUrl}")
	private String baseUsersUrl; 
	
	
	public Mono<ServerResponse> createUser(ServerRequest request) {
		
		// location should be http://localhost:8084/userByName/username
		// new candidate user
		Mono<MyUser> user = request.bodyToMono(MyUser.class);	
		Mono<String> newUser = user.flatMap(transformCreateUser);
		Mono<String> locationStr = newUser.flatMap(s -> Mono.just(baseUsersUrl + "/userByName/" + s));
		Mono<URI> location = grunge(locationStr); 
	
		return location
				.flatMap(createUserSuccess)
				.onErrorResume(createUserFallback);
	}
	
	
	public Mono<ServerResponse> primaryAddress(ServerRequest request) {
		
	
		
		Mono<Primary> primary = request.bodyToMono(Primary.class);
		
		// here I use Mono.zip method
		Mono<Tuple2<MyUser, Integer>> tuple = primary.flatMap(transformSetPrimaryAddress);
		
		Mono<MyUser> user = tuple.flatMap(transformSetPrimaryAddress2);
	
		return user
				.flatMap(primaryAddressSuccess)
				.onErrorResume(primaryAddressFallback);	
	}
	
	
	public Mono<ServerResponse> primaryPayment(ServerRequest request) {
		
		Mono<Primary> primary = request.bodyToMono(Primary.class);
		
		// here I use Mono.zip method
		Mono<Tuple2<MyUser, Integer>> tuple = primary.flatMap(transformSetPrimaryPayment);
		
		Mono<MyUser> user = tuple.flatMap(transformSetPrimaryPayment2);
	
		return user
				.flatMap(primaryPaymentSuccess)
				.onErrorResume(primaryPaymentFallback);
	}
			
	
	public Mono<ServerResponse> addAddress(ServerRequest request) {
		
		
		return request
				.bodyToMono(AddressOperations.class)
				.flatMap(transformAddAddress)
				.flatMap(userSuccess)
				.onErrorResume(userFallback);
	}
	
	
	public Mono<ServerResponse> addPaymentMethod(ServerRequest request) {
		
		
		
		return request
				.bodyToMono(PaymentOperations.class)
				.flatMap(transformAddPaymentMethod)	
				.flatMap(userSuccess)
				.onErrorResume(userFallback);
	}
	
	
	public Mono<ServerResponse> deleteAddress(ServerRequest request) {
		
	
		
		return request
				.bodyToMono(AddressOperations.class)
				.flatMap(transformDeleteAddress)	
				.flatMap(userSuccess)
				.onErrorResume(userFallback);
	}
	

	public Mono<ServerResponse> deletePaymentMethod(ServerRequest request) {
		
		
			
		return request
				.bodyToMono(PaymentOperations.class)
				.flatMap(transformDeletePaymentMethod)
				.flatMap(userSuccess)
				.onErrorResume(userFallback);
	}
	
	
	public Mono<ServerResponse> findById(ServerRequest request) {
		
		
		
		return request
				.bodyToMono(String.class)
				.flatMap(transformById)
				.flatMap(userSuccess)
				.onErrorResume(userFallback);
	}
	
	
	public Mono<ServerResponse> getUserByName(ServerRequest request) {
		Mono<String> username = Mono.just(request.pathVariable("username"));
		
		
		return username
				.flatMap(transformByUsername)
				.flatMap(userSuccess)
				.onErrorResume(userFallback);
	}


	Mono<URI> grunge(Mono<String> str) {
		
		try {	
			Mono<URI> uri = str.map(s -> {
				try {
					return new URI(s);
				} catch (URISyntaxException e) {
		
					throw new RuntimeException();
				}
			});
			return uri;
		} catch (Exception e) {
			return Mono.error(new RuntimeException());
		}
		
		

		/*
		try {
				return new URI(s);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		*/
		
	}
	
	// all utility functions
	
	Function<AddressOperations, Mono<MyUser>> transformAddAddress = 		
			s -> {
				try {			
					return userService.addAddress(s.getUserId(), s.getAddress());
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("SATOR");
				}
	};
			
	Function<PaymentOperations, Mono<MyUser>> transformAddPaymentMethod = 
			s -> {
				try {			
					return userService.addPaymentMethod(s.getUserId(), s.getPaymentMethod());
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("SATOR");
				}
	};
	
	Function<AddressOperations, Mono<MyUser>> transformDeleteAddress = 
			s -> {
				try {	
					
					return userService.deleteAddress(s.getUserId(), s.getAddress());
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("SATOR");
				}
	};
	
	Function<PaymentOperations, Mono<MyUser>> transformDeletePaymentMethod = 
			s -> {
				try {			
					return userService.deletePaymentMethod(s.getUserId(), s.getPaymentMethod());
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("SATOR");
				}
	};
	
	Function<String, Mono<MyUser>> transformById = 
			s -> {
				try {			
					return userService.findById(s);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("SATOR");
				}
	};
	
	Function<String, Mono<MyUser>> transformByUsername = 
			s -> {
				try {			
					return userService.findByUsername(s);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("SATOR");
				}
	};
					
	Function<Primary, Mono<Tuple2<MyUser,Integer>>> transformSetPrimaryPayment =
			s -> {
					
					try {
							Mono<MyUser> enclume = userService.findByUsername(s.getUsername());
							Mono<Integer> index = Mono.just(s.getIndex());
							return Mono.zip(enclume, index);
					} catch (Exception e) {
							e.printStackTrace();
							throw new RuntimeException("SATOR");
						}
					};		
	
				
	Function<Tuple2<MyUser, Integer>, Mono<MyUser>> transformSetPrimaryPayment2 =
			s -> {
					String userId = s.getT1().getId();
					int index = s.getT2();
						
					try {
						return userService.setPrimaryPayment(userId, index);
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException("SATOR");
					}		
	};
								
	Function<Primary, Mono<Tuple2<MyUser,Integer>>> transformSetPrimaryAddress =
			s -> {
					
					try {
						Mono<MyUser> enclume = userService.findByUsername(s.getUsername());
						Mono<Integer> index = Mono.just(s.getIndex());
						return Mono.zip(enclume, index);
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException("SATOR");
					}
	};		
					
	Function<Tuple2<MyUser, Integer>, Mono<MyUser>> transformSetPrimaryAddress2 =
			s -> {
					String userId = s.getT1().getId();
					int index = s.getT2();
									
					try {
						return userService.setPrimaryAddress(userId, index);
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException("SATOR");
					}		
	};
											
	Function<MyUser, Mono<String>> transformCreateUser =
			s -> {
					try {
						return userService.createUser(s)
								.flatMap(u -> Mono.just(u.getUsername()));
					} catch (Exception e) {
						
						return Mono.error(new RuntimeException("SATOR"));
					}
	};
			
	Function<Throwable, Mono<ServerResponse>> fallback = 
			e -> {
				System.err.println("Error " + e);
				return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();			
	};
							
	Function<URI, Mono<ServerResponse>> createUserSuccess =
			uri -> {
				return ServerResponse.created(uri).build();
					
	};
			
	Function<Throwable, Mono<ServerResponse>> createUserFallback =
			e -> {
					if (e.getClass().equals(DuplicateUserException.class)) {
						
						return ServerResponse
								.status(HttpStatus.CONFLICT)
								.build();
					} else {
						
						return ServerResponse
								.status(HttpStatus.INTERNAL_SERVER_ERROR)
								.build();			
					}
	};
							
	Function<MyUser, Mono<ServerResponse>> primaryAddressSuccess =
			s -> {
					return ServerResponse.ok()
								.contentType(MediaType.APPLICATION_JSON)
								.body(Mono.just(s), MyUser.class);
	};		
	
	Function<Throwable, Mono<ServerResponse>> primaryAddressFallback =
			e -> {
					if (e.getClass() == UserNotFoundException.class) {
						
						return ServerResponse
								.status(HttpStatus.NOT_FOUND)
								.build();
					} else {
						return ServerResponse
								.status(HttpStatus.INTERNAL_SERVER_ERROR)
								.build();			
					}
	};
	
	Function<MyUser, Mono<ServerResponse>> primaryPaymentSuccess =
			s -> {
					return ServerResponse.ok()
								.contentType(MediaType.APPLICATION_JSON)
								.body(Mono.just(s), MyUser.class);
	};		
	
	Function<Throwable, Mono<ServerResponse>> primaryPaymentFallback =
			e -> {
					if (e.getClass() == UserNotFoundException.class) {
						
						return ServerResponse
								.status(HttpStatus.NOT_FOUND)
								.build();
					} else {
						return ServerResponse
								.status(HttpStatus.INTERNAL_SERVER_ERROR)
								.build();			
					}
	};
	
	Function<MyUser, Mono<ServerResponse>> userSuccess =
			s -> {
					return ServerResponse.ok()
								.contentType(MediaType.APPLICATION_JSON)
								.body(Mono.just(s), MyUser.class);
	};		
	
	Function<Throwable, Mono<ServerResponse>> userFallback =
			e -> {
					if (e.getClass() == UserNotFoundException.class) {
						
						return ServerResponse
								.status(HttpStatus.NOT_FOUND)
								.build();
					} else {
						return ServerResponse
								.status(HttpStatus.INTERNAL_SERVER_ERROR)
								.build();			
					}
	};
				
}
