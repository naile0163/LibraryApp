package com.library.utilities;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LibraryUtils {

    public static String generateTokenByRole(String role) {

        Map<String, String> roleCredentials = returnCredentials(role);
        String email = roleCredentials.get("email");
        String password = roleCredentials.get("password");

        return getToken(email, password);

    }

    public static String getToken(String email, String password) {

        JsonPath jp = RestAssured.given().accept(ContentType.JSON).contentType(ContentType.URLENC)// content type that I am sending to the api
                .formParam("email", email).formParam("password", password).when().post("/login").then().statusCode(200).contentType(ContentType.JSON).extract().jsonPath();

        String accessToken = jp.getString("token");

        return accessToken;

    }


    public static Map<String, String> returnCredentials(String role) {

        //this method returns a map for credentials which has email="email",password="password" based on role
        String email = "";
        String password = "";

        switch (role) {
            case "librarian":
                //email = ConfigurationReader.getProperty("librarian_username");
                //password = ConfigurationReader.getProperty("librarian_password");
                email = System.getenv("librarian_username");
                password = System.getenv("librarian_password");
                break;

            case "student":
                //email = ConfigurationReader.getProperty("student_username");
                //password = ConfigurationReader.getProperty("student_password");
                email = System.getenv("student_username");
                password = System.getenv("student_password");
                break;

            default:

                throw new RuntimeException("Invalid Role Entry :\n>> " + role + " <<");
        }
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", email);
        credentials.put("password", password);

        return credentials;

    }

    public static Map<String, Object> createBook() {

        Faker faker = new Faker();
        Map<String, Object> bookMap = new LinkedHashMap<>();

        String name = faker.book().title()+" N.A";
        String isbn = faker.code().isbn10();
        int year = faker.number().numberBetween(1800,2024);
        String author= faker.book().author();
        int book_category_id= faker.number().numberBetween(1,20);//there is 20 category id when you get from postman
        String description=faker.book().genre();//added genre to describe what type of book it is

        bookMap.put("name", name);
        bookMap.put("isbn","N.A"+ isbn);
        bookMap.put("year", year);
        bookMap.put("author", author);
        bookMap.put("book_category_id", book_category_id);
        bookMap.put("description",description);
        return bookMap;
    }

    public static Map<String, Object> createUser() {

        Faker faker = new Faker();
        Map<String, Object> userMap = new LinkedHashMap<>();
        String name=faker.name().firstName();
        String lastName = faker.name().lastName();
        String fullName= name+" "+lastName;
        String email = name.toLowerCase()+"."+lastName.toLowerCase()+"@"+faker.internet().domainName();
        String passWord = faker.internet().password();
        int userGroupId=faker.number().numberBetween(2,3);//since I give number between 2,20 I got 500 error at the first time,then changed
        String status = faker.options().option("Active", "Inactive", "Pending", "Suspended");
        String startDate = "2024-12-12";
        String endDate = "2025-12-12";
        String address = faker.address().fullAddress();

        userMap.put("full_name",fullName);
        userMap.put("email",email);
        userMap.put("password",passWord);
        userMap.put("user_group_id",userGroupId);
        userMap.put("status",status);
        userMap.put("start_date",startDate);
        userMap.put("end_date",endDate);
        userMap.put("address",address);
        return userMap;
    }

}
