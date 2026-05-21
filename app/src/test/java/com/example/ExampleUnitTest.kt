package com.example

import org.junit.Assert.*
import org.junit.Test
import okhttp3.OkHttpClient
import okhttp3.Request

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun fetchApiSchemas() {
    val client = OkHttpClient()
    
    val urls = listOf(
        "surahs" to "https://quran.yousefheiba.com/api/surahs",
        "ayah" to "https://quran.yousefheiba.com/api/ayah?number=1",
        "azkar" to "https://quran.yousefheiba.com/api/azkar",
        "duas" to "https://quran.yousefheiba.com/api/duas",
        "laylatAlQadr" to "https://quran.yousefheiba.com/api/laylatAlQadr",
        "reciters" to "https://quran.yousefheiba.com/api/reciters",
        "reciterAudio" to "https://quran.yousefheiba.com/api/reciterAudio?reciter_id=92"
    )
    
    for ((name, url) in urls) {
        try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val snippet = if (name == "laylatAlQadr") body else (if (body.length > 500) body.substring(0, 500) else body)
                println("API_CHECK_RESULT FOR $name: $snippet")
            }
        } catch (e: Exception) {
            println("API_CHECK_RESULT FOR $name error: ${e.message}")
        }
    }
  }
}
