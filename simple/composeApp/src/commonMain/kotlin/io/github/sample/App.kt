package io.github.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kmmcrypto.KMMCrypto
import io.github.sample.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
internal fun App() = AppTheme {
    val scope = rememberCoroutineScope()
    val kmmCrypto = KMMCrypto()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            kmmCrypto.saveData(
                "test",
                "group",
                "eyJhbGciOiJSUzI1NiIsImtpZCI6Ijk0OTdCMzQxMTBEQkFFMzlFQzZGMUE0QzJDMEI0RjE2IiwidHlwIjoiYXQrand0In0.eyJpc3MiOiJodHRwczovL2RlbW8uZHVlbmRlc29mdHdhcmUuY29tIiwibmJmIjoxNzI2OTE2MDU2LCJpYXQiOjE3MjY5MTYwNTYsImV4cCI6MTcyNjkxOTY1NiwiYXVkIjoiYXBpIiwic2NvcGUiOlsib3BlbmlkIiwicHJvZmlsZSIsImVtYWlsIiwiYXBpIiwib2ZmbGluZV9hY2Nlc3MiXSwiYW1yIjpbImV4dGVybmFsIl0sImNsaWVudF9pZCI6ImludGVyYWN0aXZlLnB1YmxpYyIsInN1YiI6IjhCMkVFMjdDOUM2N0U0QkQwNUNDRUY0NzBGQzBEQkZFRkNBMTlBNEEyRDY3MTU4RjNCRDVEQTU1NjRCNDk0QjYiLCJhdXRoX3RpbWUiOjE3MjY5MTIyMzMsImlkcCI6Ikdvb2dsZSIsImVtYWlsIjoiZW5nLm1pY2hlbGxlLnJhb3VmQGdtYWlsLmNvbSIsIm5hbWUiOiJNaWNoZWxsZSBSYW91ZiIsInNpZCI6IkYzNDA2M0U0NjI5NkQ1MUZENTYzRjg1MzczQTkxMDBBIiwianRpIjoiQjU3RDU0OTQ3OUVERjM2Njk5RDFGMENGRDE4MDkxODMifQ.raVeI2SRT99TiRSfi-MeEJgA9JuBHzleKObD2c0cQUCR38vJFNgpF4-Xe6M2lTZnqhTmZEaWDtcf9cW-XiqI_cjXZmAW1WXTcT9fKsy3AEC9BitpdSXgq_uWU5kxlZDOOQ5su6pA6shIe2UZOb9Z8udh786bAZ7twkbD2nQwFK0XbUyG2YJfJ8vspNpk5oajC5CziEo1sihngapEwQZb6ffPb1RANc2Q2Gtz6EZm_x9BbnYXfqB8hX483yHHSHGTbIkcZ6bdAKAljUQ3ztEkThHCy-ohmHqQ-N4HtTYxgO4EEhpLQpMOk_8mWitgOiHX2oJ-TcMka2d0IspphjdPNA"
            )
        }) {
            Text("Save")
        }
        Spacer(Modifier.height(30.dp))

        Button(onClick = {
            scope.launch {
                val data = kmmCrypto.loadData("test", "group")
                println("data loaded $data")
            }

        }) {
            Text("Load")
        }
    }
}
