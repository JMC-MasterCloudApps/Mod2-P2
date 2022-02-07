package es.urjc.code.daw.library.rest;

import static com.github.javafaker.Faker.instance;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.path.json.JsonPath.from;
import static java.util.Optional.ofNullable;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import es.urjc.code.daw.library.DatabaseInitializer;
import es.urjc.code.daw.library.book.Book;
import es.urjc.code.daw.library.user.User;
import es.urjc.code.daw.library.user.UserRepository;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookRestControllerTest {

	private static final String USER_TEST = "testuser";
	private static final String ADMIN_TEST = "testadmin";
	private static final String ALL_PASS = "pass";
	private static final String BOOKS_PATH = "/api/books/";

	private static final String BOOK_ID = "id";
	private static final String BOOK_TITLE = "title";

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
		if (ofNullable(userRepository.findByName(USER_TEST)).isEmpty()) {
			userRepository.save(new User(USER_TEST, ALL_PASS, "ROLE_USER"));
		}

		if (ofNullable(userRepository.findByName(ADMIN_TEST)).isEmpty()) {
			userRepository.save(new User(ADMIN_TEST, ALL_PASS, "ROLE_USER", "ROLE_ADMIN"));
		}
	}
	
	@Test
	@DisplayName("REST Assured GET /api/books/")
	void getAllBooks() {
		
		given().
	 	when()
			.get(BOOKS_PATH).
	 	then()
			.statusCode(OK.value())
		 	.contentType(JSON)
		 	.body(BOOK_TITLE, hasItems(DatabaseInitializer.titles));
	}

	@Test
	@DisplayName("REST Assured POST /api/books/")
	void addNewBook() {

		// GIVEN
		var newBook = new Book(instance().book().title(), instance().funnyName().name());
		System.out.println(newBook);

		// WHEN
		Response response = requestNewBookCreation(newBook);

		System.out.println(response.asString());

		// THEN book has been created
		int id = from(response.getBody().asString()).get(BOOK_ID);
		assertBookResponse(response, newBook);
		assertBookExists(newBook, id);
	}

	@Test
	@DisplayName("REST Assured DELETE /api/books/{id}")
	void deleteExistingBook() {

      // GIVEN
      int id = getIdFromNewBookRequest();

      System.out.println("ID: " + id);

      // WHEN
      var response = requestBookDeletionById(id);

      // THEN
      response.then().statusCode(OK.value());
      requestBookDeletionById(id).then().statusCode(NOT_FOUND.value());
	}

	private Response requestNewBookCreation(Book newBook) {

		return given()
				.auth().basic(USER_TEST, ALL_PASS)
				.body(newBook)
				.contentType(JSON).
				when()
				.post(BOOKS_PATH).andReturn();
	}

	private void assertBookResponse(Response response, Book newBook) {

		response.then()
			.statusCode(CREATED.value())
			.contentType(JSON)
			.body(BOOK_ID, notNullValue())
			.body(BOOK_TITLE, equalTo(newBook.getTitle()))
			.body("description", equalTo(newBook.getDescription()));
	}

	private void assertBookExists(Book newBook, int id) {

		given()
			.pathParam(BOOK_ID, id).
		when()
			.get(BOOKS_PATH + "{id}").
		then()
			.statusCode(OK.value())
			.contentType(JSON)
			.body(BOOK_ID, notNullValue())
			.body(BOOK_TITLE, equalTo(newBook.getTitle()))
			.body("description", equalTo(newBook.getDescription()));
	}

    private int getIdFromNewBookRequest() {

      var newBook = new Book(instance().book().title(), instance().funnyName().name());
      System.out.print(newBook);

      Response response = requestNewBookCreation(newBook);
      return from(response.getBody().asString()).get(BOOK_ID);
    }

    private Response requestBookDeletionById(int id) {
      return given()
              .auth().basic(ADMIN_TEST, ALL_PASS)
              .pathParam(BOOK_ID, id).
            when()
              .delete(BOOKS_PATH + "{id}").andReturn();
    }

}
