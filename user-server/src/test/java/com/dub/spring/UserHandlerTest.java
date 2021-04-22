package com.dub.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.dub.spring.domain.Address;
import com.dub.spring.domain.AddressOperations;
import com.dub.spring.domain.MyUser;
import com.dub.spring.domain.PaymentMethod;
import com.dub.spring.domain.PaymentOperations;
import com.dub.spring.domain.Primary;
import com.dub.spring.domain.ProfileOperations;
import com.dub.spring.domain.UserAuthority;
import com.dub.spring.repository.UserRepository;
import com.dub.spring.web.UserHandler;
import com.dub.spring.web.UserRouter;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


@ActiveProfiles("test")
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"eureka.client.enabled=false"})

public class UserHandlerTest {
	
	@Value("${baseUsersUrl}")
	private String baseUsersURL;
	
	/*@Autowired
	private UserRepository userRepository;
*/
	
	@Autowired
	UserRouter userRouter;
	
	@Autowired
	UserHandler userHandler;
	
	private MyUser newUser = new MyUser();
	private MyUser conflictUser = new MyUser();
	
	private Address newAddress = new Address();
	private PaymentMethod newPayment = new PaymentMethod();
	
	private Address delAddress = new Address();
	private PaymentMethod delPayment = new PaymentMethod();
	

	@BeforeEach
	public void setup() {
		newPayment.setCardNumber("1111222233334444");
		newPayment.setName("Paul Enclume");
		
		newAddress.setCity("Paris");
		newAddress.setCountry("France");
		newAddress.setStreet("5 Avenue Victoria");
		newAddress.setZip("75001");
		
		newUser.setUsername("Nelson");
		newUser.setHashedPassword("{bcrypt}$2a$10$Ip8KBSorI9R39m.KQBk3nu/WhjekgPSmfmpnmnf5yCL3aL9y.ITVW");
	
		conflictUser.setUsername("Albert");
		conflictUser.setHashedPassword("{bcrypt}$2a$10$Ip8KBSorI9R39m.KQBk3nu/WhjekgPSmfmpnmnf5yCL3aL9y.ITVW");
		
	}
	
	
	private Predicate<MyUser> setPrimaryAddressPred =
			user -> user.getMainShippingAddress() == 1;
			
	private Predicate<MyUser> setPrimaryPaymentPred =
			user -> user.getMainPayMeth() == 1;

	private Predicate<MyUser> addAddressPred = 				
			user -> matchAddress(user.getAddresses(), newAddress);
			
	private Predicate<MyUser> addPaymentPred = 				
			user -> matchPayment(user.getPaymentMethods(), newPayment);
			
	private Predicate<MyUser> deleteAddressPred = 				
			user -> !matchAddress(user.getAddresses(), delAddress);
			
	private Predicate<MyUser> deletePaymentPred = 			
			user -> !matchPayment(user.getPaymentMethods(), delPayment);
			
	private Predicate<MyUser> findByIdPred = 
			user -> ("Alice".equals(user.getUsername()));				
			
	private Predicate<MyUser> findByUsernamePred = 
			user -> ("5a28f2b9acc04f7f2e9740b1".equals(user.getId()));				
			
					
			
