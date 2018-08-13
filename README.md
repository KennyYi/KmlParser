# KmlParser

KmlParser is XML Parser by Kotlin

## Install

Add repository in your root `build.gradle`

```groovy
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```

Add library dependency in your app `build.gradle`

```groovy
dependencies {
    ...
    // KmlParser return result as a Observable<*>
    implementation 'io.reactivex.rxjava2:rxandroid:latest'
    implementation 'com.github.KennyYi:KmlParser:0.0.2'
    ...
}
```

## How to use

```Kotlin
// Create a model object.
class Products {
    var version: String? = null
    var id: String? = null
}
```

```Kotlin
KmlParser.getInstance(this).parse(filePath, Products::class.java)
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe {
        // it: Products
        // TODO your job
    }
```

For `Collection` model, use `@ElementType(val element: KClass<*>)`.

```Kotlin
@ElementType(Result::class)
class Results: HashSet<Result>()

...

class Result {

    var parsername: String? = null
    var name: String? = null
    var type: String? = null
}
```

For `Java` file, please make `public` for variables.

```java
public class Device {

    public String version;
    public String name;
}
```

Variable name is different with XML, uses `@Property(val name: String)` annotaion.

```xml
<device version="0.1" name="android" />
```

```Kotlin
class Device {
    @Property(name="version") var ver: String? = null
    var name: String? = null
}
```

## Proguard

TBD
