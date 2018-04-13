package org.homunculus.android.example.concept;

import android.content.Context;
import android.support.v7.app.AlertDialog.Builder;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculus.android.component.module.storage.Persistent;
import org.homunculus.android.core.ActivityCallback;
import org.homunculus.android.example.R;
import org.homunculus.android.example.concept.AsyncControllerA.InvokeControllerANextUIS;
import org.homunculusframework.factory.flavor.hcf.Bind;
import org.homunculusframework.factory.scope.Scope;
import org.homunculusframework.navigation.Navigation;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

/**
 * Created by Torben Schinke on 16.03.18.
 */
@Bind
public class UISA extends LinearLayout {
    private static AtomicInteger INSTANCE_COUNT = new AtomicInteger();
    private static AtomicInteger LIFECYCLE_COUNT = new AtomicInteger();

    @Inject
    Persistent<Boolean> testPersistent2;

    @Inject
    ControllerA controllerA;

    @Inject
    EventAppCompatActivity activity;


    @Inject
    Navigation navigation;

    @Inject
    FancyPojo fancyPojo;

    @Inject
    FancyPojo2 fancyPojo2;

    @Inject
    Scope scope;

    @Inject
    ConceptToolbarConfiguration toolbarTemplate;

    @Inject
    AsyncControllerA asyncControllerA;

    @Inject
    ActivityCallback<?> activityCallback;

    @Inject
    Context regression;

    @Inject
    FilterIndex index;

    private int helloCounter;

    public UISA(Context context) {
        super(context);
        LoggerFactory.getLogger(getClass()).info("instances: {}", INSTANCE_COUNT.incrementAndGet());
    }


    @PostConstruct
    void apply() {
        LoggerFactory.getLogger(getClass()).info("LIFE instances: {}", LIFECYCLE_COUNT.incrementAndGet());

        setOrientation(VERTICAL);

        toolbarTemplate.setUpAction(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), "Hello from Toolbar", Toast.LENGTH_LONG).show();
            }
        }).setToolbarColor(R.color.toolbarColor).setElevation(25);
        activity.setContentView(toolbarTemplate.createToolbar(this));

        Button btn = new Button(getContext());
        btn.setText("go directly to UISB");
        btn.setOnClickListener(v -> {
            navigation.forward(new BindUISB(new UISBModel()));
        });
        addView(btn);


        Button btn2 = new Button(getContext());
        btn2.setText("navigate through controller");
        btn2.setOnClickListener(v -> {
            navigation.forward(new InvokeControllerANextUIS("asdf"));
        });
        addView(btn2);

        Button btn3 = new Button(getContext());
        btn3.setText("say async hello");
        btn3.setOnClickListener(v -> {
            asyncControllerA.sayHelloToA(helloCounter++).whenDone(res -> {
                Builder builder = new Builder(getContext());
                builder.setMessage("hello world no " + res.get() + " [cancelled=" + res.isCancelled() + ", outdated=" + res.isOutdated() + "]");
                builder.setNeutralButton("k", (dialog, which) -> {
                });
                builder.create().show();
            });
        });
        addView(btn3);

        Button btn4 = new Button(getContext());
        btn4.setText("say async hello interruptible and cancelable");
        btn4.setOnClickListener(v -> {
            asyncControllerA.sayHelloToA2(helloCounter++).whenDone(res -> {
                Builder builder = new Builder(getContext());
                builder.setMessage("hello world no " + res.get() + " [cancelled=" + res.isCancelled() + ", outdated=" + res.isOutdated() + "]");
                builder.setNeutralButton("k", (dialog, which) -> {
                });
                builder.create().show();
            });
        });
        addView(btn4);


    }

    @PreDestroy
    void onDestroy() {
        LoggerFactory.getLogger(getClass()).info("LIFE instances: {}", LIFECYCLE_COUNT.decrementAndGet());
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        LoggerFactory.getLogger(getClass()).info("instances: {}", INSTANCE_COUNT.decrementAndGet());
    }
}
