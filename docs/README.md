# homunculus

The homunculus framework proposes solutions and receipts for building large Android applications.
Homunculus makes it easy to create production-grade applications and services with absolute minimum hassle. It takes an opinionated view of the Android platform so that new and existing users can quickly get the job done respecting quality requirements.


## Usage

 1. Setup your Android project as usual
 2. Add the following to your build.gradle (app module)

```groovy
    //hcf for android, also includes the core and context artifacts
    compile 'org.homunculusframework:hcf-android-core:0.0.29'
    
    //hcf spring annotation support, required by android-core by default
    compile 'org.homunculusframework:hcf-api-spring:0.0.29'
    
     //this is for @inject or @named, required by android-core by default
    compile 'javax.inject:javax.inject:1'

    //this is for @PostConstruct or @PreDestroy, required by android-core by default
    compile 'javax.annotation:jsr250-api:1.0'
    
    //SLF4j is used by all hcf components, so provide a simple output to console
    compile 'org.slf4j:slf4j-simple:1.7.25'
    
```

 3. Create your own application class

```java
import org.homunculus.android.compat.CompatApplication;
import org.homunculusframework.factory.container.Configuration;
import org.homunculusframework.factory.container.Container;

public class MyApplication extends CompatApplication {

	@Override
    public void onCreate() {
        super.onCreate();
        
        //create the configuration
        Configuration cfg = createConfiguration();
        cfg.add(MyController.class);
        cfg.add(MyUIS.class);
        cfg.add(MyControllerConnection.class);
        
         //setup and start the HCF container
        Container container = new Container(cfg);
        container.start();
    }
}
```

Don't forget to annotate it in your AndroidManifest.xml:

```xml
<application
            android:name=".MyApplication"
            ...
</application>            
```

 4. Create your Activity

```java
import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculusframework.navigation.DefaultNavigation;
import org.homunculusframework.navigation.Navigation;
import org.homunculus.android.core.Android;

public class CartActivity extends EventAppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //show a progress spinner while waiting for the asynchronous result
        setContentView(new ViewWait(this));
        
        //setup the navigation
        Navigation navigation = new DefaultNavigation(getScope());
        getScope().put(Android.NAME_NAVIGATION, navigation);

        //forward to the first UIS
        navigation.forward(new Request(MyUIS.UID));
        
    }
    
    @Override
    public void onBackPressed() {
        Navigation navigation = getScope().resolve(Navigation.class);
        if (navigation != null) {
            if (!navigation.backward()) {
                super.onBackPressed();
            }
        } else {
            LoggerFactory.getLogger(getClass()).error("no navigation available");
            super.onBackPressed();
        }
    }
}

```