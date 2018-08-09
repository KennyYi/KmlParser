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
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

class KmlParser private constructor(context: Context) {

    private val TAG: String = KmlParser::class.java.simpleName
    private var appContext = context.applicationContext
    private var pullParser = XmlPullParserFactory.newInstance().newPullParser()

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

        inputStream.use {
            pullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            pullParser.setInput(inputStream, null)
            pullParser.nextTag()

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

                return Observable.just(instance)
            }

            return Observable.empty()
        }
    }
}

