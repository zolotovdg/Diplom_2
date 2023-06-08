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

public class UserCreateTest extends BaseUtils {
    private UserRequestModel user;
    private UserCredentialsModel credential;
    private String accessToken = "";

    @BeforeEach
    public void setupEach() {
        String random = RandomStringUtils.randomAlphabetic(8);
        user = new UserRequestModel(random + "@ya.ru", "123", random);
        credential = new UserCredentialsModel(user.getEmail(), user.getPassword());
    }

    @AfterEach
    public void teardown() {
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

    @Description("Успешное создание пользователя")
    @Test
    public void createUserSuccessTest() {
        given()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(user.getEmail().toLowerCase()))
                .body("user.name", equalTo(user.getName()));
    }

    @Description("Попытка создания уже зарегистрированного пользователя")
    @Test
    public void createDuplicateUserTest() {
        given()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .post("/auth/register");
        given()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Description("Попытка создания пользователя без заполнения поля email")
    @Test
    public void createUserWithInvalidData() {
        user = new UserRequestModel("", "123", "name");
        given()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }
}
