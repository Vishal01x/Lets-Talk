//package com.exa.android.letstalk.presentation.Main.status
//import android.net.Uri
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import coil.compose.AsyncImage
//import com.exa.android.letstalk.utils.models.UserStatus
//import java.text.SimpleDateFormat
//
//
//@Composable
//fun StatusListScreen(
//    viewModel: StatusViewModel,
//    onEditClick: (UserStatus) -> Unit
//) {
//    val statuses by viewModel.statuses
//
//    LazyColumn {
//        items(statuses) { status ->
//            StatusItem(
//                status = status,
//                onEdit = { onEditClick(status) },
//                onDelete = { viewModel.deleteStatus(status.statusId) }
//            )
//        }
//    }
//}
//
//@Composable
//fun StatusItem(
//    status: UserStatus,
//    onEdit: () -> Unit,
//    onDelete: () -> Unit
//) {
//    Card(modifier = Modifier.padding(8.dp)) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            when(status.mediaType) {
//                "IMAGE" -> AsyncImage(
//                    model = status.mediaUrl,
//                    contentDescription = "Status image"
//                )
//                "VIDEO" -> {
//                    //open vidoe intent
//                }
//                "LINK" -> Text(status.mediaUrl, color = Color.Blue)
//            }
//            Text(status.content)
//            Text("Expires: ${SimpleDateFormat("dd MMM").format(status.endTime)}")
//
//            Row {
//                Button(onClick = onEdit) { Text("Edit") }
//                Spacer(Modifier.width(8.dp))
//                Button(onClick = onDelete) { Text("Delete") }
//            }
//        }
//    }
//}