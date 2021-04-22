package com.dub.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.dub.spring.domain.Address;
import com.dub.spring.domain.MyUser;
import com.dub.spring.domain.PaymentMethod;
import com.dub.spring.domain.UserAuthority;
import com.dub.spring.exceptions.UserNotFoundException;
import com.dub.spring.repository.UserRepository;
import com.dub.spring.services.UserService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


@ActiveProfiles("test")
@SpringBootTest(properties = {"eureka.client.enabled=false"})
public class UserServiceTest {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserRepository userRepository;
	
	private Address newAddress = new Address();
	
	private PaymentMethod newPayment = new PaymentMethod();
	
	private Address delAddress = new Address();
	private PaymentMethod delPayment = new PaymentMethod();
	
	private Predicate<MyUser> createUserPred = 
			user -> "Boris".equals(user.getUsername());

	private Predicate<MyUser> primaryAddressPred = 
					user -> (user.getMainShippingAddress() == 1);

	private Predicate<MyUser> primaryPaymentPred = 
					user -> (user.getMainPayMeth() == 1);

	private Predicate<MyUser> addAddressPred = 
							user -> (addressMatch(user.getAddresses(), newAddress));//user.getMainPayMeth() == 1);
				
	private Predicate<MyUser> addPaymentPred = 
					user -> {
							return paymentMatch(user.getPaymentMethods(), newPayment);};

	private Predicate<MyUser> deleteAddressPred = 
					user -> {
						return !addressMatch(user.getAddresses(), delAddress);};
				
	private Predicate<MyUser> deletePaymentPred = 
							user -> (!paymentMatch(user.getPaymentMethods(), delPayment));//user.getMainPayMeth() == 1);
						
	@BeforeEach
	public void setUp() {
		newPayment.setCardNumber("1111222233334444");
		newPayment.setName("Paul Enclume");
		
		newAddress.setCity("Paris");
		newAddress.setCountry("France");
		newAddress.setStreet("5 Avenue Victoria");
		newAddress.setZip("75001");
	}
							
	@Test
	void createUserTest() {
		List<Address> addresses = Arrays.asList(newAddress);
		List<PaymentMethod> payMeths = Arrays.asList(newPayment);
		
		MyUser user = new MyUser();
		user.setUsername("Boris");
		user.setHashedPassword("{bcrypt}$2a$10$Ip8KBSorI9R39m.KQBk3nu/WhjekgPSmfmpnmnf5yCL3aL9y.ITVW");
		user.setAddresses(addresses);
		user.setPaymentMethods(payMeths);
				
		// actual creation
		Mono<MyUser> checkUser = this.userService.createUser(user);
		StepVerifier.create(checkUser.log())
		.expectNextMatches(createUserPred)
		.verifyComplete();	
	}

	
	@Test
	void testById() {
		String userId = "5a28f2b0acc04f7f2e9740ae";
		MyUser user = this.userService.findById(userId).block();
		assertEquals("Carol", user.getUsername());
		
	}
	
	
	@Test
	void testByIdNotFound() {
		String userId = "5a28f2b0acc04f7f2e976666";
		Mono<MyUser> user = this.userService.findById(userId);
		StepVerifier.create(user.log())
		.expectError(UserNotFoundException.class)
		.verify();	
		
	}
	
	
	@Test
	void testByUsername() {
		String username = "Carol";
		MyUser user = this.userService.findByUsername(username).block();
	
		assertEquals("5a28f2b0acc04f7f2e9740ae", user.getId());
	}
	
	
	@Test
	void testByUsernameNotFound() {
		Mono<MyUser> user = this.userService.findByUsername("Calvin");
		StepVerifier.create(user.log())
		.expectComplete()
		.verify();	
	}
	
	
	@Test
	void setPrimaryAddressTest() {
		String userId = "5a28f2b9acc04f7f2e9740b1";
		int index = 1;
		Mono<MyUser> user = this.userService.setPrimaryAddress(userId, index);
		StepVerifier.create(user.log())
		.expectNextMatches(primaryAddressPred)
		.expectComplete()
		.verify();	
	}
	
	
	@Test
	void setPrimaryAddressTestNotFound() {
		String userId = "5a28f2b9acc04f7f2e976666";
		int index = 1;
		Mono<MyUser> user = this.userService.setPrimaryAddress(userId, index);
		StepVerifier.create(user.log())
		.expectError(UserNotFoundException.class)
		.verify();	
	}
	
	
	@Test
	void setPrimaryPaymentTest() {
		String userId = "5a28f2b9acc04f7f2e9740b1";
		int index = 1;
		Mono<MyUser> user = this.userService.setPrimaryPayment(userId, index);
		StepVerifier.create(user.log())
		.expectNextMatches(primaryPaymentPred)
		.expectComplete()
		.verify();	
	}
	
	
	@Test
	void addAddressTest() {
		String userId = "5a28f2b0acc04f7f2e9740ae";
		// actual update
		Mono<MyUser> user = this.userService.addAddress(userId, newAddress);
		StepVerifier.create(user.log())
		.expectNextMatches(addAddressPred)
		.expectComplete()
		.verify();			
	}
	

	@Test
	void addPaymentTest() {
		String userId = "5a28f2b2acc04f7f2e9740af";
		Mono<MyUser> user = this.userService.addPaymentMethod(userId, newPayment);
		StepVerifier.create(user.log())
		.expectNextMatches(addPaymentPred)
		.expectComplete()
		.verify();			
			
	}
	
	
	@Test
	void deleteAddressTest() {
		String userId = "5a28f2b2acc04f7f2e9740b0";
		//String userId = "5a28f2b9acc04f7f2e9740b1";
		//String userId = "5a28f2b0acc04f7f2e9740ae";
		//MyUser user = this.userService.findById(userId).block();
		//assertEquals("Carol", user.getUsername());
	
		// actual update
		Mono<MyUser> user = userService.deleteAddress(userId, delAddress);
		StepVerifier.create(user.log())
		.expectNextMatches(deleteAddressPred)
		.expectComplete()
		.verify();		
	
	}
	
	
	@Test
	void deleteAddressNotFoundTest() {
		String userId = "5a28f2b2acc04f7f2e976666";
		// actual update
		Mono<MyUser> user = userService.deleteAddress(userId, delAddress);
		StepVerifier.create(user.log())
		.expectComplete()
		.verify();		
	}
	
	
	@Test
	void testDeletePayment() {
		String userId = "5a28f2b2acc04f7f2e9740b0";
		// actual update
		Mono<MyUser> user = this.userService.deletePaymentMethod(userId, delPayment);
		StepVerifier.create(user.log())
		.expectNextMatches(deletePaymentPred)
		.expectComplete()
		.verify();		
	}
	
	
	public static boolean paymentMatch(List<PaymentMethod> payMeths, PaymentMethod payMeth) {
		boolean match = false;
		for (PaymentMethod pM : payMeths) {		
			if (payMeth.equals(pM)) {
				match = true;
				break;
			}
			
		}
		return match;
	}
	
	public static boolean addressMatch(List<Address> addresses, Address address) {
		boolean match = false;
		for (Address add : addresses) {
			if (address.equals(add)) {
				match = true;
				break;
			}
			
		}
		return match;
	}
}
