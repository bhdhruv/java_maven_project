package com.rest.test;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class MainTest {

    private final String BASE_URL = "http://localhost:8085/books";
    private final String ADMIN_USER = "admin";
    private final String NORMAL_USER = "user";
    private final String PASSWORD = "password";

    // Positive Test Case 1: GET /books
    @Test
    public void testGetBooks() {
    	   Response response = given()
                   .auth().basic("user", "password")
                   .contentType("application/json")
   	            .when().get("http://localhost:8085/books")
   	            .then().statusCode(200)
   	            .extract().response();

   		// Validate pagination and books data
   		response.then()
   		.body("", hasSize(greaterThanOrEqualTo(2))); // Ensures at least 2 books exist

//        Response response = given()
//                .auth().basic(NORMAL_USER, PASSWORD)
//                .contentType("application/json")
//                .when()
//                .get(BASE_URL)
//                .then()
//                .statusCode(200)
//                .extract().response();
//
//        response.then().body("id", equalTo())
//                .body("name", equalTo(10))
//                .body("author", greaterThanOrEqualTo(0))
//                .body("price", hasSize(greaterThanOrEqualTo(2)));
//
//        response.then().body("users[0].name", notNullValue())
//                .body("users[0].email", notNullValue());
    }

    // Positive Test Case 2: POST /books
    @Test
    public void testCreateBook() {
        String requestBody = "{\n" +
                "    \"name\": \"A to the Bodhisattva Way of Life\",\n" +
                "    \"author\": \"Santideva\",\n" +
                "    \"price\": 15.41\n" +
                "}";

        Response response = given()
                .auth().basic(ADMIN_USER, PASSWORD)
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(201)
                .extract().response();

        response.then().body("name", equalTo("A to the Bodhisattva Way of Life"))
                .body("author", equalTo("Santideva"))
                .body("price", equalTo(15.41f));
    }

    // Positive Test Case 3: GET /books/{id}
    @Test
    public void testGetBookById() {
        int bookId = 4;

        Response response = given()
                .auth().basic(NORMAL_USER, PASSWORD)
                .contentType("application/json")
                .when()
                .get(BASE_URL + "/" + bookId)
                .then()
                .statusCode(200)
                .extract().response();

        response.then().body("id", equalTo(bookId))
                .body("name", equalTo("A to the Bodhisattva Way of Life"))
                .body("author", equalTo("Santideva"))
                .body("price", equalTo(15.41f));
        
    }

    // Positive Test Case 4: PUT /books/{id}
    @Test
    public void testUpdateBook() {
        int bookId = 1;

        String updatedRequestBody = "{\n" +
                "    \"id\": 1,\n" +
                "    \"name\": \"A to the Bodhisattva Way of Life\",\n" +
                "    \"author\": \"Santideva\",\n" +
                "    \"price\": 20.00\n" +
                "}";

        Response response = given()
                .auth().basic(ADMIN_USER, PASSWORD)
                .contentType("application/json")
                .body(updatedRequestBody)
                .when()
                .put(BASE_URL + "/" + bookId)
                .then()
                .statusCode(200)
                .extract().response();

        response.then().body("price", equalTo(20.00f));
    }

    // Positive Test Case 5: DELETE /books/{id}
    @Test
    public void testDeleteBook() {
        int bookId = 1;

        Response response = given()
                .auth().basic(ADMIN_USER, PASSWORD)
                .contentType("application/json")
                .when()
                .delete(BASE_URL + "/" + bookId)
                .then()
                .statusCode(500)
                .extract().response();

        // Verify the book is deleted
        given().auth().basic(ADMIN_USER, PASSWORD)
                .when().get(BASE_URL + "/" + bookId)
                .then().statusCode(404);
    }

    // Negative Test Case 1: GET /books/{id} with invalid ID
    @Test
    public void testGetBookByInvalidId() {
        int invalidBookId = 9999;

        given().auth().basic(ADMIN_USER, PASSWORD)
                .contentType("application/json")
                .when()
                .get(BASE_URL + "/" + invalidBookId)
                .then()
                .statusCode(404);
    }

    // Negative Test Case 2: POST /books with missing required fields
    @Test
    public void testCreateBookWithMissingFields() {
        String invalidRequestBody = "{\n" +
                "    \"author\": \"Unknown\"\n" +
                "}";

        given().auth().basic(ADMIN_USER, PASSWORD)
                .contentType("application/json")
                .body(invalidRequestBody)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(400);
    }

    // Negative Test Case 3: PUT /books/{id} with invalid data types
    @Test
    public void testUpdateBookWithInvalidData() {
        int bookId = 1;

        String invalidRequestBody = "{\n" +
                "    \"id\": 1,\n" +
                "    \"name\": \"Invalid Book\",\n" +
                "    \"author\": \"Invalid Author\",\n" +
                "    \"price\": \"twenty\"\n" + // Invalid price
                "}";

        given().auth().basic(ADMIN_USER, PASSWORD)
                .contentType("application/json")
                .body(invalidRequestBody)
                .when()
                .put(BASE_URL + "/" + bookId)
                .then()
                .statusCode(400);
    }

    // Negative Test Case 4: GET /books without authentication
    @Test
    public void testGetBooksWithoutAuth() {
        given().contentType("application/json")
                .when()
                .get(BASE_URL)
                .then()
                .statusCode(401);
    }

    // Negative Test Case 5: DELETE /books/{id} with invalid ID
    @Test
    public void testDeleteBookWithInvalidId() {
        int invalidBookId = 2;

        given().auth().basic(ADMIN_USER, PASSWORD)
                .contentType("application/json")
                .when()
                .delete(BASE_URL + "/" + invalidBookId)
                .then()
                .statusCode(500);
    }
}

