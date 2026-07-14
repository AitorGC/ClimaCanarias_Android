package com.example

import org.junit.Test
import com.squareup.moshi.Types
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class MoshiAdapterTest {
    @Test
    fun testAdapter() {
        val listType = Types.newParameterizedType(
            List::class.java,
            Map::class.java,
            String::class.java,
            Any::class.java
        )
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        try {
            val adapter = moshi.adapter<List<Map<String, Any?>>>(listType)
            val list = adapter.fromJson("[{\"fint\":\"hola\"}]")
            println(list)
        } catch (e: Exception) {
            println("Exception: ${e.message}")
            e.printStackTrace()
        }
    }
}
