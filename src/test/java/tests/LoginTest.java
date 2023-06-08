package tests;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.qameta.allure.Description;
import model.UserCredentialsModel;
import model.UserRequestModel;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class LoginTest extends BaseUtils {

    private UserRequestModel user;
    private UserCredentialsModel credential;
    private String accessToken = "";


    @BeforeEach
    public void setupEach() {
        String random = RandomStringUtils.randomAlphabetic(8);
        user = new UserRequestModel(random + "@ya.ru", "123", random);
        credential = new UserCredentialsModel(user.getEmail(), user.getPassword());
        given()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .post("/auth/register");
    }

    @AfterEach
    public void teardown() {
        credential = new UserCredentialsModel(user.getEmail(), user.getPassword());
        String response = given()
                .contentType(ContentType.JSON)
                .body(credential)
                .when()
                .post("/auth/login")
                .then()
                .extract().asString();
        JsonPath j = new JsonPath(response);
        if (j.getString("accessToken") != null) {
            accessToken = j.getString("accessToken").substring(7);
            given()
                    .contentType(ContentType.JSON)
                    .auth().oauth2(accessToken)
                    .when()
                    .delete("/auth/user");
        }
    }

    @Description("Успешный вход в систему")
    @Test
    public void successLoginTest() {
        String response = given()
                .contentType(ContentType.JSON)
                .body(credential)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(user.getEmail().toLowerCase()))
                .body("user.name", equalTo(user.getName()))
                .extract().asString();
        JsonPath j = new JsonPath(response);
        assertFalse(j.getString("accessToken").isEmpty() || j.getString("accessToken").isBlank());
        assertFalse(j.getString("refreshToken").isEmpty() || j.getString("refreshToken").isBlank());
    }

    @Description("Попытка входа с неверным логином и паролем")
    @Test
    public void loginWithIncorrectCredentialsTest() {
        credential = new UserCredentialsModel(user.getEmail(), "1");
        given()
                .contentType(ContentType.JSON)
                .body(credential)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }
}
