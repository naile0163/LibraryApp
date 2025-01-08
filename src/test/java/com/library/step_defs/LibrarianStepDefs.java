package com.library.step_defs;

import com.library.pages.BooksPage;
import com.library.pages.LoginPage;
import com.library.utilities.BrowserUtils;
import com.library.utilities.DB_Util;
import com.library.utilities.Driver;
import com.library.utilities.LibraryUtils;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.*;
import io.restassured.specification.RequestSpecification;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;
import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;


public class LibrarianStepDefs {

    RequestSpecification givenPart =RestAssured.given().log().all();
    Response response;
    JsonPath jsonPath;
    ValidatableResponse thenPart;

    @Given("I logged Library api as a {string}")
    public void ı_logged_library_api_as_a(String role) {
        givenPart.header("x-library-token", LibraryUtils.generateTokenByRole(role));
    }

    @Given("Accept header is {string}")
    public void accept_header_is(String acceptHeader) {
        givenPart.accept(acceptHeader);
    }

    @When("I send GET request to {string} endpoint")
    public void ıSendGETRequestToEndpoint(String endPoint) {
        response = givenPart.when().get(endPoint);
        thenPart = response.then();
        jsonPath=thenPart.extract().jsonPath();
    }

    @Then("status code should be {int}")
    public void status_code_should_be(int expectedStatusCode) {
       assertEquals(expectedStatusCode,response.statusCode());
    }

    @Then("Response Content type is {string}")
    public void response_content_type_is(String expectedContentType) {
        assertEquals(expectedContentType,response.contentType());
        //thenPart.contentType(expectedContentType);
    }

    @Then("{string} field should not be null")
    public void field_should_not_be_null(String path) {
        assertNotNull(jsonPath.getString(path));
        thenPart.assertThat().body(path,is(notNullValue()));
    }

    String pathParam;
    @And("Path param is {string}")
    public void pathParamIs(String pathParam) {
        this.pathParam=pathParam;
        givenPart.pathParam("id",pathParam);
    }

    @Then("{string} field should be same with path param")
    public void field_should_be_same_with_path_param(String expectedPath) {
        //System.out.println("pathParam = " + pathParam);
        assertEquals(jsonPath.getString(expectedPath),pathParam);
    }
    @Then("following fields should not be null")
    public void following_fields_should_not_be_null(List<String>expectedPaths) {
        //since expectedPaths values are string,I used getString
        for (String eachExpectedPath : expectedPaths) {
            assertNotNull(jsonPath.getString(eachExpectedPath));
        }
    }

    @And("Request Content Type header is {string}")
    public void requestContentTypeHeaderIs(String expectedRequestHeader) {
        givenPart.contentType(expectedRequestHeader);
    }
    Map<String,Object> dataMap;
    @And("I create a random {string} as request body")
    public void ıCreateARandomAsRequestBody(String dataType) {

        switch (dataType){
            case "book":
                dataMap =LibraryUtils.createBook();
                break;
            case "user":
                dataMap= LibraryUtils.createUser();
                break;
            default:
                throw new RuntimeException("Wrong data type is provide");
        }
        givenPart.formParams(dataMap);
    }

    //String bookId;

    @When("I send POST request to {string} endpoint")
    public void ıSendPOSTRequestToEndpoint(String endPoint) {
        response =givenPart.post(endPoint);//here since we do post request we need to assign it to the response
        thenPart= response.then();         //response.then assign it to the then part
        jsonPath=thenPart.extract().jsonPath();//assign to the jp
        //if you do not do all these three step after get post put patch assertions don't work
        //bookId=jsonPath.getString("book_id");
    }

    @And("the field value for {string} path should be equal to {string}")
    public void theFieldValueForPathShouldBeEqualTo(String expectedPath, String expectedValue) {
        assertEquals(expectedValue,jsonPath.getString(expectedPath));
        System.out.println("jsonPath.getString(\"user_id\") = " + jsonPath.getString("user_id"));
    }

    LoginPage loginPage = new LoginPage();
    @And("I logged in Library UI as {string}")
    public void ıLoggedInLibraryUIAs(String role) {
       loginPage.login(role);
        BrowserUtils.waitFor(1);

    }

    @And("I navigate to {string} page")
    public void ıNavigateToPage(String module) {
        loginPage.booksModule.click();
        BrowserUtils.waitFor(1);
    }

    BooksPage booksPage= new BooksPage();
    @And("UI, Database and API created book information must match")
    public void uıDatabaseAndAPICreatedBookInformationMustMatch() {
        String bookId = jsonPath.getString("book_id");
        //get data from DB as a map
        String query = "select id,name,isbn,year,author from books where id ="+bookId;
        DB_Util.runQuery(query);
        Map<String,String> dbMap= DB_Util.getRowMap(1);

        //verify book_id,isbn from api is equal to db
        assertEquals(jsonPath.getString("book_id"),dbMap.get("id"));//api&db verification

        //filter and get the value from UI by api data
        Select select= new Select(booksPage.bookCategories);
        //here book_category_id gives me which category to chose(by value which is number) for the filtering on UI(Book Categories)
        select.selectByValue(""+dataMap.get("book_category_id"));
        String bookName = (String) dataMap.get("name");
        booksPage.searchBox.sendKeys(bookName);

        //UI&DB
        String isbn =Driver.get().findElement(By.xpath("//td[.='"+dataMap.get("isbn")+"']")).getText();
        assertEquals(isbn,dbMap.get("isbn"));//UI&DB

        //verify isbn of UI is equal to api and db
        assertEquals(dataMap.get("isbn"),isbn);//requestbody&UI

    }

    @And("created user information should match with Database")
    public void createdUserInformationShouldMatchWithDatabase() {
        String id= jsonPath.getString("user_id");
        String query = "select email,password from users where id="+id;
        DB_Util.runQuery(query);
        Map<String ,String> userMap = DB_Util.getRowMap(1);
        assertEquals(dataMap.get("email"),userMap.get("email"));

    }

    @And("created user should be able to login Library UI")
    public void createdUserShouldBeAbleToLoginLibraryUI() {
        loginPage.login((String) dataMap.get("email"),(String) dataMap.get("password"));

    }

    @And("created user name should appear in Dashboard Page")
    public void createdUserNameShouldAppearInDashboardPage() {
        //I tried maybe 20 times but didn't work until adding wait,gave me null pointer exception everytime
       String actualUserName = BrowserUtils.waitForClickablility(loginPage.userName,10).getText();

        assertEquals(dataMap.get("full_name"),actualUserName);

    }

    String email;//wasted 1 day for these two assertion,solution is assigning email and password class level
    String password;
    @Given("I logged Library api with credentials {string} and {string}")
    public void ıLoggedLibraryApiWithCredentialsAnd(String email, String password) {
        this.email=email;
        this.password=password;
      givenPart=givenPart.header("x-library-token",LibraryUtils.getToken(email,password));

    }


    @And("I send token information as request body")
    public void ıSendTokenInformationAsRequestBody() {
        givenPart.formParam("token",LibraryUtils.getToken(email,password));
    }
}
