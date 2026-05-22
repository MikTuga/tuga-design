package com.miktuga.design.settings

enum class UnitsSpeedValue { KMH, MPH }
enum class UnitsTempValue { CELSIUS, FAHRENHEIT }
enum class UnitsDistanceValue { METERS, FEET }

sealed class TugaSetting<T : Any>(val key: String, val default: T) {

    object UnitsSpeed : TugaSetting<UnitsSpeedValue>("units_speed", UnitsSpeedValue.KMH)
    object UnitsTemp : TugaSetting<UnitsTempValue>("units_temp", UnitsTempValue.CELSIUS)
    object UnitsDistance : TugaSetting<UnitsDistanceValue>("units_distance", UnitsDistanceValue.METERS)

    object UsbMountPath : TugaSetting<String>("usb_mount_path", "/storage/usbotg/usbotg-otg1")
    object MusicFolder : TugaSetting<String>("music_folder", "/storage/usbotg/usbotg-otg1/Music")
    object ReportsFolder : TugaSetting<String>("reports_folder", "/storage/usbotg/usbotg-otg1/Reports")

    object AutoUpdateCheck : TugaSetting<Boolean>("auto_update_check", true)

    companion object {
        val all: List<TugaSetting<*>> = listOf(
            UnitsSpeed,
            UnitsTemp,
            UnitsDistance,
            UsbMountPath,
            MusicFolder,
            ReportsFolder,
            AutoUpdateCheck
        )

        fun byKey(key: String): TugaSetting<*>? = all.firstOrNull { it.key == key }
    }
}
