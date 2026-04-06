package de.isikeren.nex101

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.first
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.isikeren.nex101.ui.theme.NEX101Theme

data class VorherigesRundenDetayUiState(
    val oyunId: Int,
    val turNo: Int,
    val takim1Adi: String,
    val takim2Adi: String,
    val takim1Oyuncular: List<VorherigesRundenOyuncuOzet>,
    val takim2Oyuncular: List<VorherigesRundenOyuncuOzet>,
    val takim1RundeToplam: Int,
    val takim2RundeToplam: Int,
    val takim1CezaToplam: Int,
    val takim2CezaToplam: Int,
    val cezalar: List<VorherigesRundenCezaOzet>
)

data class VorherigesRundenOyuncuOzet(
    val oyuncuAdi: String,
    val sonucPuani: Int,
    val sonucDetayi: String = ""
)

data class VorherigesRundenCezaOzet(
    val cezaTipiText: String,
    val kirmiziOyuncuAdi: String,
    val yesilOyuncuAdi: String? = null,
    val puan: Int
)

@Composable
fun VorherigesRundenDetayEkrani(
    oyunId: Int,
    turNo: Int,
    onGeriClick: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { DatabaseProvider.getDatabase(context) }
    val oyunKatilimciDao = remember { database.oyunKatilimciDao() }
    val oyuncuDao = remember { database.oyuncuDao() }
    val turDao = remember { database.turDao() }
    val turOyuncuSonucDao = remember { database.turOyuncuSonucDao() }
    val cezaDao = remember { database.cezaDao() }

    val uiState by produceState<VorherigesRundenDetayUiState?>(initialValue = null, key1 = oyunId, key2 = turNo) {
        val katilimcilar = oyunKatilimciDao.oyununKatilimcilariniGetir(oyunId).sortedBy { it.pozisyon }
        val oyuncular = oyuncuDao.tumOyunculariGetir().first().associateBy { it.id }
        val tur = turDao.oyunVeTurNoIleTurGetir(oyunId, turNo)

        if (tur == null) {
            value = null
            return@produceState
        }

        val sonucListesi = turOyuncuSonucDao.turunOyuncuSonuclariniGetirListe(tur.id)
        val cezaListesi = cezaDao.turunCezalariniGetirListe(tur.id)

        val takim1Adi = katilimcilar.firstOrNull { it.takimNo == 1 }?.takimAdi ?: "Team 1"
        val takim2Adi = katilimcilar.firstOrNull { it.takimNo == 2 }?.takimAdi ?: "Team 2"

        val takim1OyuncuIdleri = katilimcilar.filter { it.takimNo == 1 }.map { it.oyuncuId }.toSet()
        val takim2OyuncuIdleri = katilimcilar.filter { it.takimNo == 2 }.map { it.oyuncuId }.toSet()

        val takim1Oyuncular = katilimcilar
            .filter { it.takimNo == 1 }
            .map { katilimci ->
                val sonuc = sonucListesi.firstOrNull { it.oyuncuId == katilimci.oyuncuId }
                VorherigesRundenOyuncuOzet(
                    oyuncuAdi = oyuncular[katilimci.oyuncuId]?.ad ?: "-",
                    sonucPuani = sonuc?.sonucPuani ?: 0,
                    sonucDetayi = sonuc?.toSonucDetayiText() ?: ""
                )
            }

        val takim2Oyuncular = katilimcilar
            .filter { it.takimNo == 2 }
            .map { katilimci ->
                val sonuc = sonucListesi.firstOrNull { it.oyuncuId == katilimci.oyuncuId }
                VorherigesRundenOyuncuOzet(
                    oyuncuAdi = oyuncular[katilimci.oyuncuId]?.ad ?: "-",
                    sonucPuani = sonuc?.sonucPuani ?: 0,
                    sonucDetayi = sonuc?.toSonucDetayiText() ?: ""
                )
            }

        val cezalar = cezaListesi.map { ceza ->
            VorherigesRundenCezaOzet(
                cezaTipiText = ceza.cezaTipi.toCezaTipiText(),
                kirmiziOyuncuAdi = oyuncular[ceza.kirmiziOyuncuId]?.ad ?: "-",
                yesilOyuncuAdi = ceza.yesilOyuncuId?.let { oyuncular[it]?.ad },
                puan = ceza.puan
            )
        }

        val takim1RundeToplam = sonucListesi.filter { it.oyuncuId in takim1OyuncuIdleri }.sumOf { it.sonucPuani }
        val takim2RundeToplam = sonucListesi.filter { it.oyuncuId in takim2OyuncuIdleri }.sumOf { it.sonucPuani }
        val takim1CezaToplam = cezaListesi.filter { it.kirmiziOyuncuId in takim1OyuncuIdleri }.sumOf { it.puan }
        val takim2CezaToplam = cezaListesi.filter { it.kirmiziOyuncuId in takim2OyuncuIdleri }.sumOf { it.puan }

        value = VorherigesRundenDetayUiState(
            oyunId = oyunId,
            turNo = turNo,
            takim1Adi = takim1Adi,
            takim2Adi = takim2Adi,
            takim1Oyuncular = takim1Oyuncular,
            takim2Oyuncular = takim2Oyuncular,
            takim1RundeToplam = takim1RundeToplam,
            takim2RundeToplam = takim2RundeToplam,
            takim1CezaToplam = takim1CezaToplam,
            takim2CezaToplam = takim2CezaToplam,
            cezalar = cezalar
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
                Text("Runde wird geladen...")
            }
        }
    } else {
        VorherigesRundenDetayEkrani(
            uiState = uiState!!,
            onGeriClick = onGeriClick
        )
    }
}

