package client;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import model.OrderRequestModel;

import static io.restassured.RestAssured.given;

public class OrderClient {

    private static final String ORDER_PATH = "/orders";

    @Step("Создать заказ")
    public ValidatableResponse createOrder(OrderRequestModel order, String token) {
        return given()
                .contentType(ContentType.JSON)
                .auth().oauth2(token)
                .body(order)
                .when()
                .post(ORDER_PATH)
                .then();
    }

    @Step("Получить список заказов пользователя")
    public ValidatableResponse getUserOrders(String token) {
        return given()
                .contentType(ContentType.JSON)
                .auth().oauth2(token)
                .when()
                .get(ORDER_PATH)
                .then();
    }
}
