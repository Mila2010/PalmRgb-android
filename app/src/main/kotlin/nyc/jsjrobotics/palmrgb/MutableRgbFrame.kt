package nyc.jsjrobotics.palmrgb

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey


@Entity(tableName = "rgbFrames")
class MutableRgbFrame(
        @PrimaryKey
        var frameId : Long = UNKNOWN_ID
) {
    companion object {
        val UNKNOWN_ID : Long = -1
        fun fromDatabaseString(value: String): MutableRgbFrame {
            return MutableRgbFrame(UNKNOWN_ID)
        }

        fun toDatabaseString(value: MutableRgbFrame): String {
            return ""
        }
    }
}