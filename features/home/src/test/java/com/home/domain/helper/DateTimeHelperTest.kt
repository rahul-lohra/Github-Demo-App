package com.home.domain.helper

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.*

class DateTimeHelperTest {

    private val dateTimeHelper = spyk(DateTimeHelper())

    @Test
    fun testToHumanReadableTime() {
        val dateString = "2022-10-10"
        val date: Date = mockk()
        val returnedDate = "test"
        val simpleDateFormat: SimpleDateFormat = mockk()
        every { dateTimeHelper.getDateFormatter() } returns simpleDateFormat
        every { simpleDateFormat.parse(dateString) } returns date
        every { simpleDateFormat.format(date) } returns returnedDate

        val formattedText = dateTimeHelper.toHumanReadableTime(dateString)
        verify {
            simpleDateFormat.parse(dateString)
            simpleDateFormat.format(date)
        }
        assert(formattedText == returnedDate)
    }

    @Test
    fun testDateFormatter() {
        val dateFormatter = dateTimeHelper.getDateFormatter()
        val fieldPattern: Field = SimpleDateFormat::class.java.getDeclaredField("pattern")
        val fieldLocale: Field = SimpleDateFormat::class.java.getDeclaredField("locale")
        fieldPattern.isAccessible = true
        fieldLocale.isAccessible = true
        assert(fieldPattern.get(dateFormatter) == "yyyy-MM-dd")
        assert(fieldLocale.get(dateFormatter) == Locale.getDefault())
    }
}