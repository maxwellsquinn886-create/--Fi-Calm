package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.CURATED_PRESETS
import com.example.model.SoundType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Lo-Fi Calm", appName)
  }

  @Test
  fun `sound types and presets are configured`() {
    assertTrue(SoundType.values().isNotEmpty())
    assertTrue(CURATED_PRESETS.isNotEmpty())
    val firstPreset = CURATED_PRESETS.first()
    assertEquals("cozy_study", firstPreset.id)
  }
}
