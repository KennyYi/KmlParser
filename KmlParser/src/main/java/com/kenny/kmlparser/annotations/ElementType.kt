package com.kenny.kmlparser.annotations

import kotlin.reflect.KClass

/**
 * element is T of Collection<T>
 */
annotation class ElementType(val element: KClass<*>, val elementName: String = "")