	@Test
	void createUserTest() {
		HttpHeaders headers = WebTestClient
		.bindToRouterFunction(userRouter.route(userHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/createUser")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(newUser), MyUser.class)
		.exchange()
		.expectStatus().isCreated()
		.returnResult(String.class)
		.getResponseHeaders();// HttpHeaders
		
	
		
		String location = headers.get("location").get(0);
		String patternString = "^" + baseUsersURL + "/userByName/.*$";  
	  
		
		
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(location);
		assertEquals(true, matcher.matches());
	}	
	
	
	@Test
	void createUserConflictTest() {
		WebTestClient
		.bindToRouterFunction(userRouter.route(userHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/createUser")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(conflictUser), MyUser.class)
		.exchange()
		.expectStatus().isEqualTo(HttpStatus.CONFLICT);
	}	
	
	
	@Test
	void setPrimaryAddressTest() {
		Primary primary = new Primary();
		primary.setUsername("Alice");
		primary.setIndex(1);
		
		WebTestClient
		.bindToRouterFunction(userRouter.route(userHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/primaryAddress")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(primary), Primary.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(MyUser.class)
		.getResponseBody()// it is a Flux<MyUser>
		.as(StepVerifier::create)
		.expectNextMatches(setPrimaryAddressPred)
		.expectComplete()
		.verify();	
	}	
	
	
	@Test
	void setPrimaryPaymentTest() {
		Primary primary = new Primary();
		primary.setUsername("Alice");
		primary.setIndex(1);
		
		WebTestClient
		.bindToRouterFunction(userRouter.route(userHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/primaryPayment")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(primary), Primary.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(MyUser.class)
		.getResponseBody()// it is a Flux<MyUser>
		.as(StepVerifier::create)
		.expectNextMatches(setPrimaryPaymentPred)
		.expectComplete()
		.verify();	
	}	
	
	
	@Test
	void addAddressTest() {
		AddressOperations addOp = new AddressOperations();
		addOp.setAddress(newAddress);
		addOp.setOp(ProfileOperations.ADD);
		addOp.setUserId("5a28f2b0acc04f7f2e9740ae");
		
		WebTestClient
		.bindToRouterFunction(userRouter.route(userHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/addAddress")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(addOp), AddressOperations.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(MyUser.class)
		.getResponseBody()// it is a Flux<MyUser>
		.as(StepVerifier::create)
		.expectNextMatches(addAddressPred)
		.expectComplete()
		.verify();	
	}	
	
	
	@Test
	void addPaymentMethodTest() {
		PaymentOperations payOp = new PaymentOperations();
		
		payOp.setPaymentMethod(newPayment);
		payOp.setOp(ProfileOperations.ADD);
		payOp.setUserId("5a28f2b9acc04f7f2e9740b1");
		
		WebTestClient
		.bindToRouterFunction(userRouter.route(userHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/addPaymentMethod")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(payOp), PaymentOperations.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(MyUser.class)
		.getResponseBody()// it is a Flux<MyUser>
		.as(StepVerifier::create)
		.expectNextMatches(addPaymentPred)
		.expectComplete()
		.verify();	
	}	
	
	
	@Test
	void deleteAddressTest() {
		AddressOperations addOp = new AddressOperations();
		
		addOp.setAddress(delAddress);
		addOp.setOp(ProfileOperations.DELETE);
		addOp.setUserId("5a28f2b9acc04f7f2e9740b1");
		
		WebTestClient
		.bindToRouterFunction(userRouter.route(userHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/deleteAddress")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(addOp), AddressOperations.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(MyUser.class)
		.getResponseBody()// it is a Flux<MyUser>
		.as(StepVerifier::create)
		.expectNextMatches(deleteAddressPred)
		.expectComplete()
		.verify();	
	}	
	

	@Test
	void deletePaymentTest() {
		PaymentOperations payOp = new PaymentOperations();
		
		payOp.setPaymentMethod(delPayment);
		payOp.setOp(ProfileOperations.DELETE);
		payOp.setUserId("5a28f2b9acc04f7f2e9740b1");
		
		WebTestClient
		.bindToRouterFunction(userRouter.route(userHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/deletePaymentMethod")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(payOp), PaymentOperations.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(MyUser.class)
		.getResponseBody()// it is a Flux<MyUser>
		.as(StepVerifier::create)
		.expectNextMatches(deletePaymentPred)
		.expectComplete()
		.verify();	
	}	
	

	@Test
	void findByIdTest() {
		String userId = "5a28f2b9acc04f7f2e9740b1";
		WebTestClient
		.bindToRouterFunction(userRouter.route(userHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/findById")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(userId), String.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(MyUser.class)
		.getResponseBody()// it is a Flux<MyUser>
		.as(StepVerifier::create)
		.expectNextMatches(findByIdPred)
		.expectComplete()
		.verify();	
	}	
	
	
	@Test
	void findByUsernameTest() {
		String username = "Alice";
		WebTestClient
		.bindToRouterFunction(userRouter.route(userHandler))
		.build()
		.method(HttpMethod.GET)
		.uri("/userByName/" + username)
		.exchange()
		.expectStatus().isOk()
		.returnResult(MyUser.class)
		.getResponseBody()// it is a Flux<MyUser>
		.as(StepVerifier::create)
		.expectNextMatches(findByUsernamePred)
		.expectComplete()
		.verify();	
	}	
	
	
	private boolean matchAddress(List<Address> list, Address check) {
		boolean match = false;
		for (Address address : list) {
			if (address.equals(check)) {
				match = true;
				break;
			}
		}
		return match;
	}
	
	private boolean matchPayment(List<PaymentMethod> list, PaymentMethod check) {
		boolean match = false;
		for (PaymentMethod payMeth : list) {
			if (payMeth.equals(check)) {
				match = true;
				break;
			}
		}
		return match;
	}
	
}
