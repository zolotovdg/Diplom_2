package tests;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.junit.jupiter.api.BeforeAll;

public class BaseUtils {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI= "https://stellarburgers.nomoreparties.site/api";
        RestAssured.requestSpecification = new RequestSpecBuilder().addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter()).build();
    }


}
