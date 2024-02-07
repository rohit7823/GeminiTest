package com.rohit.geminitest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rohit.geminitest.ui.theme.GeminiTestTheme
import java.util.Date
import java.util.UUID

class MainActivity : ComponentActivity() {


    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GeminiTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    ScreenContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_4, showSystemUi = true)
@Composable
fun GreetingPreview() {
    GeminiTestTheme {

        //ScreenContent()

    }
}


@Composable
fun ScreenContent(viewModel: MainViewModel) {
    val listState = rememberLazyListState()
    val messages = viewModel.messages.collectAsState()
    Scaffold(topBar = { AppBar() }) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .imePadding(),
                state = listState,
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(items = messages.value, key = { it.id }) { msg ->
                    MessageBubble(message = msg)
                }
            }
            OutlinedTextField(
                value = viewModel.userInput,
                onValueChange = { msg -> viewModel.userInput = msg },
                placeholder = {
                    Text(text = "Ask Gemini..", style = MaterialTheme.typography.headlineMedium)
                },
                textStyle = MaterialTheme.typography.headlineMedium,
                trailingIcon = {
                    IconButton(onClick = viewModel::ask) {
                        Icon(imageVector = Icons.Filled.Send, contentDescription = "Send")
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    LaunchedEffect(messages.value) {
        if (messages.value.isNotEmpty()) {
            listState.animateScrollToItem(messages.value.size - 1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar() {
    CenterAlignedTopAppBar(
        title = { Text(text = "Ask anything to Gemini AI") },
        modifier = Modifier.fillMaxWidth(),
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Blue, titleContentColor = Color.White
        ),
    )
}

@Composable
fun MessageBubble(message: Message) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = if (message.isSender) Arrangement.Start else Arrangement.End
    ) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(fraction = .5f),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.outlinedCardElevation(
                defaultElevation = 5.dp
            ),
            colors = CardDefaults.outlinedCardColors(
                containerColor = if (message.isSender) Color.Red else Color.Cyan
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(text = message.msg, color = if (message.isSender) Color.White else Color.Black)
            }
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!", modifier = modifier
    )
}


data class Message(
    val id: String = UUID.randomUUID().toString(),
    val msg: String,
    val sendingState: Boolean = false,
    val timeStamp: String,
    val isSender: Boolean = false
)

