package com.example

import org.junit.Test
import com.squareup.moshi.Types

class MoshiTypeTest {
    @Test
    fun testTypes() {
        val listType = Types.newParameterizedType(
            List::class.java,
            Map::class.java,
            String::class.java,
            Any::class.java
        )
        println(listType)
    }
}
