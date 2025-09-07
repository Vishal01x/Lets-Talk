//package com.exa.android.letstalk.presentation.Main.status
//import android.net.Uri
//import androidx.compose.foundation.layout.*
//import androidx.compose.material.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.text.input.TextFieldValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.style.TextAlign
//import androidx.lifecycle.viewmodel.compose.viewModel
//
//
//@Composable
//fun StatusCreationScreen(
//    viewModel: StatusViewModel,
//    onComplete: () -> Unit
//) {
//    var content by remember { mutableStateOf("") }
//    var selectedMedia by remember { mutableStateOf<Uri?>(null) }
//    var mediaType by remember { mutableStateOf("TEXT") }
//    var selectedDays by remember { mutableStateOf(1) }
//
//    Column(modifier = Modifier.padding(16.dp)) {
//        TextField(
//            value = content,
//            onValueChange = { content = it },
//            label = { Text("Status text") },
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        // Media Picker Section
//        Row {
//            Button(onClick = { /* Handle image pick */ mediaType = "IMAGE" }) {
//                Text("Add Image")
//            }
//            Button(onClick = { /* Handle video pick */ mediaType = "VIDEO" }) {
//                Text("Add Video")
//            }
//            Button(onClick = { mediaType = "LINK" }) {
//                Text("Add Link")
//            }
//        }
//
//        // Days Selector
//        var showDaysDialog by remember { mutableStateOf(false) }
//        Text("Duration: $selectedDays days")
//        Button(onClick = { showDaysDialog = true }) {
//            Text("Select Duration")
//        }
//
//        if (showDaysDialog) {
//            AlertDialog(
//                onDismissRequest = { showDaysDialog = false },
//                buttons = {
//                    Button(onClick = { showDaysDialog = false }) {
//                        Text("Set")
//                    }
//                },
//                text = {
//                    Column {
//                        Text("Select days (1-7)")
//                        Slider(
//                            value = selectedDays.toFloat(),
//                            onValueChange = { selectedDays = it.toInt() },
//                            valueRange = 1f..7f,
//                            steps = 6
//                        )
//                    }
//                }
//            )
//        }
//
//        Button(
//            onClick = {
//                viewModel.createUpdateStatus(
//                    content = content,
//                    mediaUrl = selectedMedia?.toString() ?: "",
//                    mediaType = mediaType,
//                    days = selectedDays
//                )
//                onComplete()
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(if (viewModel.selectedStatus.value == null) "Create Status" else "Update Status")
//        }
//    }
//}