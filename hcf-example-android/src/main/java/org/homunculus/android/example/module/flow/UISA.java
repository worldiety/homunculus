package org.homunculus.android.example.module.flow;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.widget.Button;
import android.widget.LinearLayout;

import org.homunculus.android.component.IntentImages;
import org.homunculusframework.navigation.Navigation;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Torben Schinke on 07.11.17.
 */
@Named("/testa")
public class UISA extends LinearLayout {

    @Inject
    private Navigation navigation;

    @Inject
    private Activity activity;

    @Inject
    private IntentImages images;

    public UISA(Context context) {
        super(context);
    }


    @PostConstruct
    private void apply() {
        //throw new RuntimeException("test");
        setBackgroundColor(Color.RED);
        activity.setContentView(this);

        Button btnToB = new Button(getContext());
        btnToB.setText("to B");
        btnToB.setOnClickListener(view -> {
            navigation.forward(new BindUISB());
        });
        addView(btnToB);


        Button btnTakePhoto = new Button(getContext());
        btnTakePhoto.setText("take Photo");
        btnTakePhoto.setOnClickListener(view -> {
            images.cameraIntent().invoke().whenDone(res -> res.log());
        });
        addView(btnTakePhoto);

        images.cameraIntent().whenReceived(res -> {
            try {
                res.log();
                try (InputStream in = activity.getContentResolver().openInputStream(res.get())) {
                    Bitmap bmp = BitmapFactory.decodeStream(in);
                    System.out.println("bitmap size: " + bmp.getWidth() + "x" + bmp.getHeight());
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        Button btnPickImage = new Button(getContext());
        btnPickImage.setText("pick image");
        btnPickImage.setOnClickListener(view -> {
            images.imageIntent().invoke().whenDone(res -> res.log());
        });
        addView(btnPickImage);

        images.imageIntent().whenReceived(res -> {
            try {
                res.log();
                try (InputStream in = activity.getContentResolver().openInputStream(res.get())) {
                    Bitmap bmp = BitmapFactory.decodeStream(in);
                    System.out.println("image bitmap size: " + bmp.getWidth() + "x" + bmp.getHeight());
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
