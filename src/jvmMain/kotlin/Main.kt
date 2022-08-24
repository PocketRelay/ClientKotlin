// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
@file:Suppress("FunctionName")
@file:JvmName("Main")

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.jacobtread.relay.app.AppState
import com.jacobtread.relay.app.RedirectorServer
import org.jetbrains.skia.Image
import java.security.Security

object Images {

    val LOGO = getLogo()

    fun getLogo(): ImageBitmap {
        val bytes = Images::class.java.getResourceAsStream("/logo.png")
            ?.readAllBytes()
        requireNotNull(bytes)
        return Image.makeFromEncoded(bytes).toComposeImageBitmap()
    }

}

@Composable
@Preview
fun App() {
    MaterialTheme(
        colors = darkColors(
            primary = Color(0xFFFFFFFF)
        ),
    ) {
        Surface(
            color = MaterialTheme.colors.background,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(30.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Image(Images.LOGO, contentDescription = "")

                when (RedirectorServer.state) {
                    AppState.INITIAL -> Connect()
                    AppState.STARTING -> Text("Starting")
                    AppState.STARTED -> Started()
                    AppState.STOPPING -> Text("Stopping")
                }

            }
        }
    }
}
@Composable
fun Started() {
    Text("Running. Target:")
    Text("Host: ${RedirectorServer.targetAddress}")
    Text("Port: ${RedirectorServer.targetPort}")
    Button(onClick = {RedirectorServer.stop()}) {
        Text("Stop")
    }
}


@Composable
fun Connect() {
    var address by remember { mutableStateOf("127.0.0.1") }
    var port by remember { mutableStateOf("14219") }
    Text("Enter the remote address and port of the server you are trying to connect to", fontSize = 11.sp, color = Color.LightGray)

    Text("Address")
    TextField(
        value = address,
        onValueChange = { address = it },
        modifier = Modifier.fillMaxWidth()
    )

    Text("Port")
    TextField(
        value = port,
        onValueChange = { port = it },
        modifier = Modifier.fillMaxWidth()
    )

    Button(
        onClick = {
            RedirectorServer.start(address, port.toInt())
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Start", fontWeight = FontWeight.Bold)
    }
}


fun main() = application {
    Security.setProperty("jdk.tls.disabledAlgorithms", "")
    Window(
        onCloseRequest = ::exitApplication,
        resizable = false,
        state = WindowState(
            size = DpSize(400.dp, 500.dp)
        ),

        ) {
        App()
    }
}
