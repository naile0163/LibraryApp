package com.library.pages;

import com.library.utilities.BrowserUtils;
import com.library.utilities.Driver;
import com.library.utilities.LibraryUtils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.Map;

public class LoginPage extends BasePage{

    @FindBy(id = "inputEmail")
    public WebElement emailBox;

    @FindBy(id = "inputPassword")
    public WebElement passwordBox;

    @FindBy(xpath = "//button[.='Sign in']")
    public WebElement signInButton;

    public void login(String role) {

        // Get Credentials
        Map<String, String> roleCredentials = LibraryUtils.returnCredentials(role);
        String email = roleCredentials.get("email");
        String password = roleCredentials.get("password");

        // login
        login(email,password);

    }

    public void login(String email,String password) {

        emailBox.sendKeys(email);
        passwordBox.sendKeys(password);
        BrowserUtils.waitFor(1);
        signInButton.click();

    }


}
