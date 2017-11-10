package org.homunculus.android.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.LinearLayout;

import org.homunculus.android.example.module.cart.CartActivity;
import org.homunculus.android.example.module.flow.FlowActivity;
import org.homunculus.android.example.module.toolbar.ToolbarActivity;
import org.homunculusframework.scope.Scope;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout layout = new LinearLayout(this);
        setContentView(layout);

        Button btnCart = new Button(this);
        btnCart.setText("cart");
        btnCart.setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
        });
        layout.addView(btnCart);

        Button btnToolbar = new Button(this);
        btnToolbar.setText("toolbar");
        btnToolbar.setOnClickListener(v -> {
            startActivity(new Intent(this, ToolbarActivity.class));
        });
        layout.addView(btnToolbar);

        Button btnFlow = new Button(this);
        btnFlow.setText("flow");
        btnFlow.setOnClickListener(v -> {
            startActivity(new Intent(this, FlowActivity.class));
        });
        layout.addView(btnFlow);

    }
}
