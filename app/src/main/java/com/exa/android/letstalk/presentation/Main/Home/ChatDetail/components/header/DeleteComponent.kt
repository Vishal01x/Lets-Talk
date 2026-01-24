package com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.header

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.exa.android.letstalk.domain.Message

@Composable
fun DeleteMessageDialog(
    canDeleteForEveryone: Boolean,
    hasBeenDeleted: Boolean,
    onDelete: (String) -> Unit,
    onCancel: () -> Unit
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancel,
        backgroundColor = MaterialTheme.colors.background,
        title = { Text(text = "Delete message?", color = MaterialTheme.colors.onBackground) },
        text = {
            Column {
                if (canDeleteForEveryone && !hasBeenDeleted) {
                    Text(text = "You can delete messages for everyone or just for yourself.")
                    Spacer(modifier = Modifier.height(16.dp))
                }


                if (canDeleteForEveryone && !hasBeenDeleted) {
                    val options = listOf("Delete for Me", "Delete for Everyone")
                    options.forEach { option ->
                        SelectableOption(
                            text = option,
                            isSelected = selectedOption == option,
                            onSelect = { selectedOption = option }
                        )
                    }
                }
            }
        },
        confirmButton = {
            CustomButton(
                text = "Delete",
                isPrimary = true,
                enabled = selectedOption != null || !canDeleteForEveryone || hasBeenDeleted,
                onClick = {
                    val deleteOption = selectedOption ?: "Delete for Me"
                    onDelete(deleteOption)
                }
            )
        },
        dismissButton = {
            CustomButton(text = "Cancel", isPrimary = false, onClick = onCancel)
        }
    )
}

@Composable
fun SelectableOption(text: String, isSelected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelect
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = onSelect)
        Text(text = text, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun CustomButton(text: String, isPrimary: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    if (isPrimary) {
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (enabled) Color.Red else Color.Gray,
                contentColor = MaterialTheme.colors.onSecondary
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(text = text)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, MaterialTheme.colors.secondary)
        ) {
            Text(text = text, color = MaterialTheme.colors.secondary)
        }
    }
}
