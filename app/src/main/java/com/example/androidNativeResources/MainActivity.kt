package com.example.androidNativeResources
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DatabaseHelper(this)
        setContent {
            Formulario(dbHelper)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Formulario(dbHelper: DatabaseHelper) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var comentario by remember { mutableStateOf("") }
    var imageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoFile by remember { mutableStateOf<File?>(null) }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    val context = LocalContext.current

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)


    val launcherCamera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoFile?.let {
                imageBitmap = BitmapFactory.decodeFile(it.absolutePath)
                photoUri = Uri.fromFile(it)
            }
        } else {
            Toast.makeText(context, "Foto não capturada", Toast.LENGTH_SHORT).show()
        }
    }

    val LOCATION_PERMISSION_REQUEST_CODE = 1000

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        TextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = comentario,
            onValueChange = { comentario = it },
            label = { Text("Comentário") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (cameraPermissionState.status.isGranted) {
                photoFile = createImageFile(context)
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        it
                    )
                    launcherCamera.launch(photoURI)
                }
            } else {
                cameraPermissionState.launchPermissionRequest()
            }
        }) {
            Text("Tirar Foto")
        }

        Spacer(modifier = Modifier.height(16.dp))

        imageBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Foto tirada",
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                getLastLocation(fusedLocationClient) { lat, lon ->
                    latitude = lat
                    longitude = lon
                }
            }
        }) {
            Text("Obter Localização")
        }

        Spacer(modifier = Modifier.height(8.dp))

        latitude?.let {
            Text(text = "Latitude: $it")
        }

        longitude?.let {
            Text(text = "Longitude: $it")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (nome.isNotEmpty() && email.isNotEmpty() && comentario.isNotEmpty() && photoUri != null && latitude != null && longitude != null) {
                dbHelper.insertData(
                    nome,
                    email,
                    comentario,
                    photoUri.toString(),
                    latitude,
                    longitude
                )
                nome = ""
                email = ""
                comentario = ""
                photoUri = null
                imageBitmap = null
                latitude = null
                longitude = null
                Toast.makeText(context, "Dados salvos com sucesso!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Por favor, preencha todos os campos, tire uma foto e obtenha a localização.", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Salvar")
        }
    }
}

fun createImageFile(context: Context): File? {
    return try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    } catch (ex: IOException) {
        ex.printStackTrace()
        null
    }
}

@SuppressLint("MissingPermission")
fun getLastLocation(fusedLocationClient: FusedLocationProviderClient, onLocationReceived: (Double?, Double?) -> Unit) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        location?.let {
            onLocationReceived(it.latitude, it.longitude)
        } ?: run {
            onLocationReceived(null, null)
        }
    }
}
