package de.isikeren.nex101

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.isikeren.nex101.ui.theme.NEX101Theme

enum class MasaPozisyonu(val siraNo: Int) {
    UST(1),
    SOL(2),
    ALT(3),
    SAG(4)
}


data class YeniOyunTaslakDurumu(
    val seciliMod: String = "ortak",
    val takim1Adi: String = "Team 1",
    val takim2Adi: String = "Team 2",
    val ustOyuncuId: Int? = null,
    val solOyuncuId: Int? = null,
    val altOyuncuId: Int? = null,
    val sagOyuncuId: Int? = null
)

data class OyunBaslangicOzeti(
    val oyunId: Int,
    val mod: String,
    val baslangicZamani: Long,
    val takim1Adi: String,
    val takim2Adi: String,
    val oyuncu1Adi: String,
    val oyuncu2Adi: String,
    val oyuncu3Adi: String,
    val oyuncu4Adi: String
)

private fun ortakTakimNo(pozisyon: MasaPozisyonu): Int {
    return when (pozisyon.siraNo) {
        1, 3 -> 1
        2, 4 -> 2
        else -> 0
    }
}

@Composable
fun YeniOyunEkrani(
    taslakDurumu: YeniOyunTaslakDurumu,
    onTaslakDurumuDegisti: (YeniOyunTaslakDurumu) -> Unit,
    onGeriClick: () -> Unit,
    onOyunBasladi: (OyunBaslangicOzeti) -> Unit
) {
    val context = LocalContext.current
    val ekranYuksekligiDp = LocalContext.current.resources.configuration.screenHeightDp
    val kucukEkran = ekranYuksekligiDp < 780
    val ortaAlanAgirlik = if (kucukEkran) 0.78f else 1f
    val altAlanBosluk = if (kucukEkran) 6.dp else 12.dp
    val altKisimAltPadding = if (kucukEkran) 70.dp else 50.dp
    val database = remember { DatabaseProvider.getDatabase(context) }
    val oyuncuDao = remember { database.oyuncuDao() }
    val oyunDao = remember { database.oyunDao() }
    val oyunKatilimciDao = remember { database.oyunKatilimciDao() }
    val oyuncular by oyuncuDao.tumOyunculariGetir().collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    var acikSecimDialoguPozisyonu by remember { mutableStateOf<MasaPozisyonu?>(null) }
    var oyuncuEkleDialogAcik by remember { mutableStateOf(false) }
    var yeniOyuncuAdi by remember { mutableStateOf("") }

    val seciliMod = taslakDurumu.seciliMod
    val takim1Adi = taslakDurumu.takim1Adi
    val takim2Adi = taslakDurumu.takim2Adi
    val ustOyuncuId = taslakDurumu.ustOyuncuId
    val solOyuncuId = taslakDurumu.solOyuncuId
    val altOyuncuId = taslakDurumu.altOyuncuId
    val sagOyuncuId = taslakDurumu.sagOyuncuId

    val ustOyuncu = oyuncular.firstOrNull { it.id == ustOyuncuId }
    val solOyuncu = oyuncular.firstOrNull { it.id == solOyuncuId }
    val altOyuncu = oyuncular.firstOrNull { it.id == altOyuncuId }
    val sagOyuncu = oyuncular.firstOrNull { it.id == sagOyuncuId }

    val seciliOyuncuSayisi = listOf(ustOyuncu, solOyuncu, altOyuncu, sagOyuncu).count { it != null }
    val oyunBaslatAktif = if (seciliMod == "ortak") seciliOyuncuSayisi == 4 else seciliOyuncuSayisi >= 2

    val mevcutOyuncuIdleri = listOfNotNull(
        ustOyuncuId,
        solOyuncuId,
        altOyuncuId,
        sagOyuncuId
    )

    BackHandler(onBack = onGeriClick)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = altKisimAltPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "←",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .clickable(onClick = onGeriClick)
                    .padding(end = 16.dp)
            )

            Text(
                text = "Neues Spiel",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Box(
            modifier = Modifier
                .weight(ortaAlanAgirlik)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            MasaAlani(
                seciliMod = seciliMod,
                onModSec = {
                    onTaslakDurumuDegisti(taslakDurumu.copy(seciliMod = it))
                },
                ustOyuncu = ustOyuncu,
                solOyuncu = solOyuncu,
                altOyuncu = altOyuncu,
                sagOyuncu = sagOyuncu,
                onPozisyonClick = { pozisyon ->
                    acikSecimDialoguPozisyonu = pozisyon
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        if (seciliMod == "ortak") {
            OutlinedTextField(
                value = takim1Adi,
                onValueChange = {
                    onTaslakDurumuDegisti(taslakDurumu.copy(takim1Adi = it))
                },
                label = { Text("Team 1") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = altAlanBosluk)
            )

            OutlinedTextField(
                value = takim2Adi,
                onValueChange = {
                    onTaslakDurumuDegisti(taslakDurumu.copy(takim2Adi = it))
                },
                label = { Text("Team 2") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = altAlanBosluk)
            )
        }

        if (oyunBaslatAktif) {
            AnaMenuButonu(
                text = "Spiel starten",
                onClick = {
                    scope.launch {
                        val baslangicZamani = System.currentTimeMillis()

                        val oyunId = oyunDao.oyunEkle(
                            OyunEntity(
                                mod = seciliMod,
                                baslangicZamani = baslangicZamani,
                                durum = "devam_ediyor"
                            )
                        ).toInt()

                        val katilimcilar = buildList {
                            if (ustOyuncu != null) {
                                add(
                                    OyunKatilimciEntity(
                                        oyunId = oyunId,
                                        oyuncuId = ustOyuncu.id,
                                        pozisyon = 1,
                                        takimNo = if (seciliMod == "ortak") 1 else null,
                                        takimAdi = if (seciliMod == "ortak") takim1Adi.trim().ifBlank { "Team 1" } else null
                                    )
                                )
                            }
                            if (solOyuncu != null) {
                                add(
                                    OyunKatilimciEntity(
                                        oyunId = oyunId,
                                        oyuncuId = solOyuncu.id,
                                        pozisyon = 2,
                                        takimNo = if (seciliMod == "ortak") 2 else null,
                                        takimAdi = if (seciliMod == "ortak") takim2Adi.trim().ifBlank { "Team 2" } else null
                                    )
                                )
                            }
                            if (altOyuncu != null) {
                                add(
                                    OyunKatilimciEntity(
                                        oyunId = oyunId,
                                        oyuncuId = altOyuncu.id,
                                        pozisyon = 3,
                                        takimNo = if (seciliMod == "ortak") 1 else null,
                                        takimAdi = if (seciliMod == "ortak") takim1Adi.trim().ifBlank { "Team 1" } else null
                                    )
                                )
                            }
                            if (sagOyuncu != null) {
                                add(
                                    OyunKatilimciEntity(
                                        oyunId = oyunId,
                                        oyuncuId = sagOyuncu.id,
                                        pozisyon = 4,
                                        takimNo = if (seciliMod == "ortak") 2 else null,
                                        takimAdi = if (seciliMod == "ortak") takim2Adi.trim().ifBlank { "Team 2" } else null
                                    )
                                )
                            }
                        }

                        oyunKatilimciDao.katilimcilariEkle(katilimcilar)

                        onOyunBasladi(
                            OyunBaslangicOzeti(
                                oyunId = oyunId,
                                mod = seciliMod,
                                baslangicZamani = baslangicZamani,
                                takim1Adi = takim1Adi.trim().ifBlank { "Team 1" },
                                takim2Adi = takim2Adi.trim().ifBlank { "Team 2" },
                                oyuncu1Adi = ustOyuncu?.ad ?: "-",
                                oyuncu2Adi = solOyuncu?.ad ?: "-",
                                oyuncu3Adi = altOyuncu?.ad ?: "-",
                                oyuncu4Adi = sagOyuncu?.ad ?: "-"
                            )
                        )
                    }
                }
            )
        }
    }

    acikSecimDialoguPozisyonu?.let { pozisyon ->
        val buPozisyondakiOyuncu = when (pozisyon) {
            MasaPozisyonu.UST -> ustOyuncu
            MasaPozisyonu.SOL -> solOyuncu
            MasaPozisyonu.ALT -> altOyuncu
            MasaPozisyonu.SAG -> sagOyuncu
        }

        val secilebilirOyuncular = oyuncular.filter { oyuncu ->
            oyuncu.id !in mevcutOyuncuIdleri || oyuncu.id == buPozisyondakiOyuncu?.id
        }

        AlertDialog(
            onDismissRequest = { acikSecimDialoguPozisyonu = null },
            title = { Text("Spieler auswählen") },
            text = {
                Column {
                    AnaMenuButonu(
                        text = "Spieler hinzufügen",
                        onClick = {
                            yeniOyuncuAdi = ""
                            oyuncuEkleDialogAcik = true
                        }
                    )

                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .height(260.dp)
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(secilebilirOyuncular, key = { it.id }) { oyuncu ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            when (pozisyon) {
                                                MasaPozisyonu.UST -> onTaslakDurumuDegisti(taslakDurumu.copy(ustOyuncuId = oyuncu.id))
                                                MasaPozisyonu.SOL -> onTaslakDurumuDegisti(taslakDurumu.copy(solOyuncuId = oyuncu.id))
                                                MasaPozisyonu.ALT -> onTaslakDurumuDegisti(taslakDurumu.copy(altOyuncuId = oyuncu.id))
                                                MasaPozisyonu.SAG -> onTaslakDurumuDegisti(taslakDurumu.copy(sagOyuncuId = oyuncu.id))
                                            }
                                            acikSecimDialoguPozisyonu = null
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "#${oyuncu.id}",
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    Text(
                                        text = oyuncu.ad,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { acikSecimDialoguPozisyonu = null }) {
                    Text("Schließen")
                }
            },
            dismissButton = {}
        )
    }

    if (oyuncuEkleDialogAcik) {
        AlertDialog(
            onDismissRequest = { oyuncuEkleDialogAcik = false },
            title = { Text("Spieler hinzufügen") },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = yeniOyuncuAdi,
                    onValueChange = { yeniOyuncuAdi = it },
                    label = { Text("Spielername") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val temizAd = yeniOyuncuAdi.trim()
                        if (temizAd.isNotEmpty()) {
                            scope.launch {
                                oyuncuDao.oyuncuEkle(OyuncuEntity(ad = temizAd))
                                yeniOyuncuAdi = ""
                                oyuncuEkleDialogAcik = false
                            }
                        }
                    }
                ) {
                    Text("Hinzufügen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        oyuncuEkleDialogAcik = false
                        yeniOyuncuAdi = ""
                    }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
private fun MasaAlani(
    seciliMod: String,
    onModSec: (String) -> Unit,
    ustOyuncu: OyuncuEntity?,
    solOyuncu: OyuncuEntity?,
    altOyuncu: OyuncuEntity?,
    sagOyuncu: OyuncuEntity?,
    onPozisyonClick: (MasaPozisyonu) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val kisaKenar = minOf(maxWidth, maxHeight)
        val masaBoyutu = (kisaKenar * 0.52f).coerceAtMost(220.dp)

        val ustAltAlan = ((maxHeight - masaBoyutu) / 2).coerceAtLeast(86.dp)
        val sagSolAlan = ((maxWidth - masaBoyutu) / 2).coerceAtLeast(72.dp)

        val dikeyUzaklik = (masaBoyutu / 2) + (ustAltAlan * 0.42f)
        val yatayUzaklik = (masaBoyutu / 2) + (sagSolAlan * 0.60f)

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            OyuncuEkleButonu(
                pozisyon = MasaPozisyonu.UST,
                oyuncu = ustOyuncu,
                takimGoster = seciliMod == "ortak",
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = -dikeyUzaklik),
                onClick = { onPozisyonClick(MasaPozisyonu.UST) }
            )

            OyuncuEkleButonu(
                pozisyon = MasaPozisyonu.SOL,
                oyuncu = solOyuncu,
                takimGoster = seciliMod == "ortak",
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = -yatayUzaklik),
                onClick = { onPozisyonClick(MasaPozisyonu.SOL) }
            )

            OyuncuEkleButonu(
                pozisyon = MasaPozisyonu.SAG,
                oyuncu = sagOyuncu,
                takimGoster = seciliMod == "ortak",
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = yatayUzaklik),
                onClick = { onPozisyonClick(MasaPozisyonu.SAG) }
            )

            OyuncuEkleButonu(
                pozisyon = MasaPozisyonu.ALT,
                oyuncu = altOyuncu,
                takimGoster = seciliMod == "ortak",
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = dikeyUzaklik),
                onClick = { onPozisyonClick(MasaPozisyonu.ALT) }
            )

            Box(
                modifier = Modifier
                    .size(masaBoyutu)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ModSecici(
                        seciliMod = seciliMod,
                        onModSec = onModSec
                    )
                }
            }
        }
    }
}

