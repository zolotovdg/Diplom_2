package tests;

import client.UserClient;
import io.qameta.allure.Description;
import io.restassured.response.ValidatableResponse;
import model.UserCredentialsModel;
import model.UserRequestModel;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChangeUserDataTest extends BaseUtils {
    private UserClient userClient;
    private UserRequestModel user;
    private UserCredentialsModel credential;
    private String accessToken = "";

    @BeforeEach
    public void setupEach() {
        userClient = new UserClient();
        String random = RandomStringUtils.randomAlphabetic(8);
        user = new UserRequestModel(random + "@ya.ru", "123", random);
        credential = new UserCredentialsModel(user.getEmail(), user.getPassword());
        userClient.createUser(user);
    }

    @AfterEach
    public void teardown() {
        user.setEmail(credential.getEmail());
        credential = new UserCredentialsModel(user.getEmail(), user.getPassword());
        ValidatableResponse response = userClient.userLogin(credential);
        if (response.extract().path("accessToken") != null) {
            accessToken = response.extract().path("accessToken").toString().substring(7);
            userClient.deleteUser(accessToken);
        }
    }

    @Description("Успешное изменение данных пользователя")
    @Test
    public void successChangeUserDataTest() {
        ValidatableResponse loginResponse = userClient.userLogin(credential);
        accessToken = loginResponse.extract().path("accessToken").toString().substring(7);
        String random = RandomStringUtils.randomAlphabetic(8);
        credential = new UserCredentialsModel(random + "ya.ru", user.getPassword());
        ValidatableResponse response = userClient.changeUser(credential, accessToken);
        assertEquals(200, response.extract().statusCode());
        assertEquals(true, response.extract().path("success"));
        assertEquals(credential.getEmail().toLowerCase(), response.extract().path("user.email"));
        assertEquals(user.getName(), response.extract().path("user.name"));
    }

    @Description("Попытка изменения данных пользователя без авторизации")
    @Test
    public void failureChangeUserDataTest() {
        ValidatableResponse response = userClient.changeUser(credential, "");
        assertEquals(401, response.extract().statusCode());
        assertEquals(false, response.extract().path("success"));
        assertEquals("You should be authorised", response.extract().path("message"));
    }
}
