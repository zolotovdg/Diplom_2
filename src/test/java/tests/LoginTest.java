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
import static org.junit.jupiter.api.Assertions.assertFalse;

public class LoginTest extends BaseUtils {
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
        credential = new UserCredentialsModel(user.getEmail(), user.getPassword());
        ValidatableResponse response = userClient.userLogin(credential);
        if (response.extract().path("accessToken") != null) {
            accessToken = response.extract().path("accessToken").toString().substring(7);
            userClient.deleteUser(accessToken);
        }
    }

    @Description("Успешный вход в систему")
    @Test
    public void successLoginTest() {
        ValidatableResponse response = userClient.userLogin(credential);
        assertEquals(200, response.extract().statusCode());
        assertEquals(true, response.extract().path("success"));
        assertEquals(user.getEmail().toLowerCase(), response.extract().path("user.email"));
        assertEquals(user.getName(), response.extract().path("user.name"));
        assertFalse(response.extract().path("accessToken").toString().isEmpty());
        assertFalse(response.extract().path("refreshToken").toString().isEmpty());
    }

    @Description("Попытка входа с неверным логином и паролем")
    @Test
    public void loginWithIncorrectCredentialsTest() {
        credential = new UserCredentialsModel(user.getEmail(), "1");
        ValidatableResponse response = userClient.userLogin(credential);
        assertEquals(401, response.extract().statusCode());
        assertEquals(false, response.extract().path("success"));
        assertEquals("email or password are incorrect", response.extract().path("message"));
    }
}
