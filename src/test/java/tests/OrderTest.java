package tests;

import client.OrderClient;
import client.UserClient;
import io.qameta.allure.Description;
import io.restassured.response.ValidatableResponse;
import model.OrderRequestModel;
import model.UserCredentialsModel;
import model.UserRequestModel;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static client.IngredientsClient.createBurger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class OrderTest extends BaseUtils {
    private UserClient userClient;
    private OrderClient orderClient;
    private UserRequestModel user;
    private UserCredentialsModel credential;
    private String accessToken = "";
    private OrderRequestModel ingredients;

    @BeforeEach
    public void setupEach() {
        userClient = new UserClient();
        orderClient = new OrderClient();
        String random = RandomStringUtils.randomAlphabetic(8);
        user = new UserRequestModel(random + "@ya.ru", "123", random);
        credential = new UserCredentialsModel(user.getEmail(), user.getPassword());
        userClient.createUser(user);
        ingredients = new OrderRequestModel(createBurger());
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

    @Description("Успешное создание заказа")
    @Test
    public void createOrderSuccessTest() {
        ValidatableResponse loginResponse = userClient.userLogin(credential);
        accessToken = loginResponse.extract().path("accessToken").toString().substring(7);
        ValidatableResponse response = orderClient.createOrder(ingredients, accessToken);
        assertEquals(200, response.extract().statusCode());
        assertEquals(true, response.extract().path("success"));
        assertFalse(response.extract().path("order.status").toString().isEmpty()
                || response.extract().path("order.status").toString().isBlank());
        assertFalse(response.extract().path("order.name").toString().isEmpty()
                || response.extract().path("order.name").toString().isBlank());
        assertFalse(response.extract().path("order.createdAt").toString().isEmpty()
                || response.extract().path("order.createdAt").toString().isBlank());
        assertFalse(response.extract().path("order.updatedAt").toString().isEmpty()
                || response.extract().path("order.updatedAt").toString().isBlank());
        assertFalse(response.extract().path("order.number").toString().isEmpty()
                || response.extract().path("order.number").toString().isBlank());
        assertFalse(response.extract().path("order.price").toString().isEmpty()
                || response.extract().path("order.price").toString().isBlank());
    }

    @Description("Создание заказа без авторизации")
    @Test
    public void createOrderWithUnauthorizedUser() {
        ValidatableResponse response = orderClient.createOrder(ingredients, "");
        assertEquals(200, response.extract().statusCode());
        assertEquals(true, response.extract().path("success"));
        assertFalse(response.extract().path("name").toString().isEmpty()
                || response.extract().path("name").toString().isBlank());
        assertFalse(response.extract().path("order.number").toString().isEmpty()
                || response.extract().path("order.number").toString().isBlank());
    }

    @Description("Создание заказа без ингредиентов")
    @Test
    public void createOrderWithoutIngredientsTest() {
        ingredients = new OrderRequestModel(new ArrayList<>());
        ValidatableResponse response = orderClient.createOrder(ingredients, accessToken);
        assertEquals(400, response.extract().statusCode());
        assertEquals(false, response.extract().path("success"));
        assertEquals("Ingredient ids must be provided", response.extract().path("message"));
    }

    @Description("Создание заказа с неверным хешем ингридиентов")
    @Test
    public void createOrderWithIncorrectIngredientsTest() {
        ArrayList<Object> arr = new ArrayList<>();
        arr.add(UUID.randomUUID().toString());
        ingredients = new OrderRequestModel(arr);
        ValidatableResponse response = orderClient.createOrder(ingredients, accessToken);
        assertEquals(500, response.extract().statusCode());
    }

    @Description("Получение заказов под авторизованным пользователем")
    @Test
    public void getOrdersListOnAuthorizedUserTest() {
        ValidatableResponse loginResponse = userClient.userLogin(credential);
        accessToken = loginResponse.extract().path("accessToken").toString().substring(7);
        ValidatableResponse createOrderResponse = orderClient.createOrder(ingredients, accessToken);
        String orderNumber = createOrderResponse.extract().path("order.number").toString();
        ValidatableResponse response = orderClient.getUserOrders(accessToken);
        assertEquals(200, response.extract().statusCode());
        assertEquals(orderNumber, response.extract().path("orders.number[0]").toString());
    }

    @Description("Получение заказов под неавторизованным пользователем")
    @Test
    public void getOrdersListOnUnathorizedUser() {
        ValidatableResponse response = orderClient.getUserOrders("");
        assertEquals(401, response.extract().statusCode());
        assertEquals(false, response.extract().path("success"));
        assertEquals("You should be authorised", response.extract().path("message"));
    }
}
