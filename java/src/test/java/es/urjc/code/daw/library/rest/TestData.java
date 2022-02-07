package es.urjc.code.daw.library.rest;

public enum TestData {

  USER_TEST("testuser"),
  ADMIN_TEST("testadmin"),
  ALL_PASS("pass"),
  BOOKS_PATH("/api/books/"),
  BOOK_ID("id"),
  BOOK_TITLE("title"),
  BOOK_DESCRIPTION("description"),
  ROLE_USER,
  ROLE_ADMIN;

  public String val;

  TestData(String val) {
    this.val = val;
  }

  TestData() {
    val = this.name();
  }

}
