package com.exa.android.letstalk.presentation.Main.Home.components

import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageSchedulerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long, ScheduleType) -> Unit
) {
    val context = LocalContext.current
    val selectedDateTime = rememberSaveable { mutableLongStateOf(Calendar.getInstance().timeInMillis) }
    var selectedOption = rememberSaveable { mutableStateOf(ScheduleType.ONCE) }

    val formattedDateTime = remember {
        derivedStateOf {
            DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a", Locale.getDefault())
                .format(Instant.ofEpochMilli(selectedDateTime.longValue).atZone(ZoneId.systemDefault()))
        }
    }

    val calendarState = rememberSheetState()
    val timePicker = createTimePicker(context, selectedDateTime)

    CalendarDialog(
        state = calendarState,
        config = CalendarConfig(
            monthSelection = true
        ),
        selection = CalendarSelection.Date { date ->
            val selectedMillis = date.toEpochDay() * 86400000
            val todayStartMillis = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            if (selectedMillis < todayStartMillis) {
                Toast.makeText(context, "Past dates can't be selected", Toast.LENGTH_SHORT).show()
            } else {
                selectedDateTime.longValue = selectedMillis
                timePicker.show()
            }
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Schedule Message",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        iconContentColor = MaterialTheme.colorScheme.onBackground,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CustomButton("Set Time", onClick = { calendarState.show() }, isOutlined = false)

                Text(
                    text = "Selected: ${formattedDateTime.value}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    SelectableOption("ALWAYS", selectedOption.value) {
                        selectedOption.value = ScheduleType.ALWAYS
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    SelectableOption("ONCE", selectedOption.value) {
                        selectedOption.value = ScheduleType.ONCE
                    }
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
    val isSelected = selected.name == text
    CustomButton(
        text = text,
        onClick = onSelect,
        isOutlined = !isSelected,
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
            shape = shape,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text)
        }
    } else {
        Button(
            onClick = onClick,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(text)
        }
    }
}

fun createTimePicker(context: Context, selectedDateTime: MutableLongState): TimePickerDialog {
    val calendar = Calendar.getInstance()
    return TimePickerDialog(
        context,
        { _, hour, minute ->
            val updatedCalendar = Calendar.getInstance().apply {
                timeInMillis = selectedDateTime.longValue
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
            val okColor = ContextCompat.getColor(context, R.color.yellow)
            val cancelColor = ContextCompat.getColor(context, R.color.yellow)

            getButton(DialogInterface.BUTTON_POSITIVE)?.setTextColor(okColor)
            getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(cancelColor)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessageSchedulerDialogPreview() {
    MaterialTheme {
        MessageSchedulerDialog(
            onDismiss = {},
            onConfirm = { _, _ -> }
        )
    }
}
