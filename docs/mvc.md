# Getting started - Classic MVC

This guide shows you an example of building a controller driven MVC "hello world"
flow with Homunculus with Android.

# Prerequisite
This example expects, that you already have configured your Android Project and included
all required dependencies (see [Usage](README.md) for more details).

# Create a controller

In Homunculus you are encouraged to create a ViewModel for each independent view state.
You usually do this by using the @Singelton and @Named annotations.

```
src/main/java/hello/GreetingController.java
```

```java
import javax.inject.Named;
import javax.inject.Singleton;
import org.homunculusframework.navigation.ModelAndView;

@Singleton
public class GreetingController {
	@Named("/greeting")
    public ModelAndView getCart(@Named("name") String name){
    	return new ModelAndView("/GreetingUIS").put("name", name);
    }
}
```

The @Singelton annotation declares a class as suitable for a controller which can be
added into a Container's configuration.

In your application, also add the new controller just like

```java
public class MyApplication extends CompatApplication {


    @Override
    public void onCreate() {
        super.onCreate();

        //configure HCF for Android
        long start = System.currentTimeMillis();
        Configuration cfg = createConfiguration();
        cfg.add(GreetingController.class);
        
        //setup and start the HCF container
        Container container = new Container(cfg);
        container.start();
    }
}
```

