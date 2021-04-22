package com.dub.client.services;


import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import com.dub.client.domain.Address;
import com.dub.client.domain.AddressOperation;
import com.dub.client.domain.MyUser;
import com.dub.client.domain.PaymentMethod;
import com.dub.client.domain.PaymentOperation;
import com.dub.client.domain.Primary;
import com.dub.client.domain.ProfileOperations;
import com.dub.client.domain.UserPrincipal;
import com.dub.client.exceptions.DuplicateUserException;
import com.dub.client.exceptions.UnknownServerException;
import com.dub.client.exceptions.UserNotFoundException;

import reactor.core.publisher.Mono;



@Service
public class UserServiceImpl implements UserService {

	private static final String USER_BY_NAME = "/userByName/"; 
	private static final String USER_BY_ID = "/findById/"; 
	private static final String DELETE_ADDRESS = "/deleteAddress"; 
	private static final String ADD_ADDRESS = "/addAddress"; 
	private static final String DELETE_PAYMENT_METHOD = "/deletePaymentMethod"; 
	private static final String ADD_PAYMENT_METHOD = "/addPaymentMethod"; 
	private static final String PRIMARY_PAYMENT = "/primaryPayment";
	private static final String PRIMARY_ADDRESS = "/primaryAddress";
	private static final String CREATE_USER = "/createUser";
	
	
	@Autowired
	private WebClient userClient;
	
		
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		MyUser user = this.findByUsername(username);	
        UserPrincipal principal = new UserPrincipal(user);
        
        return principal;   
	}
	
	
	@Override
	public MyUser findByUsername(String username) {
	
		
		WebClient.ResponseSpec enclume = userClient
				.method(HttpMethod.GET)
				.uri(USER_BY_NAME + username)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.retrieve();
		
		MyUser user = enclume
				.toEntity(MyUser.class) 
				.flatMap(catchErrorsAndTransform2)
				.block();
				
		return user;
	}
	
	
	@Override
	public MyUser findById(String userId) {
		
		WebClient.ResponseSpec enclume = userClient
				.method(HttpMethod.POST)
				.uri(USER_BY_ID)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.body(Mono.just(userId), String.class)
				.retrieve();
						
		MyUser user = enclume
					.toEntity(MyUser.class) 
					.flatMap(catchErrorsAndTransform2)
					.block();		
		
		return user;
	}


	@Override
	public MyUser setPrimaryPaymentMethod(String username, int index) {
	 
		Primary primary = new Primary();
		
		primary.setIndex(index);
		primary.setUsername(username);
		
		ResponseSpec response = userClient
				.method(HttpMethod.POST)
				.uri(PRIMARY_PAYMENT)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.body(Mono.just(primary), Primary.class)
				.retrieve();
		
		MyUser user = response.bodyToMono(MyUser.class).block();
		return user;
	}

	@Override
	public MyUser setPrimaryAddress(String username, int index) {
		 
		Primary primary = new Primary();
		
		primary.setIndex(index);
		primary.setUsername(username);
		
		ResponseSpec response = userClient
				.method(HttpMethod.POST)
				.uri(PRIMARY_ADDRESS)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.body(Mono.just(primary), Primary.class)
				.retrieve();
				
		MyUser user = response.bodyToMono(MyUser.class).block();
		return user;		
	}

		
	@Override
	public void deleteAddress(String username, Address address) {
		 
		AddressOperation deleteOp = new AddressOperation();
		
		deleteOp.setUserId(this.findByUsername(username).getId());
		deleteOp.setAddress(address);
		deleteOp.setOp(ProfileOperations.DELETE);
						
		userClient
			.method(HttpMethod.POST)
			.uri(DELETE_ADDRESS)
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.body(Mono.just(deleteOp), AddressOperation.class)
			.retrieve()
			.toEntity(MyUser.class)
			.flatMap(catchErrorsAndTransform2)
			.block();
		
	}
		
		
	@Override
	public void addAddress(String username, Address newAddress) {
		 
		AddressOperation addOp = new AddressOperation();
		
		addOp.setUserId(this.findByUsername(username).getId());
		addOp.setAddress(newAddress);
		addOp.setOp(ProfileOperations.ADD);
		
		userClient
			.method(HttpMethod.POST)
			.uri(ADD_ADDRESS)
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.body(Mono.just(addOp), AddressOperation.class)
			.retrieve()
			.toEntity(MyUser.class)
			.flatMap(catchErrorsAndTransform2)
			.block();	
	}

	@Override
	public void deletePaymentMethod(String username, PaymentMethod payMeth) {
	 
		PaymentOperation deleteOp = new PaymentOperation();
		
		deleteOp.setUserId(this.findByUsername(username).getId());
		deleteOp.setPaymentMethod(payMeth);
		deleteOp.setOp(ProfileOperations.DELETE);
		
		userClient
			.method(HttpMethod.POST)
			.uri(DELETE_PAYMENT_METHOD)
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.body(Mono.just(deleteOp), PaymentOperation.class)
			.retrieve()
			.toEntity(MyUser.class)
			.flatMap(catchErrorsAndTransform2)
			.block();
	}
	
	
	@Override
	public void addPaymentMethod(String username, PaymentMethod newPayMeth) {
		 
		PaymentOperation addOp = new PaymentOperation();
		
		addOp.setUserId(this.findByUsername(username).getId());
		addOp.setPaymentMethod(newPayMeth);
		addOp.setOp(ProfileOperations.ADD);
			
		userClient
			.method(HttpMethod.POST)
			.uri(ADD_PAYMENT_METHOD)
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.body(Mono.just(addOp), PaymentOperation.class)
			.retrieve()
			.toEntity(MyUser.class)
			.flatMap(catchErrorsAndTransform2)
			.block();
	}
	

	@Override
	public MyUser saveUser(MyUser user) {
					
		WebClient.ResponseSpec enclume = userClient
		.method(HttpMethod.POST)
		.uri(CREATE_USER)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(user), MyUser.class)
		.retrieve();
			
		String uri = enclume
			.toBodilessEntity()
			.flatMap(catchErrorsAndTransformCreate2)
			.block();
		
		return user;	
	}
	
	
	// helper function used as transformer
	Function<ResponseEntity<MyUser>, Mono<MyUser>> catchErrorsAndTransform2 = 
				(ResponseEntity<MyUser> clientResponse) -> {
					if (clientResponse.getStatusCode().is5xxServerError()) {
						throw new UnknownServerException();
					} else if (clientResponse.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
						throw new UserNotFoundException();
					} else {
						return Mono.just(clientResponse.getBody());
					}
		};
	
	// helper function used as transformer
	Function<ClientResponse, Mono<MyUser>> catchErrorsAndTransform = 
			(ClientResponse clientResponse) -> {
				if (clientResponse.statusCode().is5xxServerError()) {
					throw new UnknownServerException();
				} else if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
					throw new UserNotFoundException();
				} else {
					return clientResponse.bodyToMono(MyUser.class);
				}
	};
	
	// helper function returns Mono<String> if OK
	Function<ResponseEntity<Void>, Mono<String>> catchErrorsAndTransformCreate2 = 
							(ResponseEntity<Void> clientResponse) -> {
								if (clientResponse.getStatusCode().is5xxServerError()) {
									throw new UnknownServerException();
								} else {
									return Mono.just(clientResponse.getHeaders().get("location").get(0));
								}
		};
	
}
