package com.kenny.kmlparser

import android.content.Context
import com.kenny.kmlparser.annotations.ElementType
import com.kenny.kmlparser.annotations.Property
import com.kenny.kmlparser.annotations.XmlText
import io.reactivex.Observable
import io.reactivex.annotations.NonNull
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.internal.impl.utils.CollectionsKt

/**
 * KmlParser is XML Parser by Kotlin
 */
class KmlParser private constructor(context: Context) {

    private val TAG: String = KmlParser::class.java.simpleName
    private var appContext = context.applicationContext

    init {
        // Initializing
    }

    companion object: SingletonHolder<KmlParser, Context>(:: KmlParser)

    @Throws(XmlPullParserException::class, IOException::class)
    fun <T : Any>parse(@NonNull path: String, @NonNull clazz: Class<T>): Observable<T> {

        return parse(File(path), clazz)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun <T : Any>parse(@NonNull file: File, @NonNull clazz: Class<T>): Observable<T> {

        return parse(FileInputStream(file), clazz)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun <T : Any>parse(@NonNull inputStream: InputStream, @NonNull clazz: Class<T>): Observable<T> {

        val pullParser = XmlPullParserFactory.newInstance().newPullParser()

        inputStream.use {
            pullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            pullParser.setInput(inputStream, null)
            pullParser.nextTag()

            val instance = parse(pullParser, clazz)

            return when (instance) {
                null -> Observable.empty()
                else -> Observable.just(instance)
            }
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun <T : Any>parse(@NonNull pullParser: XmlPullParser, @NonNull clazz: Class<T>): T? {

        val className = clazz.annotations.find { it is Property }?.let { (it as Property).name }?:clazz.simpleName

        if (pullParser.name.equals(className, true)) {

            val instance = clazz.newInstance()
            val attributeMap = HashMap<String, Any>()

            // If this class is a collection, check element type
            val elementProperty = instance.takeIf { it is Collection<*> }.let {
                clazz.annotations.find { annotation -> annotation is ElementType }?.let { annotation ->

                    (annotation as ElementType).let { element ->

                        val name = element.elementName.isEmpty().let { isEmpty ->
                            when (isEmpty) {
                                true -> element.element.simpleName
                                false -> element.elementName
                            }
                        }
                        Pair(name, element.element)
                    }
                }
            }

            // Mapping and saving XML attribute name and value
            for (index in 0..(pullParser.attributeCount - 1)) {
                attributeMap[pullParser.getAttributeName(index)] = pullParser.getAttributeValue(index)
            }

            instance::class.memberProperties.forEach {

                val name = it.annotations.find { annotation -> annotation is Property}
                        ?.let { property -> (property as Property).name }
                        ?:it.name

                attributeMap[name]?.let { value ->
                    if (it is KMutableProperty<*>) it.setter.call(instance, value)
                }
            }

            while (pullParser.next() != XmlPullParser.END_DOCUMENT) {

                if (pullParser.eventType == XmlPullParser.END_TAG && pullParser.name.equals(className, true)) break

                if (pullParser.eventType == XmlPullParser.TEXT) {
                    // Check @XmlText annotation
                    instance::class.memberProperties.find {
                        it.annotations.find { annotation -> annotation is XmlText } != null
                    }?.let { (it as KMutableProperty<*>).setter.call(instance, pullParser.text) }
                }

                if (pullParser.eventType == XmlPullParser.START_TAG) {

                    // Collection
                    if (elementProperty != null && pullParser.name.equals(elementProperty.first, true)) {
                        val element = parse(pullParser, elementProperty.second.java).takeIf { element ->  element != null }
                        CollectionsKt.addIfNotNull(instance as Collection<*>, element)
                    } else {
                        clazz.declaredFields.forEach { field ->
                            val name = field.annotations.find { annotation -> annotation is Property}
                                    ?.let { property -> (property as Property).name }
                                    ?:field.name

                            if (pullParser.name.equals(name, true)) {
                                val property = instance::class.memberProperties.find { it.name == field.name }
                                if (property is KMutableProperty<*>) property.setter.call(instance, parse(pullParser, field.type))
                            }
                        }
                    }
                }
            }

            return instance
        }

        return null
    }
}

