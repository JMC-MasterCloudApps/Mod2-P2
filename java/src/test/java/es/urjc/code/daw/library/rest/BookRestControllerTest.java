package es.urjc.code.daw.library.rest;

import static io.restassured.RestAssured.given;

import es.urjc.code.daw.library.DatabaseInitializer;
import es.urjc.code.daw.library.user.User;

import static org.hamcrest.CoreMatchers.hasItems;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import es.urjc.code.daw.library.user.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookRestControllerTest {

	@LocalServerPort
    int port;

	@Autowired
	private UserRepository userRepository;
	
	@BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.baseURI = "https://localhost:" + port;
        
        createTestUsers();
    }
	
	private void createTestUsers() {
		userRepository.save(new User("testuser", "pass", "ROLE_USER"));
		userRepository.save(new User("testadmin", "pass", "ROLE_USER, ROLE_ADMIN"));
	}
	
	@Test
	void getAllBooks() {
		
		given().
		 when()
		 	.get("/api/books/").
		 then()
		 	.statusCode(200)
		 	.contentType(ContentType.JSON)
		 	.body("title", hasItems(DatabaseInitializer.titles));
	}
}
