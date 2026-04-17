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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

data class OyuncuStatistikUiState(
    val oyuncuAdi: String,
    val oyunSayisi: Int,
    val turSayisi: Int,
    val rank: Int,
    val kazanilanOyunSayisi: Int,
    val toplamEndstandPuani: Int,
    val rotCezaAdet: Int,
    val rotCezaPuan: Int,
    val yesilCezaAdet: Int,
    val yesilCezaPuan: Int,
    val acdiSayisi: Int,
    val bittiSayisi: Int,
    val okeyleBitirmeSayisi: Int,
    val eldenBitirmeSayisi: Int,
    val ciftSayisi: Int,
    val acamadiSayisi: Int
)

@Composable
fun OyuncuStatistikEkrani(
    uiState: OyuncuStatistikUiState,
    onGeriClick: () -> Unit,
    onSpieleClick: () -> Unit = {},
    onRundenClick: () -> Unit = {},
    onRankClick: () -> Unit = {},
    onGewonneneSpieleClick: () -> Unit = {},
    onGesamtpunkteClick: () -> Unit = {},
    onRotCezaClick: () -> Unit = {},
    onYesilCezaClick: () -> Unit = {},
    onAcdiClick: () -> Unit = {},
    onBittiClick: () -> Unit = {},
    onOkeyleClick: () -> Unit = {},
    onEldenClick: () -> Unit = {},
    onCiftClick: () -> Unit = {},
    onAcamadiClick: () -> Unit = {}
) {
    BackHandler(onBack = onGeriClick)

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
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
                    text = uiState.oyuncuAdi,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StatistikKarte(
                    title = "Spiele",
                    value = uiState.oyunSayisi.toString(),
                    modifier = Modifier.weight(1f),
                    onClick = onSpieleClick
                )
                StatistikKarte(
                    title = "Runden",
                    value = uiState.turSayisi.toString(),
                    modifier = Modifier.weight(1f),
                    onClick = onRundenClick
                )
                StatistikKarte(
                    title = "Rang",
                    value = if (uiState.rank > 0) uiState.rank.toString() else "-",
                    modifier = Modifier.weight(0.8f),
                    onClick = onRankClick
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StatistikKarte(
                    title = "Gewonnene\nSpiele",
                    value = uiState.kazanilanOyunSayisi.toString(),
                    modifier = Modifier.weight(1f),
                    onClick = onGewonneneSpieleClick
                )
                StatistikKarte(
                    title = "Gesamtpunkte\nEndstand",
                    value = uiState.toplamEndstandPuani.toString(),
                    modifier = Modifier.weight(1.3f),
                    onClick = onGesamtpunkteClick
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            CezaKarti(
                rotAdet = uiState.rotCezaAdet,
                rotPuan = uiState.rotCezaPuan,
                yesilAdet = uiState.yesilCezaAdet,
                yesilPuan = uiState.yesilCezaPuan,
                onRotClick = onRotCezaClick,
                onYesilClick = onYesilCezaClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StatistikKarte(
                    title = "acdı",
                    value = uiState.acdiSayisi.toString(),
                    modifier = Modifier.weight(1f),
                    onClick = onAcdiClick
                )
                StatistikKarte(
                    title = "bitti",
                    value = uiState.bittiSayisi.toString(),
                    modifier = Modifier.weight(1f),
                    onClick = onBittiClick
                )
                StatistikKarte(
                    title = "okeyle",
                    value = uiState.okeyleBitirmeSayisi.toString(),
                    modifier = Modifier.weight(1f),
                    onClick = onOkeyleClick
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StatistikKarte(
                    title = "elden",
                    value = uiState.eldenBitirmeSayisi.toString(),
                    modifier = Modifier.weight(1f),
                    onClick = onEldenClick
                )
                StatistikKarte(
                    title = "çift",
                    value = uiState.ciftSayisi.toString(),
                    modifier = Modifier.weight(1f),
                    onClick = onCiftClick
                )
                StatistikKarte(
                    title = "açmadı",
                    value = uiState.acamadiSayisi.toString(),
                    modifier = Modifier.weight(1f),
                    onClick = onAcamadiClick
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun StatistikKarte(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.titleLarge.lineHeight
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CezaKarti(
    rotAdet: Int,
    rotPuan: Int,
    yesilAdet: Int,
    yesilPuan: Int,
    onRotClick: () -> Unit,
    onYesilClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Ceza",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "rot",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFC62828),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "grün",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(96.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CezaKolonu(
                    baslikRenk = Color(0xFFC62828),
                    adet = rotAdet,
                    puan = rotPuan,
                    modifier = Modifier.weight(1f),
                    onClick = onRotClick
                )
                CezaKolonu(
                    baslikRenk = Color(0xFF2E7D32),
                    adet = yesilAdet,
                    puan = yesilPuan,
                    modifier = Modifier.weight(1f),
                    onClick = onYesilClick
                )
            }
        }
    }
}

@Composable
private fun CezaKolonu(
    baslikRenk: Color,
    adet: Int,
    puan: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Anzahl:  $adet",
                style = MaterialTheme.typography.titleMedium,
                color = baslikRenk
            )
            Text(
                text = "Punkte:  $puan",
                style = MaterialTheme.typography.titleMedium,
                color = baslikRenk
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OyuncuStatistikEkraniPreview() {
    NEX101Theme {
        OyuncuStatistikEkrani(
            uiState = OyuncuStatistikUiState(
                oyuncuAdi = "Eren",
                oyunSayisi = 12,
                turSayisi = 84,
                rank = 1,
                kazanilanOyunSayisi = 7,
                toplamEndstandPuani = -215,
                rotCezaAdet = 9,
                rotCezaPuan = 454,
                yesilCezaAdet = 6,
                yesilCezaPuan = 252,
                acdiSayisi = 11,
                bittiSayisi = 14,
                okeyleBitirmeSayisi = 3,
                eldenBitirmeSayisi = 1,
                ciftSayisi = 5,
                acamadiSayisi = 8
            ),
            onGeriClick = {}
        )
    }
}