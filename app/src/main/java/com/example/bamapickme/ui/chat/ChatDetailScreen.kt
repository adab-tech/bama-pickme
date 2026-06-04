package com.example.bamapickme.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bamapickme.data.DataRepository
import com.example.bamapickme.data.Message

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
  threadId: String,
  threadTitle: String,
  repository: DataRepository,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  var messages by remember { mutableStateOf(repository.getMessages(threadId)) }
  val activeUser by repository.activeUser.collectAsStateWithLifecycle(initialValue = null)
  var messageText by remember { mutableStateOf("") }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(threadTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF9E1B32))
            Text("Secure handoff coordination channel", fontSize = 11.sp, color = Color(0xFF6d5b5d))
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF9E1B32))
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFAFAF5))
      )
    },
    containerColor = Color(0xFFFAFAF5),
    modifier = modifier
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(messages) { msg ->
          val isMe = msg.authorId == activeUser?.id
          MessageBubble(message = msg, isMe = isMe)
        }
      }

      Card(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .padding(16.dp)
            .navigationBarsPadding()
            .imePadding(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextField(
            value = messageText,
            onValueChange = { messageText = it },
            placeholder = { Text("Confirm pickup, ask questions...") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
              focusedContainerColor = Color(0x089E1B32),
              unfocusedContainerColor = Color(0x089E1B32),
              focusedIndicatorColor = Color.Transparent,
              unfocusedIndicatorColor = Color.Transparent
            )
          )

          Spacer(modifier = Modifier.width(8.dp))

          IconButton(
            onClick = {
              if (messageText.trim().isNotEmpty()) {
                val (success, _) = repository.sendMessage(threadId, messageText.trim())
                if (success) {
                  messageText = ""
                  messages = repository.getMessages(threadId) // refresh
                }
              }
            },
            colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF9E1B32))
          ) {
            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
          }
        }
      }
    }
  }
}

@Composable
fun MessageBubble(message: Message, isMe: Boolean) {
  Box(
    modifier = Modifier.fillMaxWidth(),
    contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
  ) {
    Column(
      horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
      modifier = Modifier.widthIn(max = 280.dp)
    ) {
      Text(
        text = message.authorName.substringBefore(","),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF6d5b5d),
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
      )

      Box(
        modifier = Modifier
          .background(
            color = if (isMe) Color(0xFF9E1B32) else Color(0x129E1B32),
            shape = RoundedCornerShape(
              topStart = 16.dp,
              topEnd = 16.dp,
              bottomStart = if (isMe) 16.dp else 4.dp,
              bottomEnd = if (isMe) 4.dp else 16.dp
            )
          )
          .padding(horizontal = 16.dp, vertical = 10.dp)
      ) {
        Text(
          text = message.body,
          color = if (isMe) Color.White else Color(0xFF22181a),
          fontSize = 14.sp
        )
      }
    }
  }
}
