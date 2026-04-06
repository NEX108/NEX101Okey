package de.isikeren.nex101

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.isikeren.nex101.ui.theme.NEX101Theme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.first

data class VorherigesOyunDetayUiState(
    val oyunId: Int,
    val tarihText: String,
    val saatText: String,
    val modText: String,
    val takim1Adi: String,
    val takim2Adi: String,
    val takim1Oyuncular: List<String>,
    val takim2Oyuncular: List<String>,
    val takim1Toplam: Int,
    val takim2Toplam: Int,
    val kazananTakimNo: Int?,
    val runden: List<VorherigesOyunRundeOzet>
)

data class VorherigesOyunRundeOzet(
    val turNo: Int,
    val takim1Deger: Int,
    val takim2Deger: Int,
    val takim1CezaToplami: Int = 0,
    val takim2CezaToplami: Int = 0
)

@Composable
fun VorherigesOyunDetayEkrani(
    oyunId: Int,
    onGeriClick: () -> Unit,
    onRundeClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val database = remember { DatabaseProvider.getDatabase(context) }
    val oyunDao = remember { database.oyunDao() }
    val oyunKatilimciDao = remember { database.oyunKatilimciDao() }
    val oyuncuDao = remember { database.oyuncuDao() }
    val turDao = remember { database.turDao() }
    val turOyuncuSonucDao = remember { database.turOyuncuSonucDao() }
    val cezaDao = remember { database.cezaDao() }

    val uiState by produceState<VorherigesOyunDetayUiState?>(initialValue = null, key1 = oyunId) {
        val oyun = oyunDao.oyunGetir(oyunId)
        if (oyun == null) {
            value = null
            return@produceState
        }

        val katilimcilar = oyunKatilimciDao.oyununKatilimcilariniGetir(oyunId).sortedBy { it.pozisyon }
        val oyuncular = oyuncuDao.tumOyunculariGetir().first().associateBy { it.id }
        val turlar = turDao.oyununTurlariniGetirListe(oyunId).sortedBy { it.turNo }

        val takim1Adi = katilimcilar.firstOrNull { it.takimNo == 1 }?.takimAdi ?: "Team 1"
        val takim2Adi = katilimcilar.firstOrNull { it.takimNo == 2 }?.takimAdi ?: "Team 2"

        val takim1Oyuncular = katilimcilar
            .filter { it.takimNo == 1 }
            .mapNotNull { oyuncular[it.oyuncuId]?.ad }
        val takim2Oyuncular = katilimcilar
            .filter { it.takimNo == 2 }
            .mapNotNull { oyuncular[it.oyuncuId]?.ad }

        val takim1OyuncuIdleri = katilimcilar.filter { it.takimNo == 1 }.map { it.oyuncuId }.toSet()
        val takim2OyuncuIdleri = katilimcilar.filter { it.takimNo == 2 }.map { it.oyuncuId }.toSet()

        val rundeOzetleri = turlar.map { tur ->
            val sonucListesi = turOyuncuSonucDao.turunOyuncuSonuclariniGetirListe(tur.id)
            val cezaListesi = cezaDao.turunCezalariniGetirListe(tur.id)

            val takim1Deger = sonucListesi.filter { it.oyuncuId in takim1OyuncuIdleri }.sumOf { it.sonucPuani }
            val takim2Deger = sonucListesi.filter { it.oyuncuId in takim2OyuncuIdleri }.sumOf { it.sonucPuani }
            val takim1CezaToplami = cezaListesi.filter { it.kirmiziOyuncuId in takim1OyuncuIdleri }.sumOf { it.puan }
            val takim2CezaToplami = cezaListesi.filter { it.kirmiziOyuncuId in takim2OyuncuIdleri }.sumOf { it.puan }

            VorherigesOyunRundeOzet(
                turNo = tur.turNo,
                takim1Deger = takim1Deger,
                takim2Deger = takim2Deger,
                takim1CezaToplami = takim1CezaToplami,
                takim2CezaToplami = takim2CezaToplami
            )
        }

        val takim1Toplam = rundeOzetleri.sumOf { it.takim1Deger + it.takim1CezaToplami }
        val takim2Toplam = rundeOzetleri.sumOf { it.takim2Deger + it.takim2CezaToplami }

        val tarihKaynak = oyun.bitisZamani ?: oyun.baslangicZamani
        val tarihText = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(tarihKaynak))
        val saatText = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(tarihKaynak))

        value = VorherigesOyunDetayUiState(
            oyunId = oyun.id,
            tarihText = tarihText,
            saatText = saatText,
            modText = oyun.mod,
            takim1Adi = takim1Adi,
            takim2Adi = takim2Adi,
            takim1Oyuncular = takim1Oyuncular,
            takim2Oyuncular = takim2Oyuncular,
            takim1Toplam = takim1Toplam,
            takim2Toplam = takim2Toplam,
            kazananTakimNo = when {
                takim1Toplam < takim2Toplam -> 1
                takim2Toplam < takim1Toplam -> 2
                else -> null
            },
            runden = rundeOzetleri
        )
    }

    if (uiState == null) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Spiel wird geladen...")
            }
        }
    } else {
        VorherigesOyunDetayEkrani(
            uiState = uiState!!,
            onGeriClick = onGeriClick,
            onRundeClick = onRundeClick
        )
    }
}

