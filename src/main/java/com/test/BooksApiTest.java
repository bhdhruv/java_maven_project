package com.test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class BooksApiTest {
    
    private final String BASE_URL = "http://localhost:8085/books";
    private final String USERNAME = "admin";
    private final String PASSWORD = "password";
    private int createdBookId;
    
    @BeforeClass
    public void setup() {
        RestAssured.baseURI = BASE_URL;
    }
    
    // Test 1: GET /books - Retrieve all books
    @Test(priority = 1)
    public void testGetAllBooks() {
        given().auth().preemptive().basic(USERNAME, PASSWORD)
            .when().get()
            .then().statusCode(200)
            .contentType(ContentType.JSON)
            .body("total", greaterThan(0));
    }
    
    // Test 2: POST /books - Create a new book
    @Test(priority = 2)
    public void testCreateBook() {
        String requestBody = """
            {
                "name": "A Guide to the Bodhisattva Way of Life",
                "author": "Santideva",
                "price": 15.41
            }
        """;
        
        Response response = given().auth().preemptive().basic(USERNAME, PASSWORD)
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when().post()
            .then().statusCode(201)
            .extract().response();
        
        createdBookId = response.jsonPath().getInt("id");
        Assert.assertNotEquals(createdBookId, 0, "Book ID should not be zero");
    }
    
    // Test 3: GET /books/{id} - Retrieve a book by ID
    @Test(priority = 3, dependsOnMethods = "testCreateBook")
    public void testGetBookById() {
        given().auth().preemptive().basic(USERNAME, PASSWORD)
            .when().get("/" + createdBookId)
            .then().statusCode(200)
            .body("id", equalTo(createdBookId))
            .body("name", equalTo("A Guide to the Bodhisattva Way of Life"));
    }
    
    // Test 4: PUT /books/{id} - Update book details
    @Test(priority = 4, dependsOnMethods = "testCreateBook")
    public void testUpdateBook() {
        String updatedRequestBody = """
            {
                "id": " + createdBookId + ",
                "name": "Updated Book Title",
                "author": "Updated Author",
                "price": 20.99
            }
        """;
        
        given().auth().preemptive().basic(USERNAME, PASSWORD)
            .contentType(ContentType.JSON)
            .body(updatedRequestBody)
            .when().put("/" + createdBookId)
            .then().statusCode(200)
            .body("name", equalTo("Updated Book Title"))
            .body("author", equalTo("Updated Author"));
    }
    
    // Test 5: DELETE /books/{id} - Delete book
    @Test(priority = 5, dependsOnMethods = "testCreateBook")
    public void testDeleteBook() {
        given().auth().preemptive().basic(USERNAME, PASSWORD)
            .when().delete("/" + createdBookId)
            .then().statusCode(200);
    }
    
    // Test 6: Unauthorized access to GET /books
    @Test(priority = 6)
    public void testUnauthorizedGetBooks() {
        given().when().get()
            .then().statusCode(401);
    }
    
    // Test 7: GET /books/{id} with invalid ID
    @Test(priority = 7)
    public void testGetBookByInvalidId() {
        given().auth().preemptive().basic(USERNAME, PASSWORD)
            .when().get("/9999")
            .then().statusCode(404);
    }
    
    // Test 8: POST /books with missing fields
    @Test(priority = 8)
    public void testCreateBookWithMissingFields() {
        String invalidRequestBody = """
            {
                "name": "Incomplete Book"
            }
        """;
        
        given().auth().preemptive().basic(USERNAME, PASSWORD)
            .contentType(ContentType.JSON)
            .body(invalidRequestBody)
            .when().post()
            .then().statusCode(400);
    }
    
    // Test 9: PUT /books/{id} with invalid ID
    @Test(priority = 9)
    public void testUpdateBookWithInvalidId() {
        String updateBody = """
            {
                "id": 9999,
                "name": "Non-existent Book",
                "author": "Unknown",
                "price": 10.99
            }
        """;
        
        given().auth().preemptive().basic(USERNAME, PASSWORD)
            .contentType(ContentType.JSON)
            .body(updateBody)
            .when().put("/9999")
            .then().statusCode(404);
    }
    
    // Test 10: DELETE /books/{id} with invalid ID
    @Test(priority = 10)
    public void testDeleteBookWithInvalidId() {
        given().auth().preemptive().basic(USERNAME, PASSWORD)
            .when().delete("/9999")
            .then().statusCode(404);
    }
}
