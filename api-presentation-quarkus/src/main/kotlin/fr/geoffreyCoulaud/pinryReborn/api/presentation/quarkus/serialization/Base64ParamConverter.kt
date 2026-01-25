package fr.geoffreyCoulaud.pinryReborn.api.presentation.quarkus.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Inject
import jakarta.ws.rs.ext.ParamConverter
import jakarta.ws.rs.ext.ParamConverterProvider
import jakarta.ws.rs.ext.Provider
import kotlin.io.encoding.Base64

@Provider
class Base64JsonParamConverterProvider
    @Inject
    constructor(
        private val objectMapper: ObjectMapper,
    ) : ParamConverterProvider {
        override fun <T : Any> getConverter(
            rawType: Class<T>,
            genericType: java.lang.reflect.Type?,
            annotations: Array<out Annotation>?,
        ): ParamConverter<T>? {
            // Only apply to classes annotated with a custom annotation
            return Base64JsonParamConverter(objectMapper, rawType)
                .takeIf { annotations?.any { it is Base64Json } == true }
        }
    }

class Base64JsonParamConverter<T>(
    private val objectMapper: ObjectMapper,
    private val targetClass: Class<T>,
) : ParamConverter<T> {
    override fun fromString(value: String?): T? {
        if (value == null) return null
        return Base64
            .decode(value)
            .decodeToString()
            .let { objectMapper.readValue(it, targetClass) }
    }

    override fun toString(value: T?): String? {
        if (value == null) return null
        return objectMapper
            .writeValueAsString(value)
            .let { it.toByteArray() }
            .let { Base64.encode(it) }
    }
}

// Custom annotation to mark which params should use this converter
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Base64Json
