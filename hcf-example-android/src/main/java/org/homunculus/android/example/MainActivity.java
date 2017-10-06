package org.homunculus.android.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
        });
        setContentView(btn);
    }
}
