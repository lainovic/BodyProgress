package care.mare.bodyprogress

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import care.mare.bodyprogress.ui.theme.BodyProgressTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BodyProgressTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    BodySnapshotScreen()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BodyProgressTheme {
        BodySnapshotScreen()
    }
}

// TODO: fix the keyboard issue with the input fields
// TODO: return unit of measurement to the input field when out of focus
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MeasurementInput(name: String, unit: String) {
    var text by remember {
        mutableStateOf("")
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(modifier = Modifier
        .width(150.dp)
        .clickable {
            Log.i("mare -->", "clickable")
            keyboardController?.hide()
        },
        value = text,
        onValueChange = { newText ->
            Log.i("mare -->", "Input: $newText")
            val decimalRegex = "^\\d{0,3}(\\.)?(\\d)?$".toRegex()
            if (!decimalRegex.matches(newText)) {
                return@OutlinedTextField
            }
            text = newText
        },
        label = { Text("$name") },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done
        ),
        maxLines = 1,
        isError = text.isEmpty(),
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = "Search")
        },
        trailingIcon = {
            if (text.isNotEmpty()) {
                Icon(Icons.Filled.Clear,
                    contentDescription = "Clear text",
                    modifier = Modifier.clickable { text = "" })
            } else {
                Text(text = "$unit", modifier = Modifier.padding(8.dp))
            }
        })
}

@Composable
fun BodySnapshotScreen() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            //.fillMaxSize()
            .requiredWidth(context.resources.displayMetrics.widthPixels.dp)
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Row() {
                ImagePicker("front")
                ImagePicker("side")
                ImagePicker("back")
            }
        }
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center

        ) {
            MeasurementInput(name = "Weight", unit = "kg")
            MeasurementInput(name = "Waist", unit = "cm")
            MeasurementInput(name = "Thighs", unit = "cm")
            MeasurementInput(name = "Arms", unit = "cm")
        }
    }
}

@Composable
fun ImagePicker(name: String) {
    var imageUri by remember { mutableStateOf<String?>(null) }
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent(),
            onResult = { uri ->
                imageUri = uri.toString()
            })

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { launcher.launch("image/*") }, modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(name.replaceFirstChar { it.uppercase() })
        }
        Log.i(
            "mare -->", "Image URI: $imageUri"
        )
        if (imageUri != null) {
            DisplayImage(imageUri.toString())
        }
    }
}

// TODO - Fix image size
@Composable
fun DisplayImage(imageUri: String) {
    val context = LocalContext.current
    val inputStream = context.contentResolver.openInputStream(imageUri.toUri())
    val bitmap = BitmapFactory.decodeStream(inputStream)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .width(context.resources.displayMetrics.widthPixels.dp / 3f)
    ) {
        Image(
            bitmap = bitmap?.asImageBitmap() ?: ImageBitmap(1, 1),
            contentDescription = "Selected Image",
            modifier = Modifier.fillMaxSize()
        )
    }
}