package client;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import model.UserCredentialsModel;
import model.UserRequestModel;

import static io.restassured.RestAssured.given;

public class UserClient {

    private final static String USER_PATH = "/auth";

    @Step("Создание пользователя")
    public ValidatableResponse createUser(UserRequestModel user) {
        return given()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .post(USER_PATH + "/register")
                .then();
    }

    @Step("Удаление пользователя")
    public ValidatableResponse deleteUser(String token) {
        return given()
                .contentType(ContentType.JSON)
                .auth().oauth2(token)
                .when()
                .delete(USER_PATH + "/user")
                .then();
    }

    @Step("Авторизация пользователя в системе")
    public ValidatableResponse userLogin(UserCredentialsModel credentials) {
        return given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post(USER_PATH + "/login")
                .then();
    }

    @Step("Изменение данных пользователя")
    public ValidatableResponse changeUser(UserCredentialsModel credentials, String token) {
        return given()
                .contentType(ContentType.JSON)
                .auth().oauth2(token)
                .body(credentials)
                .when()
                .patch(USER_PATH + "/user")
                .then();
    }
}
