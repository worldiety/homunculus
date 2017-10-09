package org.homunculus.android.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import org.homunculus.android.example.module.cart.CartActivity;
import org.homunculusframework.scope.Scope;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Scope scope = new Scope("hallo", null);
        Button btn = new Button(this);
        btn.setOnClickListener(v -> {
            System.out.println("hallo welt");
            startActivity(new Intent(this, CartActivity.class));
        });
        setContentView(btn);
    }
}