@Composable
fun VorherigesRundenDetayEkrani(
    uiState: VorherigesRundenDetayUiState,
    onGeriClick: () -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            UstBaslik(
                baslik = "Runde ${uiState.turNo}",
                onGeriClick = onGeriClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            TeamOzetSatiri(
                takimAdi = uiState.takim1Adi,
                oyuncular = uiState.takim1Oyuncular,
                rundeToplami = uiState.takim1RundeToplam,
                cezaToplami = uiState.takim1CezaToplam
            )

            Spacer(modifier = Modifier.height(12.dp))

            TeamOzetSatiri(
                takimAdi = uiState.takim2Adi,
                oyuncular = uiState.takim2Oyuncular,
                rundeToplami = uiState.takim2RundeToplam,
                cezaToplami = uiState.takim2CezaToplam
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Cezalar",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            CezalarOyuncuTablosu(
                uiState = uiState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun UstBaslik(
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
private fun TeamOzetSatiri(
    takimAdi: String,
    oyuncular: List<VorherigesRundenOyuncuOzet>,
    rundeToplami: Int,
    cezaToplami: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = "$takimAdi  ${rundeToplami + cezaToplami}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        oyuncular.forEach { oyuncu ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = oyuncu.oyuncuAdi,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (oyuncu.sonucDetayi.isNotBlank()) {
                        Text(
                            text = oyuncu.sonucDetayi,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = oyuncu.sonucPuani.toString(),
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(60.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun CezalarOyuncuTablosu(
    uiState: VorherigesRundenDetayUiState,
    modifier: Modifier = Modifier
) {
    val oyuncuSirasi = (uiState.takim1Oyuncular + uiState.takim2Oyuncular).map { it.oyuncuAdi }
    val cezaKolonlari = oyuncuSirasi.associateWith { oyuncuAdi ->
        uiState.cezalar.filter { ceza ->
            ceza.kirmiziOyuncuAdi == oyuncuAdi
        }
    }
    val maxSatir = cezaKolonlari.values.maxOfOrNull { it.size } ?: 0

    if (oyuncuSirasi.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text("Keine Spieler vorhanden.")
        }
        return
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 10.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            oyuncuSirasi.forEach { oyuncuAdi ->
                CezaTabloHucre(
                    text = oyuncuAdi,
                    modifier = Modifier.weight(1f),
                    bold = true
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                )
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (uiState.cezalar.isEmpty()) {
                Text(
                    text = "Keine Cezas in dieser Runde.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                repeat(maxSatir) { satirIndex ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        oyuncuSirasi.forEach { oyuncuAdi ->
                            val ceza = cezaKolonlari[oyuncuAdi].orEmpty().getOrNull(satirIndex)
                            CezaIcerikHucre(
                                text = ceza?.let { cezaKartMetni(it, oyuncuAdi) } ?: "",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CezaTabloHucre(
    text: String,
    modifier: Modifier = Modifier,
    bold: Boolean = false
) {
    Box(
        modifier = modifier.padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
            style = if (bold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun CezaIcerikHucre(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .border(
                width = if (text.isNotBlank()) 1.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun cezaKartMetni(
    ceza: VorherigesRundenCezaOzet,
    oyuncuAdi: String
): String {
    val digerOyuncu = when (oyuncuAdi) {
        ceza.kirmiziOyuncuAdi -> ceza.yesilOyuncuAdi
        ceza.yesilOyuncuAdi -> ceza.kirmiziOyuncuAdi
        else -> null
    }

    return if (!digerOyuncu.isNullOrBlank()) {
        "${ceza.cezaTipiText}\n$digerOyuncu\n${ceza.puan}"
    } else {
        "${ceza.cezaTipiText}\n${ceza.puan}"
    }
}

private fun TurOyuncuSonucEntity.toSonucDetayiText(): String {
    val detaylar = buildList {
        if (cift) add("çifte")
        if (bitti) add("bitti")
        if (okeyle) add("okeyle")
        if (eldenBitti) add("elden")
        if (acamadi) add("açamadı")
    }
    return detaylar.joinToString(", ")
}

private fun String.toCezaTipiText(): String {
    return when (this) {
        "ISLEK_ATTI" -> "İşlek attı"
        "TAS_CEKILDI" -> "Taş çekildi"
        "ACAMADI_CEZA" -> "Açamadı ceza"
        "OKEY_ATTI" -> "Okey attı"
        "OKEY_ELDE" -> "Okey elde"
        "OKEY_CALDIRDI" -> "Okey çaldırdı"
        "DIGER" -> "Diğer"
        else -> this
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun VorherigesRundenDetayEkraniPreview() {
    NEX101Theme {
        VorherigesRundenDetayEkrani(
            uiState = VorherigesRundenDetayUiState(
                oyunId = 1,
                turNo = 3,
                takim1Adi = "Mühendis",
                takim2Adi = "Gay",
                takim1Oyuncular = listOf(
                    VorherigesRundenOyuncuOzet("Eren", 88, "bitti, okeyle"),
                    VorherigesRundenOyuncuOzet("Erol", 88, "")
                ),
                takim2Oyuncular = listOf(
                    VorherigesRundenOyuncuOzet("Semir", 0, "açamadı"),
                    VorherigesRundenOyuncuOzet("Eray", 0, "")
                ),
                takim1RundeToplam = 176,
                takim2RundeToplam = 0,
                takim1CezaToplam = 0,
                takim2CezaToplam = 101,
                cezalar = listOf(
                    VorherigesRundenCezaOzet(
                        cezaTipiText = "Taş çekildi",
                        kirmiziOyuncuAdi = "Semir",
                        yesilOyuncuAdi = "Eren",
                        puan = 50
                    ),
                    VorherigesRundenCezaOzet(
                        cezaTipiText = "Okey attı",
                        kirmiziOyuncuAdi = "Semir",
                        yesilOyuncuAdi = null,
                        puan = 51
                    )
                )
            ),
            onGeriClick = {}
        )
    }
}
