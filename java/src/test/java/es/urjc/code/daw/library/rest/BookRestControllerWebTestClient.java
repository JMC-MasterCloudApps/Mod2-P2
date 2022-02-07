package es.urjc.code.daw.library.rest;

import static com.github.javafaker.Faker.instance;
import static es.urjc.code.daw.library.DatabaseInitializer.titles;
import static es.urjc.code.daw.library.rest.TestData.ADMIN_TEST;
import static es.urjc.code.daw.library.rest.TestData.ALL_PASS;
import static es.urjc.code.daw.library.rest.TestData.BOOKS_PATH;
import static es.urjc.code.daw.library.rest.TestData.BOOK_DESCRIPTION;
import static es.urjc.code.daw.library.rest.TestData.BOOK_ID;
import static es.urjc.code.daw.library.rest.TestData.BOOK_TITLE;
import static es.urjc.code.daw.library.rest.TestData.USER_TEST;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.path.json.JsonPath.from;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import es.urjc.code.daw.library.book.Book;
import es.urjc.code.daw.library.user.User;
import es.urjc.code.daw.library.user.UserRepository;
import io.restassured.response.Response;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("REST Web Test Client")
class BookRestControllerWebTestClient {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private WebTestClient testClient;

	@BeforeEach
    public void setUp() {
		if (ofNullable(userRepository.findByName(USER_TEST.val)).isEmpty()) {
			userRepository.save(new User(USER_TEST.val, ALL_PASS.val, "ROLE_USER"));
		}

		if (ofNullable(userRepository.findByName(ADMIN_TEST.val)).isEmpty()) {
			userRepository.save(new User(ADMIN_TEST.val, ALL_PASS.val, "ROLE_USER", "ROLE_ADMIN"));
		}
    }
	
	@Test
	@DisplayName("GET /api/books/")
	void getAllBooks() {

		testClient.
		get().uri(BOOKS_PATH.val).
		exchange()
			.expectStatus().isOk()
			.expectBody()
				.jsonPath("$.title", containsString(Arrays.toString(titles)));
	}

	@Test
	@DisplayName("POST /api/books/")
	void addNewBook() {

		// GIVEN
		var newBook = new Book(
				instance().book().title(),
				instance().funnyName().name()
		);

		// WHEN
		var result = requestNewBookCreation(newBook);

		// THEN
		var returnedBook = assertNewBookCreationResponse(result, newBook);
		assertBookExists(returnedBook);
	}

	@Test
	@DisplayName("DELETE /api/books/{id}")
	void deleteExistingBook() {

		// GIVEN
		long id = getIdFromNewBookRequest();

		// WHEN
		var response = requestBookDeletionById(id);

		// THEN
		response.expectStatus().isOk();
		requestBookDeletionById(id).expectStatus().isNotFound();
	}

	private WebTestClient.ResponseSpec requestNewBookCreation(Book newBook) {

		return testClient.
			mutate().filter(basicAuthentication(USER_TEST.val, ALL_PASS.val)).build().
			post()
				.uri(BOOKS_PATH.val)
				.bodyValue(newBook).
			exchange();
	}

	private Book assertNewBookCreationResponse(WebTestClient.ResponseSpec response, Book newBook) {

		var book = response
							.expectStatus().isCreated()
							.returnResult(Book.class).getResponseBody().blockFirst();

		assertThat(book.getId()).isNotNull();
		assertThat(book.getTitle()).isEqualTo(newBook.getTitle());
		assertThat(book.getDescription()).isEqualTo(newBook.getDescription());

		return book;
	}

	private void assertBookExists(Book returnedBook) {

		var id = returnedBook.getId();

		testClient.
				get().uri(BOOKS_PATH.val + id).
				exchange()
					.expectStatus().isOk()
					.expectHeader().contentType(APPLICATION_JSON)
					.expectBody().jsonPath(
							BOOK_ID.val, equalTo(id),
							BOOK_TITLE.val, returnedBook.getTitle(),
							BOOK_DESCRIPTION.val, returnedBook.getDescription()
				);
	}

    private Long getIdFromNewBookRequest() {

      var newBook = new Book(instance().book().title(), instance().funnyName().name());
      var response = requestNewBookCreation(newBook);

      return response.returnResult(Book.class).getResponseBody().blockFirst().getId();
    }

    private WebTestClient.ResponseSpec requestBookDeletionById(long id) {

		return testClient.
				mutate().filter(basicAuthentication(ADMIN_TEST.val, ALL_PASS.val)).build().
				delete().uri(BOOKS_PATH.val + id).
				exchange();
    }

}
