package org.homunculus.android.example.module.cart;

import java.util.ArrayList;
import java.util.List;

public class CartModel {
    private List<CartEntry> entries = new ArrayList<>();
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<CartEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<CartEntry> entries) {
        this.entries = entries;
    }

}
