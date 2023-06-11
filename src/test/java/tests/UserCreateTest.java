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

public class UserCreateTest extends BaseUtils {
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
    }

    @AfterEach
    public void teardown() {
        ValidatableResponse response = userClient.userLogin(credential);
        if (response.extract().path("accessToken") != null) {
            accessToken = response.extract().path("accessToken").toString().substring(7);
            userClient.deleteUser(accessToken);
        }
    }

    @Description("Успешное создание пользователя")
    @Test
    public void createUserSuccessTest() {
        ValidatableResponse response = userClient.createUser(user);
        assertEquals(200, response.extract().statusCode());
        assertEquals(true, response.extract().path("success"));
        assertEquals(user.getEmail().toLowerCase(), response.extract().path("user.email"));
        assertEquals(user.getName(), response.extract().path("user.name"));
    }

    @Description("Попытка создания уже зарегистрированного пользователя")
    @Test
    public void createDuplicateUserTest() {
        userClient.createUser(user);
        ValidatableResponse response = userClient.createUser(user);
        assertEquals(403, response.extract().statusCode());
        assertEquals(false, response.extract().path("success"));
        assertEquals("User already exists", response.extract().path("message"));
    }

    @Description("Попытка создания пользователя без заполнения поля email")
    @Test
    public void createUserWithInvalidData() {
        user = new UserRequestModel("", "123", "name");
        ValidatableResponse response = userClient.createUser(user);
        assertEquals(403, response.extract().statusCode());
        assertEquals(false, response.extract().path("success"));
        assertEquals("Email, password and name are required fields", response.extract().path("message"));
    }
}
