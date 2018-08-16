package com.kenny.kmlparser

import android.content.Context
import android.support.annotation.NonNull
import io.reactivex.Observable
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

            return Observable.just(parse(pullParser, clazz))
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun <T : Any>parse(@NonNull pullParser: XmlPullParser, @NonNull clazz: Class<T>): T? {

        Log.d("Kenny", "parse > ${clazz.simpleName}")

        if (pullParser.name.equals(clazz.simpleName, true)) {

            val instance = clazz.newInstance()
            val attributeMap = HashMap<String, Any>()

            for (index in 0..(pullParser.attributeCount - 1)) {
                attributeMap[pullParser.getAttributeName(index)] = pullParser.getAttributeValue(index)
            }

            // To check annotations
            instance::class.memberProperties.forEach { property ->

                // If @Property is used for variable..
                val name = property.annotations.find { it is Property }
                        ?.let {
                            (it as Property).name
                        } ?: property.name

                if (attributeMap[name] != null) {
                    property.takeIf { it is KMutableProperty<*> }
                            ?.let { (it as KMutableProperty<*>).setter.call(instance, attributeMap[name]) }
                } else {

                    val field = clazz.declaredFields.find { it.name == property.name }
                    field?.let {
                        (property as KMutableProperty<*>).setter.call(instance, parse(pullParser, field.type))
                    }
                }
            }

            // Use @ElementType Annotation to know Element class type of Collection
            if (instance is Collection<*>) {

                // Check element class with annotation
                val annotations = clazz.annotations
                val elementType = annotations.find { it is ElementType }

                elementType?.let {
                    val elementClass = (elementType as ElementType).element
                    while (pullParser.next() != XmlPullParser.END_TAG) {
                        val element = parse(pullParser, elementClass.java).takeIf { element ->  element != null }
                        CollectionsKt.addIfNotNull(instance, element)
                    }
                }
            }

            while (pullParser.next() != XmlPullParser.END_TAG) {
                if (pullParser.eventType == XmlPullParser.END_DOCUMENT) break
                if (pullParser.eventType == XmlPullParser.TEXT) {

                    // Handle @XmlText annotation
                    val property = instance::class.memberProperties.mapNotNull {
                        it.takeIf {
                            member -> member.annotations.find { annotation -> annotation is XmlText } != null
                        }
                    }.firstOrNull()

                    if (property != null) {
                        (property as KMutableProperty<*>).setter.call(instance, pullParser.text)
                    }
                } else if (pullParser.eventType == XmlPullParser.START_TAG) {
                    Log.i("Kenny", "parser name: ${pullParser.name}")

                }
            }

            return instance
        }
}

