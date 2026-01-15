package com.example.prayertimes

import retrofit2.converter.gson.GsonConverterFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import android.content.Context
import android.util.Log
import android.hardware.*
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- 1. SENSÖR YÖNETİCİSİ ---
class CompassManager(context: Context, val onAngleChanged: (Float) -> Unit) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private var gData = FloatArray(3)
    private var mData = FloatArray(3)

    fun start() {
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, mag, SensorManager.SENSOR_DELAY_UI)
    }
    fun stop() { sensorManager.unregisterListener(this) }
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) gData = event.values
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) mData = event.values
        val r = FloatArray(9)
        if (SensorManager.getRotationMatrix(r, null, gData, mData)) {
            val orient = FloatArray(3)
            SensorManager.getOrientation(r, orient)
            onAngleChanged(-Math.toDegrees(orient[0].toDouble()).toFloat())
        }
    }
    override fun onAccuracyChanged(s: Sensor?, a: Int) {}
}

// --- 2. ANA ACTIVITY ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // --- 1. ViewModel ve Durum Tanımlamaları ---
            val prayerViewModel: PrayerViewModel = viewModel()
            var currentScreen by remember { mutableStateOf("vakitler") }
            var isDarkMode by remember { mutableStateOf(true) }
            var azimuth by remember { mutableStateOf(0f) }
            val context = LocalContext.current

            // --- 2. API Veri Çekme (Hata Takibi İçin Loglu) ---
            LaunchedEffect(Unit) {
                Log.d("API_HATASI", "Veri çekme işlemi başlatılıyor: Berlin, Germany")
                prayerViewModel.fetchVakitler("Berlin", "Germany")
            }

            // --- 3. Pusula Sensör Yönetimi ---
            val compassManager = remember { CompassManager(context) { azimuth = it } }
            DisposableEffect(Unit) {
                compassManager.start()
                onDispose { compassManager.stop() }
            }

            // --- 4. Tema Ayarları ---
            val dynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            val colors = when {
                dynamic && isDarkMode -> dynamicDarkColorScheme(context)
                dynamic && !isDarkMode -> dynamicLightColorScheme(context)
                isDarkMode -> darkColorScheme(primary = Color(0xFFEDBBC6))
                else -> lightColorScheme(primary = Color(0xFF8E4A62))
            }

            // --- 5. UI Arayüzü ---
            MaterialTheme(colorScheme = colors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Üst Bar
                            UpperBar(isDarkMode) { isDarkMode = !isDarkMode }

                            // Ekran İçeriği
                            if (currentScreen == "vakitler") {
                                VakitlerEkrani() // ViewModel buranın içinde kullanılmalı
                            } else {
                                PusulaEkrani(azimuth)
                            }
                        }

                        // Alt Bar (Yüzen Bar)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 32.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            FloatingBar(currentScreen) { currentScreen = it }
                        }
                    }
                }
            }
        }
    }
}


// --- 3. UI BİLEŞENLERİ ---

@Composable
fun UpperBar(isDark: Boolean, onThemeToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("İSTANBUL", fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 2.sp)
        IconButton(onClick = onThemeToggle) {
            Icon(if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode, null)
        }
    }
}

@Composable
fun VakitlerEkrani() {
    val suAnkiVakit = "Akşam"
    val vakitler = listOf("İmsak" to "06:12", "Güneş" to "07:45", "Öğle" to "13:12", "İkindi" to "15:50", "Akşam" to "18:12", "Yatsı" to "19:45")

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(32.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(24.dp)) {
            Column {
                Text("SIRADAKİ: $suAnkiVakit", style = MaterialTheme.typography.labelSmall)
                Text("18:12", fontSize = 42.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 120.dp)) {
            items(vakitler) { (isim, saat) ->
                val isSelected = isim == suAnkiVakit
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .then(if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp)) else Modifier)
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(isim, fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold)
                    Text(saat, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun PusulaEkrani(azimuth: Float) {
    val animatedAzimuth by animateFloatAsState(targetValue = azimuth, label = "pusula_anim")
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Circle, null, modifier = Modifier.size(260.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
            Icon(Icons.Default.Navigation, null, modifier = Modifier.size(140.dp).rotate(animatedAzimuth), tint = MaterialTheme.colorScheme.primary)
            Text("N", modifier = Modifier.padding(bottom = 180.dp), color = Color.Red, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("${((azimuth + 360) % 360).toInt()}°", fontSize = 28.sp, fontWeight = FontWeight.Light)
    }
}

@Composable
fun FloatingBar(current: String, onTabSelect: (String) -> Unit) {
    Surface(
        modifier = Modifier.height(70.dp).fillMaxWidth(0.7f),
        shape = RoundedCornerShape(35.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(0.95f),
        shadowElevation = 10.dp
    ) {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            FloatingItem(Icons.Default.CalendarToday, current == "vakitler") { onTabSelect("vakitler") }
            FloatingItem(Icons.Default.Explore, current == "pusula") { onTabSelect("pusula") }
        }
    }
}

@Composable
fun FloatingItem(icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    val color by animateColorAsState(if (selected) MaterialTheme.colorScheme.primary else Color.Gray, label = "tab_color")
    Box(modifier = Modifier.size(50.dp).clip(CircleShape).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = color)
    }
}


// OKKKKK

// API Arayüzü
interface PrayerApi {
    @GET("v1/timingsByCity")
    suspend fun getTimings(
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("method") method: Int = 2
    ): PrayerResponse
}

// ViewModel: Veriyi yöneten merkez
class PrayerViewModel : ViewModel() {
    var timings by mutableStateOf<Map<String, String>?>(null)
    var isLoading by mutableStateOf(false)

    private val api = Retrofit.Builder()
        .baseUrl("https://api.aladhan.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PrayerApi::class.java)

    fun fetchVakitler(sehir: String, ulke: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = api.getTimings(sehir, ulke)
                val timings = response.data.timings
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}

data class PrayerResponse(
    val data: PrayerData
)

data class PrayerData(
    val timings: Timings // İşte eksik olan veya ismi farklı olan kısım burası
)

data class Timings(
    val Fajr: String,
    val Dhuhr: String,
    val Asr: String,
    val Maghrib: String,
    val Isha: String
)