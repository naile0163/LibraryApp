package com.library.pages;

import com.library.utilities.Driver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class BooksPage extends BasePage {


    @FindBy(xpath = "//input[@type='search']")
    public WebElement searchBox;

    @FindBy(id = "book_categories")
    public WebElement bookCategories;


}
