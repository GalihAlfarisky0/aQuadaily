package com.aquadaily.app.core.notification

import android.content.Context
import android.net.Uri
import com.aquadaily.app.R

/**
 * Single source of truth for notification sounds bundled with AquaDaily.
 *
 * Sounds are packaged in app/src/main/res/raw and therefore do not depend
 * on the device's system ringtone library.
 */
object NotificationSoundCatalog {

    data class Sound(
        val id: String,
        val name: String,
        val resourceId: Int,
    )

    const val DEFAULT_ID = "default"
    const val AQUADAILY_ID = "aquadaily_reminder"
    const val BRAND_NEW_DAY_ID = "brand_new_day"

    private val bundledSounds = listOf(
        Sound(
            id = AQUADAILY_ID,
            name = "AquaDaily Reminder",
            resourceId = R.raw.aquadaily_reminder,
        ),
        Sound(
            id = BRAND_NEW_DAY_ID,
            name = "Brand New Day",
            resourceId = R.raw.brand_new_day,
        ),
    )

    fun getBundledSounds(): List<Sound> = bundledSounds

    fun findById(id: String?): Sound? =
        bundledSounds.firstOrNull { it.id == id }

    fun toUri(context: Context, sound: Sound): Uri =
        Uri.parse("android.resource://${context.packageName}/raw/${sound.id}")
}
