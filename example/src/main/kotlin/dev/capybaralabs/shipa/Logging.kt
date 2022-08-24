package dev.capybaralabs.shipa

import kotlin.reflect.full.companionObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("UnusedReceiverParameter")
internal inline fun <reified T : Any> T.logger(): Logger = getLogger(getClassForLogging(T::class.java))

internal inline fun <reified T : Any> getClassForLogging(javaClass: Class<T>): Class<*> {
	return javaClass.enclosingClass?.takeIf {
		it.kotlin.companionObject?.java == javaClass
	} ?: javaClass
}

internal fun getLogger(forClass: Class<*>): Logger =
	LoggerFactory.getLogger(forClass)