@Composable
private fun ModSecici(
    seciliMod: String,
    onModSec: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ModButonu(
            text = "tek",
            secili = seciliMod == "tek",
            onClick = { onModSec("tek") }
        )

        ModButonu(
            text = "ortak",
            secili = seciliMod == "ortak",
            onClick = { onModSec("ortak") }
        )
    }
}

@Composable
private fun ModButonu(
    text: String,
    secili: Boolean,
    onClick: () -> Unit
) {
    val arkaPlan = if (secili) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    val yaziRengi = if (secili) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .size(width = 120.dp, height = 56.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(arkaPlan)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = yaziRengi,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun OyuncuEkleButonu(
    pozisyon: MasaPozisyonu,
    oyuncu: OyuncuEntity?,
    takimGoster: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "P${pozisyon.siraNo}",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = oyuncu?.ad ?: "+",
                style = if (oyuncu == null) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }

        if (takimGoster) {
            Text(
                text = if (ortakTakimNo(pozisyon) == 1) "Takim 1" else "Takim 2",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun YeniOyunEkraniPreview() {
    NEX101Theme {
        Surface {
            YeniOyunEkrani(
                taslakDurumu = YeniOyunTaslakDurumu(),
                onTaslakDurumuDegisti = {},
                onGeriClick = {},
                onOyunBasladi = {}
            )
        }
    }
}