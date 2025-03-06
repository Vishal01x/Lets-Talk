package com.exa.android.letstalk.presentation.Main.Home.components

import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.exa.android.letstalk.R
import com.exa.android.letstalk.utils.models.ScheduleType
import com.maxkeppeker.sheets.core.models.base.rememberSheetState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.calendar.models.CalendarTimeline
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageSchedulerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long, ScheduleType) -> Unit
) {

    val context = LocalContext.current
    val selectedDateTime = rememberSaveable { mutableLongStateOf(Calendar.getInstance().timeInMillis) }
    val selectedOption = rememberSaveable { mutableStateOf(ScheduleType.ONCE) }

    val formattedDateTime = remember {
        derivedStateOf {
            DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm", Locale.getDefault())
                .format(Instant.ofEpochMilli(selectedDateTime.longValue).atZone(ZoneId.systemDefault()))
        }
    }

    val calendarState = rememberSheetState()
    val timePicker = createTimePicker(context, selectedDateTime)

    CalendarDialog(
        state = calendarState,
        config = CalendarConfig(
            monthSelection = true,
            disabledTimeline = CalendarTimeline.PAST
        ),
        selection = CalendarSelection.Date { date ->
            selectedDateTime.longValue = date.toEpochDay() * 86400000
            timePicker.show()
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        backgroundColor = MaterialTheme.colors.background,
        title = { Text("Schedule Message", color = Color.Black) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CustomButton("Set Time", onClick = { calendarState.show() }, isOutlined = false)

                Text(text = "Selected: ${formattedDateTime.value}", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                        SelectableOption("ALWAYS", selectedOption.value) { selectedOption.value = ScheduleType.ALWAYS }
                    Spacer(modifier = Modifier.width(8.dp))
                    SelectableOption("ONCE", selectedOption.value) { selectedOption.value = ScheduleType.NONE }
                }
            }
        },
        confirmButton = {
            CustomButton("Confirm", onClick = {
                onConfirm(selectedDateTime.longValue, selectedOption.value)
                onDismiss()
            }, isOutlined = false)
        },
        dismissButton = {
            CustomButton("Cancel", onClick = onDismiss, isOutlined = true)
        }
    )
}

@Composable
fun SelectableOption(text: String, selected: ScheduleType, onSelect: () -> Unit) {
    val selectedType = ScheduleType.valueOf(text)
    CustomButton(
        text = text,
        onClick = onSelect,
        isOutlined = selected != selectedType,
        shape = CircleShape
    )
}

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    isOutlined: Boolean,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    if (isOutlined) {
        OutlinedButton(
            onClick = onClick,
            border = BorderStroke(1.dp, Color(0xFF007AFF)),
            shape = shape,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
        ) {
            Text(text, color = Color.Black)
        }
    } else {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF007AFF)),
            shape = shape
        ) {
            Text(text, color = Color.White)
        }
    }
}

fun createTimePicker(context: Context, selectedDateTime: MutableLongState): TimePickerDialog {
    val calendar = Calendar.getInstance()
    return TimePickerDialog(
        context,
        { _, hour, minute ->
            val updatedCalendar = Calendar.getInstance().apply {
                timeInMillis = selectedDateTime.value
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }
            selectedDateTime.longValue = updatedCalendar.timeInMillis
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    ).apply {
        setOnShowListener {
            val dialog = this as TimePickerDialog
            val okColor = ContextCompat.getColor(context, R.color.blue)
            val cancelColor = ContextCompat.getColor(context, R.color.blue)

            dialog.getButton(DialogInterface.BUTTON_POSITIVE)?.setTextColor(okColor)  // OK Button
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(cancelColor)
        }
    }
}

//@Preview
//@Composable
//private fun preview() {
//    MessageSchedulerDialog(
//        onDismiss = TODO()
//    ) { }
//}
