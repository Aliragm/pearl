package gg.pearl.plugins

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class KotlinUuidSerializer : JsonSerializer<Uuid>() {
    override fun serialize(value: Uuid, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.toString())
    }
}

@OptIn(ExperimentalUuidApi::class)
class KotlinUuidDeserializer : JsonDeserializer<Uuid>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Uuid {
        return Uuid.parse(p.text)
    }
}

@OptIn(ExperimentalUuidApi::class)
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            val module = SimpleModule().apply {
                addSerializer(Uuid::class.java, KotlinUuidSerializer())
                addDeserializer(Uuid::class.java, KotlinUuidDeserializer())
            }
            registerModule(module)
        }
    }
}