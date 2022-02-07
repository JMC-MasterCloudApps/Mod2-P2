package es.urjc.code.daw.library.rest;

import static com.github.javafaker.Faker.instance;
import static es.urjc.code.daw.library.rest.TestData.ADMIN_TEST;
import static es.urjc.code.daw.library.rest.TestData.ALL_PASS;
import static es.urjc.code.daw.library.rest.TestData.BOOKS_PATH;
import static es.urjc.code.daw.library.rest.TestData.BOOK_DESCRIPTION;
import static es.urjc.code.daw.library.rest.TestData.BOOK_ID;
import static es.urjc.code.daw.library.rest.TestData.BOOK_TITLE;
import static es.urjc.code.daw.library.rest.TestData.ROLE_ADMIN;
import static es.urjc.code.daw.library.rest.TestData.ROLE_USER;
import static es.urjc.code.daw.library.rest.TestData.USER_TEST;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("REST Assured tests")
class BookRestControllerRestAssuredTest {

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
		if (ofNullable(userRepository.findByName(USER_TEST.val)).isEmpty()) {
			userRepository.save(new User(USER_TEST.val, ALL_PASS.val, ROLE_USER.val));
		}

		if (ofNullable(userRepository.findByName(ADMIN_TEST.val)).isEmpty()) {
			userRepository.save(new User(ADMIN_TEST.val, ALL_PASS.val, ROLE_USER.val, ROLE_ADMIN.val));
		}
	}
	
	@Test
	@DisplayName("GET /api/books/")
	void getAllBooks() {

		given().
	 	when()
			.get(BOOKS_PATH.val).
	 	then()
			.statusCode(OK.value())
		 	.contentType(JSON)
		 	.body(BOOK_TITLE.val, hasItems(DatabaseInitializer.titles));
	}

	@Test
	@DisplayName("POST /api/books/")
	void addNewBook() {

		// GIVEN
		var newBook = new Book(instance().book().title(), instance().funnyName().name());

		// WHEN
		Response response = requestNewBookCreation(newBook);

		// THEN book has been created
		int id = from(response.getBody().asString()).get(BOOK_ID.val);
		assertBookResponse(response, newBook);
		assertBookExists(newBook, id);
	}

	@Test
	@DisplayName("DELETE /api/books/{id}")
	void deleteExistingBook() {

      // GIVEN
      int id = getIdFromNewBookRequest();

      // WHEN
      var response = requestBookDeletionById(id);

      // THEN
      response.then().statusCode(OK.value());
      requestBookDeletionById(id).then().statusCode(NOT_FOUND.value());
	}

	private Response requestNewBookCreation(Book newBook) {

		return given()
				.auth().basic(USER_TEST.val, ALL_PASS.val)
				.body(newBook)
				.contentType(JSON).
				when()
				.post(BOOKS_PATH.val).andReturn();
	}

	private void assertBookResponse(Response response, Book newBook) {

		response.then()
			.statusCode(CREATED.value())
			.contentType(JSON)
			.body(BOOK_ID.val, notNullValue())
			.body(BOOK_TITLE.val, equalTo(newBook.getTitle()))
			.body(BOOK_DESCRIPTION.val, equalTo(newBook.getDescription()));
	}

	private void assertBookExists(Book newBook, int id) {

		given()
			.pathParam(BOOK_ID.val, id).
		when()
			.get(BOOKS_PATH.val + "{id}").
		then()
			.statusCode(OK.value())
			.contentType(JSON)
			.body(BOOK_ID.val, notNullValue())
			.body(BOOK_TITLE.val, equalTo(newBook.getTitle()))
			.body(BOOK_DESCRIPTION.val, equalTo(newBook.getDescription()));
	}

    private int getIdFromNewBookRequest() {

      var newBook = new Book(instance().book().title(), instance().funnyName().name());

      Response response = requestNewBookCreation(newBook);
      return from(response.getBody().asString()).get(BOOK_ID.val);
    }

    private Response requestBookDeletionById(int id) {
      return given()
              .auth().basic(ADMIN_TEST.val, ALL_PASS.val)
              .pathParam(BOOK_ID.val, id).
            when()
              .delete(BOOKS_PATH.val + "{id}").andReturn();
    }

}
