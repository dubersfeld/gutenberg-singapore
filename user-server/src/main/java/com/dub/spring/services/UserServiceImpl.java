package com.dub.spring.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.dub.spring.domain.Address;
import com.dub.spring.domain.MyUser;
import com.dub.spring.domain.PaymentMethod;
import com.dub.spring.exceptions.DuplicateUserException;
import com.dub.spring.exceptions.UserNotFoundException;
import com.dub.spring.repository.UserRepository;

import reactor.core.publisher.Mono;

@Component
public class UserServiceImpl implements UserService {
	
	@Autowired private 
	UserRepository userRepository;
	
	@Autowired 
	private ReactiveMongoOperations reactiveMongoOperations;

	@Override
	public Mono<MyUser> findByUsername(String username) {
	
	
		Mono<MyUser> user = userRepository.findByUsername(username);
		
	
		
		return user;
	}

	@Override
	public Mono<MyUser> findById(String userId) {
	
		Mono<MyUser> user = userRepository.findById(userId);
		
		return user.hasElement().flatMap(present -> {
			if (present) {
				
				return user;
			} else {
				
				return Mono.error(new UserNotFoundException());
			}
		}); // may not be empty
		
	}

	@Override
	public Mono<MyUser> setPrimaryAddress(String userId, int index) {
		
	
		Mono<MyUser> user = userRepository.findById(userId);
			
		return user.hasElement().flatMap(present -> {
			if (present) {
				 return user.flatMap(u -> {
					u.setMainShippingAddress(index);
					return this.userRepository.save(u);
				 });
			} else {
				return Mono.error(new UserNotFoundException());
			}
		});
	}

	@Override
	public Mono<MyUser> setPrimaryPayment(String userId, int index) {
		
		Mono<MyUser> user = userRepository.findById(userId);
			
		return user.hasElement().flatMap(present -> {
			if (present) {
				 return user.flatMap(u -> {
					u.setMainPayMeth(index);
					return this.userRepository.save(u);
				 });
			} else {
				return Mono.error(new UserNotFoundException());
			}
		});
	}

	@Override
	public Mono<MyUser> addAddress(String userId, Address newAddress) {
		Mono<MyUser> user = userRepository.findById(userId);
	
		

		return user.flatMap(u -> {
			u.getAddresses().add(newAddress);
			return this.userRepository.save(u);
		});
	}

	@Override
	public Mono<MyUser> addPaymentMethod(String userId, PaymentMethod newPayment) {
		Mono<MyUser> user = userRepository.findById(userId);
				
		
		return user.flatMap(u -> {
			u.getPaymentMethods().add(newPayment);
			Mono<MyUser> toto = this.userRepository.save(u);
			return this.userRepository.save(u);		 
		});
	}

	@Override
	public Mono<MyUser> deleteAddress(String userId, Address delAddress) {
		
		Query query = new Query();
		Update update = new Update();
		query.addCriteria(Criteria.where("_id").is(userId));
		update.pull("addresses", delAddress);
		
		Mono<MyUser> user = reactiveMongoOperations.findAndModify(
											query, 
											update, 
											new FindAndModifyOptions()
													.returnNew(true),
											MyUser.class);
			
		
		return user;
		
	}

	@Override
	public Mono<MyUser> deletePaymentMethod(String userId, PaymentMethod payMeth) {
		Query query = new Query();
		Update update = new Update();
		query.addCriteria(Criteria.where("_id").is(userId));
		update.pull("paymentMethods", payMeth);
		Mono<MyUser> user = reactiveMongoOperations.findAndModify(
											query, 
											update, 
											new FindAndModifyOptions()
													.returnNew(true),
											MyUser.class);
		return user;
	}

	@Override
	public Mono<MyUser> createUser(MyUser user) {
		// check if username already present
	
		Mono<MyUser> check = userRepository.findByUsername(user.getUsername());
		

		
		return check.hasElement().flatMap(present -> {
			if (!present) {
				// OK
				
				return this.userRepository.save(user);
			} else {
				// not allowed
				
				
				return Mono.error(new DuplicateUserException());
			}
		});
	}
	
}
