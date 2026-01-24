package com.exa.android.letstalk.presentation.Main.scheduledMessages

// Jetpack Compose core
import android.app.TimePickerDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// Hilt for Compose ViewModel
import androidx.hilt.navigation.compose.hiltViewModel

// Coil for image loading
import coil.compose.AsyncImage
import com.exa.android.letstalk.R
import com.exa.android.letstalk.data.local.room.MessageListItem
import com.exa.android.letstalk.data.local.room.ScheduledMessageViewModel
import com.exa.android.letstalk.data.local.room.ScheduledMessageEntity
import com.exa.android.letstalk.presentation.Main.Home.components.MessageSchedulerDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScheduledMessagesScreen(
    viewModel: ScheduledMessageViewModel = hiltViewModel(),
    onEditClick: (ScheduledMessageEntity) -> Unit
) {
    val scheduledMessages by viewModel.scheduledMessages.collectAsState()
    val sentMessages by viewModel.sentMessages.collectAsState()
    
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    val titles = listOf("Scheduled", "Sent")

    var selectedMessage by remember { mutableStateOf<ScheduledMessageEntity?>(null) }

    // Edit Dialog (Only for Scheduled messages generally)
    selectedMessage?.let { message ->
        EditMessageDialog(
            message = message,
            onDismiss = { selectedMessage = null },
            onConfirm = { updatedMessage ->
                viewModel.updateScheduledMessage(updatedMessage, message)
                selectedMessage = null
            }
        )
    }

    Scaffold(
        topBar = { MessagesHeader() }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                titles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        },
                        text = { Text(title, style = MaterialTheme.typography.titleMedium) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                if (page == 0) {
                    ScheduledMessageList(
                        messages = scheduledMessages,
                        onDelete = { viewModel.deleteMessage(it) },
                        onEdit = { selectedMessage = it },
                        isHistory = false
                    )
                } else {
                    ScheduledMessageList(
                        messages = sentMessages,
                        onDelete = { viewModel.deleteMessage(it) }, // Allow deleting history
                        onEdit = { /* No edit for sent messages */ },
                        isHistory = true
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduledMessageList(
    messages: List<MessageListItem>,
    onDelete: (ScheduledMessageEntity) -> Unit,
    onEdit: (ScheduledMessageEntity) -> Unit,
    isHistory: Boolean
) {
    if (messages.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if(isHistory)Icons.Default.Check else Icons.Default.Emergency,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if(isHistory) "No sent messages" else "No scheduled messages",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            items(messages) { item ->
                when (item) {
                    is MessageListItem.DateHeader -> DateSeparator(item.date)
                    is MessageListItem.MessageItem -> MessageCard(
                        message = item.message,
                        onDelete = onDelete,
                        onEdit = if (!isHistory) onEdit else { {} }, // Disable edit for history
                        showEditArg = !isHistory 
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessagesHeader() {
    TopAppBar(
        title = { Text("Scheduled Messages", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onTertiary) },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
        modifier = Modifier.shadow(4.dp)
    )
}

@Composable
private fun MessageCard(
    message: ScheduledMessageEntity,
    onDelete: (ScheduledMessageEntity) -> Unit,
    onEdit: (ScheduledMessageEntity) -> Unit,
    showEditArg: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Header Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = message.profileImageUri,
                    contentDescription = "Profile image",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Text(
                        text = message.recipientName ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

//                    Text(
//                        text = "Scheduled for ${formatFullDate(message.scheduledTime)}",
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
                }

                if (showEditArg) {
                    IconButton(
                        onClick = { onEdit(message) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Message Content
            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 8.dp),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Status and Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary.copy(.2f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        text = "Scheduled • ${formatTime(message.scheduledTime)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = "Time",
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = { onDelete(message) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun EditMessageDialog(
    message: ScheduledMessageEntity,
    onDismiss: () -> Unit,
    onConfirm: (ScheduledMessageEntity) -> Unit
) {
    var editedMessage by remember { mutableStateOf(message.message) }
    var editedTime by remember { mutableStateOf(message.scheduledTime) }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showTimePicker) {
        MessageSchedulerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { scheduledTime, _ ->
                editedTime = scheduledTime
                showTimePicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Scheduled Message") },
        text = {
            Column {
                OutlinedTextField(
                    value = editedMessage,
                    onValueChange = { editedMessage = it },
                    label = { Text("Message") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(
                            painter = painterResource(R.drawable.alarm_light),
                            contentDescription = "Pick Time",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "Scheduled for: ${formatFullDate(editedTime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        message.copy(
                            message = editedMessage,
                            scheduledTime = editedTime
                        )
                    )
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Date Utils
fun formatFullDate(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

@Composable
private fun DateSeparator(date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Divider
        Divider(
            modifier = Modifier
                .weight(1f)
                .height(1.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.width(8.dp)) // Space between divider and date

        // Dot + Date + Dot
        DotDivider(date)

        Spacer(modifier = Modifier.width(8.dp)) // Space between date and divider

        // Right Divider
        Divider(
            modifier = Modifier
                .weight(1f)
                .height(1.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
    }
}


@Composable
private fun DotDivider(date: String) {


        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Left dot
            Dot()

            Spacer(modifier = Modifier.width(8.dp)) // Space between dot and date text

            // Date Text in the middle
            Text(
                text = date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(8.dp)) // Space between date text and dot

            // Right dot
            Dot()
        }
}

@Composable
private fun Dot() {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface)
    )
}
