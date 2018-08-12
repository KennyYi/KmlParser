# KmlParser

KmlParser is XML Parser by Kotlin

## Install

TBD

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

For `Collection` model, use `@ElementType(KClass<*>)`.

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

## Precautions

1. Do not use `val` keyword on XML.
    * Cannot use `val` as a variable name in Kotlin.