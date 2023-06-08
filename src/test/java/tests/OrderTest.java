package tests;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.qameta.allure.Description;
import model.OrderRequestModel;
import model.UserCredentialsModel;
import model.UserRequestModel;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class OrderTest extends BaseUtils {
    private UserRequestModel user;
    private UserCredentialsModel credential;
    private String accessToken = "";
    private OrderRequestModel ingredients;

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
        ArrayList<Object> list = new ArrayList<>();
        String response = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/ingredients")
                .then()
                .extract().asString();
        JsonPath j = new JsonPath(response);
        int bunIndex = nextInt(0, 1);
        int mainIndex = nextInt(0, 8);
        int sauceIndex = nextInt(0, 3);
        List<Object> buns = j.getList("data.findAll{it.type == 'bun'}._id");
        List<Object> mains = j.getList("data.findAll{it.type == 'main'}._id");
        List<Object> sauces = j.getList("data.findAll{it.type == 'sauce'}._id");

        list.add(buns.get(bunIndex));
        list.add(mains.get(mainIndex));
        list.add(sauces.get(sauceIndex));

        ingredients = new OrderRequestModel(list);
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

    @Description("Успешное создание заказа")
    @Test
    public void createOrderSuccessTest() {
        String response = given()
                .contentType(ContentType.JSON)
                .body(credential)
                .when()
                .post("/auth/login")
                .then()
                .extract().asString();
        JsonPath j = new JsonPath(response);
        accessToken = j.getString("accessToken").substring(7);

        String responseOrder = given()
                .contentType(ContentType.JSON)
                .auth().oauth2(accessToken)
                .body(ingredients)
                .when()
                .post("/orders")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .extract().asString();
        j = new JsonPath(responseOrder);
        assertFalse(j.getString("order.status").isEmpty()
                || j.getString("order.status").isBlank());
        assertFalse(j.getString("order.name").isEmpty()
                || j.getString("order.name").isBlank());
        assertFalse(j.getString("order.createdAt").isEmpty()
                || j.getString("order.createdAt").isBlank());
        assertFalse(j.getString("order.updatedAt").isEmpty()
                || j.getString("order.updatedAt").isBlank());
        assertFalse(j.getString("order.number").isEmpty()
                || j.getString("order.number").isBlank());
        assertFalse(j.getString("order.price").isEmpty()
                || j.getString("order.price").isBlank());
    }

    @Description("Создание заказа без авторизации")
    @Test
    public void createOrderWithUnauthorizedUser() {
        String responseOrder = given()
                .contentType(ContentType.JSON)
                .auth().oauth2(accessToken)
                .body(ingredients)
                .when()
                .post("/orders")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .extract().asString();
        JsonPath j = new JsonPath(responseOrder);
        assertFalse(j.getString("name").isEmpty()
                || j.getString("name").isBlank());
        assertFalse(j.getString("order.number").isEmpty()
                || j.getString("order.number").isBlank());
    }

    @Description("Создание заказа без ингредиентов")
    @Test
    public void createOrderWithoutIngredientsTest() {
        ingredients = new OrderRequestModel(new ArrayList<>());
        given()
                .contentType(ContentType.JSON)
                .auth().oauth2(accessToken)
                .body(ingredients)
                .when()
                .post("/orders")
                .then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Description("Создание заказа с неверным хешем ингридиентов")
    @Test
    public void createOrderWithIncorrectIngredientsTest() {
        ArrayList<Object> arr = new ArrayList<>();
        arr.add(UUID.randomUUID().toString());
        ingredients = new OrderRequestModel(arr);
        given()
                .contentType(ContentType.JSON)
                .auth().oauth2(accessToken)
                .body(ingredients)
                .when()
                .post("/orders")
                .then()
                .statusCode(500);
    }

    @Description("Получение заказов под авторизованным пользователем")
    @Test
    public void getOrdersListOnAuthorizedUserTest() {
        String response = given()
                .contentType(ContentType.JSON)
                .body(credential)
                .when()
                .post("/auth/login")
                .then()
                .extract().asString();
        JsonPath j = new JsonPath(response);
        accessToken = j.getString("accessToken").substring(7);

        String createOrderResponse = given()
                .contentType(ContentType.JSON)
                .auth().oauth2(accessToken)
                .body(ingredients)
                .when()
                .post("/orders")
                .then()
                .extract().asString();
        j = new JsonPath(createOrderResponse);
        String orderNumber = j.getString("orders.number");

        given()
                .contentType(ContentType.JSON)
                .auth().oauth2(accessToken)
                .when()
                .get("/orders")
                .then()
                .statusCode(200)
                .body("order.number", equalTo(orderNumber));
    }

    @Description("Получение заказов под неавторизованным пользователем")
    @Test
    public void getOrdersListOnUnathorizedUser() {
        given()
                .contentType(ContentType.JSON)
                .auth().oauth2(accessToken)
                .when()
                .get("/orders")
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
}
