# homunculus

The homunculus framework proposes solutions and receipts for building large Android applications.
Homunculus makes it easy to create production-grade applications and services with absolute minimum hassle. It takes an opinionated view of the Android platform so that new and existing users can quickly get the job done respecting quality requirements.

See the [Quick start](quickstart.md) for more details.




## Usage

1. Setup your Android project as usual
2. Add the following to your build.gradle (app module)

```groovy
    //hcf for android, also includes the core and context artifacts
    compile 'org.homunculusframework:hcf-android-core:0.0.24'
    
    //hcf spring annotation support, required by android-core by default
    compile 'org.homunculusframework:hcf-api-spring:0.0.24'
    
     //this is for @inject or @named, required by android-core by default
    compile 'javax.inject:javax.inject:1'

    //this is for @PostConstruct or @PreDestroy, required by android-core by default
    compile 'javax.annotation:jsr250-api:1.0'
    
    //SLF4j is used by all hcf components, so provide a simple output to console
    compile 'org.slf4j:slf4j-simple:1.7.25'
    
```

```java
public class
```
