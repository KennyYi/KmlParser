package com.kenny.kmlparser

import android.content.Context
import android.support.annotation.NonNull
import android.util.Log
import io.reactivex.Observable
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

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

            return parse(pullParser, clazz)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun <T : Any>parse(@NonNull pullParser: XmlPullParser, @NonNull clazz: Class<T>): Observable<T> {

        if (pullParser.name.equals(clazz.simpleName, true)) {

            val instance = clazz.newInstance()
            val attributeMap = HashMap<String, Any>()

            for (index in 0..(pullParser.attributeCount - 1)) {
                attributeMap[pullParser.getAttributeName(index)] = pullParser.getAttributeValue(index)
            }

            for (field in clazz.declaredFields) {

                val name = field.name
                val property = instance::class.memberProperties.find { it.name == name }
                if (property is KMutableProperty<*>) {
                    property.setter.call(instance, attributeMap[name])
                }
            }

            while (pullParser.next() != XmlPullParser.END_TAG) {
                if (pullParser.eventType != XmlPullParser.START_TAG) continue
                Log.d("Kenny", "next name: ${pullParser.name}, ${pullParser.namespace}")

                for (field in clazz.declaredFields) {
                    if (pullParser.name.equals(field.name, true)) {

                        Log.d("Kenny", "field: ${field.type}")
                    }
                }
            }

            return Observable.just(instance)
        }

        return Observable.empty()
    }
}