@Composable
fun VorherigesOyunDetayEkrani(
    uiState: VorherigesOyunDetayUiState,
    onGeriClick: () -> Unit,
    onRundeClick: (Int) -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            BaslikAlani(
                baslik = "Vorheriges Spiel",
                onGeriClick = onGeriClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            BilgiAlani(
                tarihText = uiState.tarihText,
                saatText = uiState.saatText,
                modText = uiState.modText
            )

            Spacer(modifier = Modifier.height(16.dp))

            EndstandAlani(uiState = uiState)

            Spacer(modifier = Modifier.height(12.dp))

            FarkAlani(uiState = uiState)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Runden",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (uiState.runden.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Keine Runden vorhanden.",
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.runden) { runde ->
                        RundeSatiri(
                            runde = runde,
                            onClick = { onRundeClick(runde.turNo) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BaslikAlani(
    baslik: String,
    onGeriClick: () -> Unit
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
            text = baslik,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun BilgiAlani(
    tarihText: String,
    saatText: String,
    modText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            BilgiSatiri(label = "Datum", value = tarihText)
            Spacer(modifier = Modifier.height(8.dp))
            BilgiSatiri(label = "Zeit", value = saatText)
        }

        Text(
            text = "Modus: $modText",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun BilgiSatiri(
    label: String,
    value: String
) {
    Row {
        Text(
            text = "$label ",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun EndstandAlani(
    uiState: VorherigesOyunDetayUiState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TakimSonucKarti(
            takimAdi = uiState.takim1Adi,
            oyuncular = uiState.takim1Oyuncular,
            toplamText = uiState.takim1Toplam.toString(),
            kazanan = uiState.kazananTakimNo == 1
        )
        TakimSonucKarti(
            takimAdi = uiState.takim2Adi,
            oyuncular = uiState.takim2Oyuncular,
            toplamText = uiState.takim2Toplam.toString(),
            kazanan = uiState.kazananTakimNo == 2
        )
    }
}

@Composable
private fun FarkAlani(
    uiState: VorherigesOyunDetayUiState
) {
    val fark = kotlin.math.abs(uiState.takim1Toplam - uiState.takim2Toplam)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 10.dp, horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Fark: $fark",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TakimSonucKarti(
    takimAdi: String,
    oyuncular: List<String>,
    toplamText: String,
    kazanan: Boolean
) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .background(
                color = if (kazanan) Color(0x332E7D32) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = takimAdi,
            style = MaterialTheme.typography.titleMedium,
            color = if (kazanan) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = oyuncular.joinToString(", "),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            text = toplamText,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 10.dp),
            color = if (kazanan) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun RundeSatiri(
    runde: VorherigesOyunRundeOzet,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "R${runde.turNo}",
            modifier = Modifier.width(50.dp),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        RundeDegerHucre(
            anaDeger = runde.takim1Deger,
            cezaDeger = runde.takim1CezaToplami,
            modifier = Modifier.weight(1f)
        )

        RundeDegerHucre(
            anaDeger = runde.takim2Deger,
            cezaDeger = runde.takim2CezaToplami,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RundeDegerHucre(
    anaDeger: Int,
    cezaDeger: Int,
    modifier: Modifier = Modifier
) {
    val toplam = anaDeger + cezaDeger
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Gesamt: $toplam",
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Tur Sonuç: $anaDeger",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Ceza: $cezaDeger",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun VorherigesOyunDetayEkraniPreview() {
    NEX101Theme {
        VorherigesOyunDetayEkrani(
            uiState = VorherigesOyunDetayUiState(
                oyunId = 1,
                tarihText = "06.04.2026",
                saatText = "19:45",
                modText = "ortak",
                takim1Adi = "Mühendis",
                takim2Adi = "Gay",
                takim1Oyuncular = listOf("Eren", "Erol"),
                takim2Oyuncular = listOf("Semir", "Eray"),
                takim1Toplam = 245,
                takim2Toplam = 312,
                kazananTakimNo = 1,
                runden = listOf(
                    VorherigesOyunRundeOzet(
                        turNo = 1,
                        takim1Deger = 120,
                        takim2Deger = 202,
                        takim1CezaToplami = 0,
                        takim2CezaToplami = 101
                    ),
                    VorherigesOyunRundeOzet(
                        turNo = 2,
                        takim1Deger = -101,
                        takim2Deger = 110,
                        takim1CezaToplami = 50,
                        takim2CezaToplami = 0
                    ),
                    VorherigesOyunRundeOzet(
                        turNo = 3,
                        takim1Deger = 176,
                        takim2Deger = 0,
                        takim1CezaToplami = 0,
                        takim2CezaToplami = 0
                    )
                )
            ),
            onGeriClick = {},
            onRundeClick = {}
        )
    }
}
