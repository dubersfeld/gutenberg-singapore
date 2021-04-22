package com.dub.spring.services;

import com.dub.spring.domain.Address;
import com.dub.spring.domain.MyUser;
import com.dub.spring.domain.PaymentMethod;

import reactor.core.publisher.Mono;

public interface UserService {

	 Mono<MyUser> findById(String userId);// not override
		
	 Mono<MyUser> findByUsername(String username);// not override
	 
	 Mono<MyUser> setPrimaryAddress(String userId, int index);
		
	 Mono<MyUser> setPrimaryPayment(String userId, int index);
	 
	 Mono<MyUser> addAddress(String userId, Address newAddress);
	 
	 Mono<MyUser> addPaymentMethod(String userId, PaymentMethod newPayment);
		
	 Mono<MyUser> deleteAddress(String userId, Address delAddress);
	 
	 Mono<MyUser> deletePaymentMethod(String userId, PaymentMethod payMeth);
	 
	 Mono<MyUser> createUser(MyUser user);
	 
}
