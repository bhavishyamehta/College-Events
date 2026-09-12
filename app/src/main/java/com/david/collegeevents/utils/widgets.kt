package com.david.collegeevents.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun generateColorFromName(name: String): Color {
    val colors = listOf(
        Color(0xFFEF5350),
        Color(0xFFAB47BC),
        Color(0xFF5C6BC0),
        Color(0xFF29B6F6),
        Color(0xFF66BB6A),
        Color(0xFFFFCA28),
        Color(0xFFFF7043)
    )

    val hash = name.hashCode()
    return colors[kotlin.math.abs(hash) % colors.size]
}

@Composable
fun NameAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val initials = remember(name) {
        val words = name
            .trim()
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }

        when {
            words.isEmpty() -> ""
            words.size == 1 -> {
                words[0].take(2).uppercase()
            }

            else -> {
                "${words.first().first()}${words.last().first()}".uppercase()
            }
        }
    }

    val backgroundColor = remember(name) {
        generateColorFromName(name)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontSize = (size.value / 2.3).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

data class DateTimeValue(
    val dateMillis: Long? = null,
    val hour: Int? = null,
    val minute: Int? = null
) {
    val isComplete get() = dateMillis != null && hour != null && minute != null

    fun toIso(): String? {
        if (!isComplete) return null
        val cal = Calendar.getInstance().apply {
            timeInMillis = dateMillis!!
            set(Calendar.HOUR_OF_DAY, hour!!)
            set(Calendar.MINUTE, minute!!)
            set(Calendar.SECOND, 0)
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return sdf.format(cal.time)
    }

    fun display(): String {
        if (dateMillis == null) return ""
        val dateSdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val dateStr = dateSdf.format(Date(dateMillis))
        if (hour == null || minute == null) return dateStr
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        val timeSdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return "$dateStr • ${timeSdf.format(cal.time)}"
    }

    companion object {
        fun fromIso(iso: String?): DateTimeValue {
            if (iso.isNullOrBlank()) return DateTimeValue()

            // Backend se aane wale possible formats: with seconds ya without seconds
            val patterns = arrayOf(
                "yyyy-MM-dd'T'HH:mm",
                "yyyy-MM-dd'T'HH:mm:ss"
            )

            for (pattern in patterns) {
                try {
                    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                    sdf.isLenient = false
                    val date = sdf.parse(iso)
                    if (date != null) {
                        val cal = Calendar.getInstance().apply { time = date }
                        return DateTimeValue(
                            dateMillis = cal.timeInMillis,
                            hour = cal.get(Calendar.HOUR_OF_DAY),
                            minute = cal.get(Calendar.MINUTE)
                        )
                    }
                } catch (_: Exception) {
                    // Agla pattern try karega
                }
            }

            return DateTimeValue()
        }
    }
}