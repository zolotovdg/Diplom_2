package client;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.apache.commons.lang3.RandomUtils.nextInt;

public class IngredientsClient {

    public static ArrayList<Object> createBurger() {
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
        return list;
    }
}
