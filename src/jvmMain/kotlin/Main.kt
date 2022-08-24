// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
@file:Suppress("FunctionName")
@file:JvmName("Main")

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jacobtread.relay.app.RedirectorServer

@Composable
@Preview
fun App() {
    var address by remember { mutableStateOf("127.0.0.1") }
    var port by remember { mutableStateOf("14219") }

    MaterialTheme(
    ) {
        Text("Address")
        TextField(value = address, onValueChange = { address = it })

        Text("Port")
        TextField(value = port, onValueChange = { port = it })

        Button(onClick = {
            RedirectorServer.targetAddress = address
            RedirectorServer.targetPort = port.toInt()

        }) {
            Text("Set")
        }
    }
}

fun main() = application {
    RedirectorServer.start()
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
