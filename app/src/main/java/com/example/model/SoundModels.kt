package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class SoundType(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val descriptionAr: String,
    val accentColor: Color,
    val defaultVolume: Float
) {
    RAIN(
        id = "rain",
        nameAr = "مطر على النافذة",
        nameEn = "Window Rain",
        descriptionAr = "قطرات مطر دافئة ومستمرة",
        accentColor = AccentRain,
        defaultVolume = 0.65f
    ),
    CAMPFIRE(
        id = "campfire",
        nameAr = "موقد نار هادئ",
        nameEn = "Campfire",
        descriptionAr = "فرقعة خشب الموقد الدافئة",
        accentColor = AccentFire,
        defaultVolume = 0.50f
    ),
    CRICKETS(
        id = "crickets",
        nameAr = "صرار الليل وطبيعة",
        nameEn = "Night Crickets",
        descriptionAr = "هدوء الطبيعة الليلية الريفية",
        accentColor = AccentCrickets,
        defaultVolume = 0.40f
    ),
    OCEAN(
        id = "ocean",
        nameAr = "أمواج المحيط",
        nameEn = "Ocean Waves",
        descriptionAr = "أمواج متلاطمة ببطء واسترخاء",
        accentColor = AccentOcean,
        defaultVolume = 0.55f
    ),
    WIND(
        id = "wind",
        nameAr = "نسيم الرياح",
        nameEn = "Gentle Wind",
        descriptionAr = "رياح لطيفة بين أوراق الشجر",
        accentColor = AccentWind,
        defaultVolume = 0.45f
    ),
    CAFE(
        id = "cafe",
        nameAr = "أجواء مقهى",
        nameEn = "Cozy Cafe",
        descriptionAr = "أحاديث خافتة ورنين فناجين قهوة",
        accentColor = AccentCafe,
        defaultVolume = 0.45f
    ),
    LOFI_CHORDS(
        id = "lofi_chords",
        nameAr = "نغمات لو-فاي هادئة",
        nameEn = "Lo-Fi Chords",
        descriptionAr = "تسلسلات نغمات بيانو إلكتروني كلاسيكية",
        accentColor = AccentLofi,
        defaultVolume = 0.70f
    ),
    VINYL(
        id = "vinyl",
        nameAr = "خشخشة الفينيل",
        nameEn = "Vinyl Crackle",
        descriptionAr = "خشخشة أسطوانات الفينيل الدافئة",
        accentColor = AccentVinyl,
        defaultVolume = 0.45f
    ),
    BROWN_NOISE(
        id = "brown_noise",
        nameAr = "ضوضاء بنية (ADHD)",
        nameEn = "Brown Noise",
        descriptionAr = "ترددات عميقة لحجب التشتت والتركيز التام",
        accentColor = AccentBrown,
        defaultVolume = 0.60f
    ),
    ALPHA_WAVES(
        id = "alpha_waves",
        nameAr = "موجات ألفا (10Hz)",
        nameEn = "Alpha Waves",
        descriptionAr = "موجات دماغية ثنائية التردد للتأمل والتفكير",
        accentColor = AccentAlpha,
        defaultVolume = 0.50f
    )
}

data class SoundChannelState(
    val type: SoundType,
    val isEnabled: Boolean = false,
    val volume: Float = 0.5f
)

data class PresetMix(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val descriptionAr: String,
    val iconName: String,
    val volumes: Map<SoundType, Float>
)

val CURATED_PRESETS = listOf(
    PresetMix(
        id = "cozy_study",
        titleAr = "دراسة هادئة",
        titleEn = "Cozy Study",
        descriptionAr = "مطر ناعم مع نغمات بيانو وفينيل",
        iconName = "menu_book",
        volumes = mapOf(
            SoundType.RAIN to 0.70f,
            SoundType.LOFI_CHORDS to 0.65f,
            SoundType.VINYL to 0.40f,
            SoundType.BROWN_NOISE to 0.35f
        )
    ),
    PresetMix(
        id = "midnight_cafe",
        titleAr = "مقهى منتصف الليل",
        titleEn = "Midnight Cafe",
        descriptionAr = "أجواء قهوة دافئة مع موسيقى خافتة ومطر",
        iconName = "local_cafe",
        volumes = mapOf(
            SoundType.CAFE to 0.65f,
            SoundType.RAIN to 0.50f,
            SoundType.LOFI_CHORDS to 0.60f,
            SoundType.VINYL to 0.45f
        )
    ),
    PresetMix(
        id = "deep_focus",
        titleAr = "تركيز عميق وعزل",
        titleEn = "Deep Focus Flow",
        descriptionAr = "ضوضاء بنية مع موجات ألفا لدراسة بلا تشتت",
        iconName = "psychology",
        volumes = mapOf(
            SoundType.BROWN_NOISE to 0.75f,
            SoundType.ALPHA_WAVES to 0.60f,
            SoundType.WIND to 0.30f
        )
    ),
    PresetMix(
        id = "campfire_night",
        titleAr = "موقد التخييم",
        titleEn = "Campfire Night",
        descriptionAr = "فرقعة النار وصرار الليل ونسيم الهواء",
        iconName = "fireplace",
        volumes = mapOf(
            SoundType.CAMPFIRE to 0.70f,
            SoundType.CRICKETS to 0.55f,
            SoundType.WIND to 0.35f
        )
    ),
    PresetMix(
        id = "ocean_calm",
        titleAr = "استرخاء الشاطئ",
        titleEn = "Ocean Breeze",
        descriptionAr = "أمواج المحيط وموجات ألفا الهادئة",
        iconName = "waves",
        volumes = mapOf(
            SoundType.OCEAN to 0.75f,
            SoundType.ALPHA_WAVES to 0.45f,
            SoundType.WIND to 0.35f
        )
    ),
    PresetMix(
        id = "evening_reading",
        titleAr = "قراءة مسائية",
        titleEn = "Evening Reading",
        descriptionAr = "مطر خفيف وموقد حطب وخشخشة أسطوانة",
        iconName = "auto_stories",
        volumes = mapOf(
            SoundType.RAIN to 0.55f,
            SoundType.CAMPFIRE to 0.50f,
            SoundType.VINYL to 0.40f
        )
    )
)

enum class SceneType(
    val id: String,
    val titleAr: String,
    val titleEn: String
) {
    COZY_ROOM("room", "غرفة المذاكرة الدافئة", "Cozy Study Room"),
    VINYL_RECORD("vinyl", "مشغل الفينيل الكلاسيكي", "Vintage Turntable"),
    CAMPFIRE_NIGHT("campfire", "مخيم النجوم والنار", "Starry Campfire"),
    ZEN_MINIMAL("zen", "أفق زن البسيط", "Zen Minimalist")
}

enum class TimerMode(
    val id: String,
    val titleAr: String,
    val defaultMinutes: Int,
    val isBreak: Boolean = false
) {
    POMODORO("pomodoro", "بومودورو", 25),
    SHORT_BREAK("short_break", "استراحة قصيرة", 5, isBreak = true),
    LONG_BREAK("long_break", "استراحة طويلة", 15, isBreak = true),
    DEEP_WORK("deep_work", "جلسة تركيز طويلة", 50),
    SLEEP_30("sleep_30", "مؤقت نوم (30 د)", 30),
    SLEEP_60("sleep_60", "مؤقت نوم (60 د)", 60)
}
