// Gerekli kütüphaneler (Android Studio bunları otomatik ekler ama eksik olursa kırmızı yanar)
package com.example.prayertimes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Material 3 Teması
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EzanVaktiAnaEkran()
                }
            }
        }
    }
}

@Composable
fun EzanVaktiAnaEkran() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Üst Başlık - Şehir
        Text(
            text = "İSTANBUL",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "15 Ocak Perşembe",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Büyük Vakit Kartı (Sıradaki Vakit)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(32.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("SIRADAKİ VAKİT: AKŞAM", style = MaterialTheme.typography.labelLarge)
                Text("18:12", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold)
                Text("- 01:24 kaldı", style = MaterialTheme.typography.bodyLarge)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Diğer Vakitler Listesi
        VakitSatiri("İmsak", "06:12")
        VakitSatiri("Güneş", "07:45")
        VakitSatiri("Öğle", "13:12")
        VakitSatiri("İkindi", "15:50")
        VakitSatiri("Akşam", "18:12")
        VakitSatiri("Yatsı", "19:45")
    }
}

@Composable
fun VakitSatiri(isim: String, saat: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = isim, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Text(text = saat, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
}