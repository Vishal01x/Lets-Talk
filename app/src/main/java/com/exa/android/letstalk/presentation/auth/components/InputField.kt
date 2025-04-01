package com.exa.android.letstalk.presentation.auth.components

import android.util.Patterns
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp

@Composable
fun InputField(
    label: String,
    icon: ImageVector,
    value: String,
    error: String = "",
    onValueChange: (String) -> Unit,
    onImeAction:  (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        isError = error.isNotEmpty(), // Highlights field in error state
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = "Phone",
                tint = MaterialTheme.colorScheme.secondary // Matches theme
            )
        },
        label = {
            Text(
                label,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .5f)
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = CircleShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant, // Border color when focused
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), // Border color when not focused
            cursorColor = MaterialTheme.colorScheme.primary, // Cursor color
            errorBorderColor = MaterialTheme.colorScheme.error, // Red border when error
            errorLabelColor = MaterialTheme.colorScheme.error
        ),
        visualTransformation = if(label == "Password") PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if(label == "Phone") KeyboardType.Phone else if(label == "Password") KeyboardType.Password else KeyboardType.Email, // Numeric keyboard), // Numeric keyboard
            imeAction = ImeAction.Next // Moves to next input field
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                ImeAction.Next
            }
        ),

//        supportingText = {
//            if (error.isNotEmpty()) {
//                Text(
//                    text = error,
//                    color = MaterialTheme.colorScheme.error,
//                    fontSize = 12.sp
//                )
//            }
//        }
    )
}

fun isValidEmailAndPassword(email: String, password: String, onResult: (String) -> Unit) {
    var errorMessage = ""
    if (email.isBlank()) {
        errorMessage = "Please enter an email."
    } else if (password.length < 8) {
        errorMessage = "Please enter least 8 characters password"
    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        errorMessage = "Enter correct email"
    }

    onResult(errorMessage)
}
