package tests;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import jdk.jfr.Description;
import model.UserCredentialsModel;
import model.UserRequestModel;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class ChangeUserDataTest extends BaseUtils {
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
        user.setEmail(credential.getEmail());
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

    @Description("Успешное изменение данных пользователя")
    @Test
    public void successChangeUserDataTest() {
        String response = given()
                .contentType(ContentType.JSON)
                .body(credential)
                .when()
                .post("/auth/login")
                .then()
                .extract().asString();
        JsonPath j = new JsonPath(response);
        accessToken = j.getString("accessToken").substring(7);
        String random = RandomStringUtils.randomAlphabetic(8);
        credential = new UserCredentialsModel(random + "ya.ru", user.getPassword());
        given()
                .contentType(ContentType.JSON)
                .auth().oauth2(accessToken)
                .body(credential)
                .when()
                .patch("/auth/user")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(credential.getEmail().toLowerCase()))
                .body("user.name", equalTo(user.getName()));
    }

    @Description("Попытка изменения данных пользователя без авторизации")
    @Test
    public void failureChangeUserDataTest() {
        given()
                .contentType(ContentType.JSON)
                .auth().oauth2(accessToken)
                .body(credential)
                .when()
                .patch("/auth/user")
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
}
