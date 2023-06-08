package model;

import java.util.ArrayList;

public class OrderRequestModel {
    private ArrayList<Object> ingredients;

    public OrderRequestModel(ArrayList<Object> ingredients) {
        this.ingredients = ingredients;
    }

    public ArrayList<Object> getIngredients() {
        return ingredients;
    }
}
