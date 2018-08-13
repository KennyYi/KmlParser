package com.kenny.kmlparser

import kotlin.reflect.KClass

/**
 * element is T of Collection<T>
 */
annotation class ElementType(val element: KClass<*>)