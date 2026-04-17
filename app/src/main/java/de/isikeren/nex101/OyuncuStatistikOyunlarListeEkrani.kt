package de.isikeren.nex101

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.isikeren.nex101.ui.theme.NEX101Theme

data class OyuncuOyunListeItem(
    val oyunId: Int,
    val tarihText: String,
    val baslikText: String,
    val kazanildi: Boolean,
    val turSayisi: Int,
    val endstandPuani: Int,
    val rotCezaAdet: Int,
    val rotCezaPuan: Int,
    val yesilCezaAdet: Int,
    val yesilCezaPuan: Int,
    val puanProRundeText: String
)

data class OyuncuStatistikOyunlarListeUiState(
    val oyuncuAdi: String,
    val toplamOyunSayisi: Int,
    val oyunlar: List<OyuncuOyunListeItem>
)

@Composable
fun OyuncuStatistikOyunlarListeEkrani(
    uiState: OyuncuStatistikOyunlarListeUiState,
    onGeriClick: () -> Unit,
    onOyunClick: (OyuncuOyunListeItem) -> Unit = {}
) {
    BackHandler(onBack = onGeriClick)

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "←",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable(onClick = onGeriClick)
                )

                Text(
                    text = "${uiState.oyuncuAdi} - Spiele",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.background,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Anzahl: ${uiState.toplamOyunSayisi}",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.oyunlar.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Keine gespielten Spiele vorhanden.",
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.oyunlar) { item ->
                        OyuncuOyunKarti(
                            item = item,
                            onClick = { onOyunClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OyuncuOyunKarti(
    item: OyuncuOyunListeItem,
    onClick: () -> Unit
) {
    val kartRenk = if (item.kazanildi) {
        Color(0x332E7D32)
    } else {
        Color(0x33C62828)
    }
    val vurguRenk = if (item.kazanildi) {
        Color(0xFF2E7D32)
    } else {
        Color(0xFFC62828)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = kartRenk,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.tarihText,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = item.baslikText,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {}

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        BilgiSatiri(label = "Runden", value = item.turSayisi.toString())
                        BilgiSatiri(label = "Endstand", value = item.endstandPuani.toString())
                    }

                    Text(
                        text = "Ø ${item.puanProRundeText}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = vurguRenk,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                BilgiSatiri(label = "Ceza (rot)", value = "${item.rotCezaAdet} / ${item.rotCezaPuan}")
                BilgiSatiri(label = "Ceza (grün)", value = "${item.yesilCezaAdet} / ${item.yesilCezaPuan}")
            }
        }
    }
}

@Composable
private fun BilgiSatiri(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OyuncuStatistikOyunlarListeEkraniPreview() {
    NEX101Theme {
        OyuncuStatistikOyunlarListeEkrani(
            uiState = OyuncuStatistikOyunlarListeUiState(
                oyuncuAdi = "Eren",
                toplamOyunSayisi = 9,
                oyunlar = listOf(
                    OyuncuOyunListeItem(
                        oyunId = 1,
                        tarihText = "17.04.2026",
                        baslikText = "Erpo vs. Seray",
                        kazanildi = true,
                        turSayisi = 15,
                        endstandPuani = 865,
                        rotCezaAdet = 3,
                        rotCezaPuan = 360,
                        yesilCezaAdet = 2,
                        yesilCezaPuan = 181,
                        puanProRundeText = "57,66"
                    ),
                    OyuncuOyunListeItem(
                        oyunId = 2,
                        tarihText = "16.04.2026",
                        baslikText = "Erpo vs. Seray",
                        kazanildi = false,
                        turSayisi = 20,
                        endstandPuani = 965,
                        rotCezaAdet = 3,
                        rotCezaPuan = 201,
                        yesilCezaAdet = 2,
                        yesilCezaPuan = 50,
                        puanProRundeText = "48,25"
                    )
                )
            ),
            onGeriClick = {}
        )
    }
}