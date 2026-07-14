package com.example

import org.junit.Test
import com.squareup.moshi.Types

class MoshiCrashTest {
    @Test
    fun testCrash() {
        try {
            val listType = Types.newParameterizedType(
                List::class.java,
                Map::class.java,
                String::class.java,
                Any::class.java
            )
            println("No crash: $listType")
        } catch (e: Exception) {
            println("Crash: ${e.message}")
        }
    }
}
