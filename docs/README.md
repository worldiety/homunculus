# homunculus

The homunculus framework proposes solutions and receipts for building large Android applications.
Homunculus makes it easy to create production-grade applications and services with absolute minimum hassle. It takes an opinionated view of the Android platform so that new and existing users can quickly get the job done respecting quality requirements.

## Latest versions
#TODO Update Readme

io.homunculus:hcf-core:[ ![Download](https://api.bintray.com/packages/worldiety/homunculus/hcf-core/images/download.svg) ](https://search.maven.org/artifact/io.homunculus/hcf-core)

io.homunculus:hcf-context:[ ![Download](https://api.bintray.com/packages/worldiety/homunculus/hcf-context/images/download.svg) ](https://search.maven.org/artifact/io.homunculus/hcf-context)

io.homunculus:hcf-android-component:[ ![Download](https://api.bintray.com/packages/worldiety/homunculus/hcf-android-component/images/download.svg) ](https://search.maven.org/artifact/io.homunculus/hcf-android-component)

io.homunculus:hcf-android-core:[ ![Download](https://api.bintray.com/packages/worldiety/homunculus/hcf-android-core/images/download.svg) ](https://search.maven.org/artifact/io.homunculus/hcf-android-core)

io.homunculus:hcf-api-spring:[ ![Download](https://api.bintray.com/packages/worldiety/homunculus/hcf-api-spring/images/download.svg) ](https://search.maven.org/artifact/io.homunculus/hcf-api-spring)

## Usage

1) Setup your Android project as usual
2) Add the following to your build.gradle (app module)

```groovy
    //hcf for android, also includes the core and context artifacts
    implementation group: 'io.homunculus', name: 'hcf-android-core', version: '0.0.+'

    //advanced and optional hcf components for android
    implementation group: 'io.homunculus', name: 'hcf-android-component', version: '0.0.+'

    //hcf spring annotation support, required by android-core by default
    implementation group: 'io.homunculus', name: 'hcf-api-spring', version: '0.0.+'
    
     //this is for @inject or @named, required by android-core by default
    implementation 'javax.inject:javax.inject:1'

    //this is for @PostConstruct or @PreDestroy, required by android-core by default
    implementation 'javax.annotation:javax.annotation-api:1.3.2
'
    
    //SLF4j is used by all hcf components, so provide a simple output to console
    implementation 'org.slf4j:slf4j-simple:1.7.25'
    
```

3) Create your own application class

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
          	
</application>            
```

4) Create your Activity

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
        if (!onDispatchNavigationBackPressed()) {
            super.onBackPressed();
        }
    }
}

```