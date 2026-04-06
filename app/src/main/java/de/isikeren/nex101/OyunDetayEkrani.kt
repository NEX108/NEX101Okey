package de.isikeren.nex101

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import de.isikeren.nex101.ui.theme.NEX101Theme
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OyunDetayEkrani(
    oyunId: Int,
    baslangicOzeti: OyunBaslangicOzeti? = null,
    onGeriClick: () -> Unit,
    onRundeBeendenClick: (Int) -> Unit = {},
    onCezaClick: (Int) -> Unit = {},
    onRundenDetayClick: (Int) -> Unit = {},
    onOyunBitirildiClick: () -> Unit = {},
    onTurSilClick: (Int) -> Unit = {},
    rundenListe: SnapshotStateList<Int>,
    aktifTurNo: Int?,
    onAktifTurNoChange: (Int?) -> Unit,
    ortakTurSonuclari: MutableMap<Int, Pair<Int, Int>>,
    cezaKayitlari: MutableMap<Int, MutableList<CezaKaydi>>
) {
    val context = LocalContext.current
    val database = remember { DatabaseProvider.getDatabase(context) }
    val oyunDao = remember { database.oyunDao() }
    val oyunKatilimciDao = remember { database.oyunKatilimciDao() }
    val oyuncuDao = remember { database.oyuncuDao() }

    val tumOyuncular by oyuncuDao.tumOyunculariGetir().collectAsState(initial = emptyList())

    val oyun by produceState<OyunEntity?>(initialValue = null, key1 = oyunId) {
        value = oyunDao.oyunGetir(oyunId)
    }

    val katilimcilar by produceState<List<OyunKatilimciEntity>>(initialValue = emptyList(), key1 = oyunId) {
        value = oyunKatilimciDao.oyununKatilimcilariniGetir(oyunId)
    }

    BackHandler(onBack = onGeriClick)

    var silinecekTurNo by remember { mutableStateOf<Int?>(null) }
    var hesaplananToplamlar by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var oyunBitirildi by remember { mutableStateOf(false) }

    fun gosterilenTurNo(turNo: Int): Int {
        val index = rundenListe.indexOf(turNo)
        return if (index == -1) turNo else rundenListe.size - index
    }

    fun takimToplamlariniHesapla(): Pair<Int, Int> {
        var takim1Toplami = 0
        var takim2Toplami = 0

        rundenListe.forEach { turNo ->
            val rundeSonucu = ortakTurSonuclari[turNo]
            takim1Toplami += rundeSonucu?.first ?: 0
            takim2Toplami += rundeSonucu?.second ?: 0

            cezaKayitlari[turNo].orEmpty().forEach { ceza ->
                if (ceza.kirmiziTakimRengiArgb == 0xFF81D4FAL) {
                    takim1Toplami += ceza.puan
                } else {
                    takim2Toplami += ceza.puan
                }
            }
        }

        return takim1Toplami to takim2Toplami
    }

    val pozisyon1 = katilimcilar.firstOrNull { it.pozisyon == 1 }
    val pozisyon2 = katilimcilar.firstOrNull { it.pozisyon == 2 }
    val pozisyon3 = katilimcilar.firstOrNull { it.pozisyon == 3 }
    val pozisyon4 = katilimcilar.firstOrNull { it.pozisyon == 4 }

    val oyuncu1 = tumOyuncular.firstOrNull { it.id == pozisyon1?.oyuncuId }
    val oyuncu2 = tumOyuncular.firstOrNull { it.id == pozisyon2?.oyuncuId }
    val oyuncu3 = tumOyuncular.firstOrNull { it.id == pozisyon3?.oyuncuId }
    val oyuncu4 = tumOyuncular.firstOrNull { it.id == pozisyon4?.oyuncuId }

    val takim1Adi = katilimcilar.firstOrNull { it.takimNo == 1 }?.takimAdi
        ?: baslangicOzeti?.takim1Adi
        ?: "Team 1"
    val takim2Adi = katilimcilar.firstOrNull { it.takimNo == 2 }?.takimAdi
        ?: baslangicOzeti?.takim2Adi
        ?: "Team 2"

    val oyuncu1Adi = oyuncu1?.ad ?: baslangicOzeti?.oyuncu1Adi ?: "-"
    val oyuncu2Adi = oyuncu2?.ad ?: baslangicOzeti?.oyuncu2Adi ?: "-"
    val oyuncu3Adi = oyuncu3?.ad ?: baslangicOzeti?.oyuncu3Adi ?: "-"
    val oyuncu4Adi = oyuncu4?.ad ?: baslangicOzeti?.oyuncu4Adi ?: "-"

    val etkinMod: String? = when {
        oyun?.mod == "ortak" -> "ortak"
        oyun?.mod == "tek" -> "tek"
        baslangicOzeti?.mod == "ortak" -> "ortak"
        baslangicOzeti?.mod == "tek" -> "tek"
        katilimcilar.any { it.takimNo != null } -> "ortak"
        katilimcilar.count { !it.takimAdi.isNullOrBlank() } >= 2 -> "ortak"
        oyun == null && katilimcilar.isEmpty() && baslangicOzeti == null -> null
        else -> "tek"
    }

    val baslangicZamani = oyun?.baslangicZamani ?: baslangicOzeti?.baslangicZamani

    val tarihText = baslangicZamani?.let {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))
    } ?: "-"

    val saatText = baslangicZamani?.let {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
    } ?: "-"

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            UstBaslik(
                onGeriClick = onGeriClick,
                baslik = "Spiel Detail"
            )

            OyunBilgiAlani(
                tarihText = tarihText,
                saatText = saatText,
                modText = etkinMod ?: "lädt..."
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (etkinMod) {
                "ortak" -> {
                    OrtakRundeTablosu(
                        takim1Adi = takim1Adi,
                        takim1Oyuncular = listOfNotNull(oyuncu1Adi, oyuncu3Adi),
                        takim2Adi = takim2Adi,
                        takim2Oyuncular = listOfNotNull(oyuncu2Adi, oyuncu4Adi),
                        rundenListe = rundenListe,
                        aktifTurNo = aktifTurNo,
                        modifier = Modifier.weight(1f),
                        onSilinecekTurNoChange = { silinecekTurNo = it },
                        onRundenDetayClick = onRundenDetayClick,
                        ortakTurSonuclari = ortakTurSonuclari,
                        cezaKayitlari = cezaKayitlari
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OrtakAltToplamAlani(
                        takim1Adi = takim1Adi,
                        takim2Adi = takim2Adi,
                        takim1Deger = hesaplananToplamlar?.first,
                        takim2Deger = hesaplananToplamlar?.second,
                        kazananTakimNo = when {
                            !oyunBitirildi || hesaplananToplamlar == null -> null
                            hesaplananToplamlar!!.first < hesaplananToplamlar!!.second -> 1
                            hesaplananToplamlar!!.second < hesaplananToplamlar!!.first -> 2
                            else -> null
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ButonAlani(
                        solUstText = if (aktifTurNo == null) "Neue Runde" else "Runde beenden",
                        sagUstText = "Strafe / Ceza",
                        solAltText = "Spiel beenden",
                        sagAltText = if (oyunBitirildi) "AnaSayfaya Dön" else "Berechnen",
                        solUstAktif = !oyunBitirildi,
                        sagUstAktif = aktifTurNo != null && !oyunBitirildi,
                        solAltAktif = !oyunBitirildi,
                        sagAltAktif = true,
                        onSolUstClick = {
                            if (aktifTurNo == null) {
                                val yeniTurNo = (rundenListe.maxOrNull() ?: 0) + 1
                                rundenListe.add(0, yeniTurNo)
                                onAktifTurNoChange(yeniTurNo)
                            } else {
                                onRundeBeendenClick(aktifTurNo!!)
                            }
                        },
                        onSagUstClick = {
                            aktifTurNo?.let { onCezaClick(it) }
                        },
                        onSolAltClick = {
                            hesaplananToplamlar = takimToplamlariniHesapla()
                            oyunBitirildi = true
                        },
                        onSagAltClick = {
                            if (oyunBitirildi) {
                                onOyunBitirildiClick()
                            } else {
                                hesaplananToplamlar = takimToplamlariniHesapla()
                            }
                        }
                    )
                }

                "tek" -> {
                    TekRundeTablosu(
                        oyuncuAdlari = listOf(
                            oyuncu1Adi,
                            oyuncu2Adi,
                            oyuncu3Adi,
                            oyuncu4Adi
                        ),
                        rundenListe = rundenListe,
                        aktifTurNo = aktifTurNo,
                        modifier = Modifier.weight(1f),
                        onSilinecekTurNoChange = { silinecekTurNo = it },
                        onRundenDetayClick = onRundenDetayClick
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OrtakAltToplamAlani(
                        takim1Adi = "Team 1",
                        takim2Adi = "Team 2",
                        takim1Deger = hesaplananToplamlar?.first,
                        takim2Deger = hesaplananToplamlar?.second,
                        kazananTakimNo = when {
                            !oyunBitirildi || hesaplananToplamlar == null -> null
                            hesaplananToplamlar!!.first < hesaplananToplamlar!!.second -> 1
                            hesaplananToplamlar!!.second < hesaplananToplamlar!!.first -> 2
                            else -> null
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ButonAlani(
                        solUstText = if (aktifTurNo == null) "Neue Runde" else "Runde beenden",
                        sagUstText = "Strafe / Ceza",
                        solAltText = "Spiel beenden",
                        sagAltText = if (oyunBitirildi) "AnaSayfaya Dön" else "Berechnen",
                        solUstAktif = !oyunBitirildi,
                        sagUstAktif = aktifTurNo != null && !oyunBitirildi,
                        solAltAktif = !oyunBitirildi,
                        sagAltAktif = true,
                        onSolUstClick = {
                            if (aktifTurNo == null) {
                                val yeniTurNo = (rundenListe.maxOrNull() ?: 0) + 1
                                rundenListe.add(0, yeniTurNo)
                                onAktifTurNoChange(yeniTurNo)
                            } else {
                                onRundeBeendenClick(aktifTurNo!!)
                            }
                        },
                        onSagUstClick = {
                            aktifTurNo?.let { onCezaClick(it) }
                        },
                        onSolAltClick = {
                            hesaplananToplamlar = takimToplamlariniHesapla()
                            oyunBitirildi = true
                        },
                        onSagAltClick = {
                            if (oyunBitirildi) {
                                onOyunBitirildiClick()
                            } else {
                                hesaplananToplamlar = takimToplamlariniHesapla()
                            }
                        }
                    )
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Spiel wird geladen...")
                    }
                }
            }

            silinecekTurNo?.let { turNo ->
                AlertDialog(
                    onDismissRequest = { silinecekTurNo = null },
                    title = { Text("Runde löschen") },
                    text = { Text("Soll Runde ${gosterilenTurNo(turNo)} gelöscht werden?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onTurSilClick(turNo)
                                silinecekTurNo = null
                            }
                        ) {
                            Text("Löschen")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { silinecekTurNo = null }) {
                            Text("Abbrechen")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun UstBaslik(
    onGeriClick: () -> Unit,
    baslik: String
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
private fun OyunBilgiAlani(
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
private fun OrtakRundeTablosu(
    takim1Adi: String,
    takim1Oyuncular: List<String>,
    takim2Adi: String,
    takim2Oyuncular: List<String>,
    rundenListe: List<Int>,
    aktifTurNo: Int?,
    modifier: Modifier = Modifier,
    onSilinecekTurNoChange: (Int?) -> Unit,
    onRundenDetayClick: (Int) -> Unit,
    ortakTurSonuclari: MutableMap<Int, Pair<Int, Int>>,
    cezaKayitlari: MutableMap<Int, MutableList<CezaKaydi>>
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 10.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabloHucre(
                text = "Runde",
                modifier = Modifier.width(70.dp),
                bold = true
            )
            TabloHucre(
                text = "$takim1Adi\n(${takim1Oyuncular.joinToString(", ")})",
                modifier = Modifier.width(140.dp),
                bold = true
            )
            TabloHucre(
                text = "$takim2Adi\n(${takim2Oyuncular.joinToString(", ")})",
                modifier = Modifier.width(140.dp),
                bold = true
            )
            TabloHucre(
                text = "",
                modifier = Modifier.width(72.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                )
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (rundenListe.isEmpty()) {
                Text(
                    text = "Noch keine Runden vorhanden.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                rundenListe.forEachIndexed { index, turNo ->
                    key(turNo) {
                        val cezaListesi = cezaKayitlari[turNo].orEmpty()
                        val takim1Cezalari = cezaListesi.filter { it.kirmiziTakimRengiArgb == 0xFF81D4FAL }
                        val takim2Cezalari = cezaListesi.filter { it.kirmiziTakimRengiArgb != 0xFF81D4FAL }
                        val cezaSatirSayisi = maxOf(takim1Cezalari.size, takim2Cezalari.size)

                        RundeSwipeSatiri(
                            turNo = turNo,
                            onSil = { onSilinecekTurNoChange(turNo) },
                            onBearbeiten = { onRundenDetayClick(turNo) }
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = if (cezaSatirSayisi > 0) 6.dp else 8.dp)
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val gosterilenTurNo = rundenListe.size - index
                                    val sonuc = ortakTurSonuclari[turNo]
                                    TabloHucre(text = gosterilenTurNo.toString(), modifier = Modifier.width(70.dp))
                                    TabloHucre(
                                        text = when {
                                            sonuc != null -> sonuc.first.toString()
                                            aktifTurNo == turNo -> "läuft"
                                            else -> "-"
                                        },
                                        modifier = Modifier.width(140.dp)
                                    )
                                    TabloHucre(
                                        text = when {
                                            sonuc != null -> sonuc.second.toString()
                                            aktifTurNo == turNo -> "läuft"
                                            else -> "-"
                                        },
                                        modifier = Modifier.width(140.dp)
                                    )
                                    TabloHucre(text = "⋮", modifier = Modifier.width(72.dp))
                                }

                                repeat(cezaSatirSayisi) { cezaIndex ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = if (cezaIndex == cezaSatirSayisi - 1) 8.dp else 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Spacer(modifier = Modifier.width(78.dp))
                                        CezaHucre(
                                            text = takim1Cezalari.getOrNull(cezaIndex)?.puan?.toString() ?: "",
                                            modifier = Modifier.width(148.dp)
                                        )
                                        CezaHucre(
                                            text = takim2Cezalari.getOrNull(cezaIndex)?.puan?.toString() ?: "",
                                            modifier = Modifier.width(148.dp)
                                        )
                                        Spacer(modifier = Modifier.width(80.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun cezaMetni(ceza: CezaKaydi): String {
    return if (ceza.yesilOyuncuAdi != null) {
        "${ceza.cezaTipi.gorunenAd} (${ceza.kirmiziOyuncuAdi}/${ceza.yesilOyuncuAdi}) ${ceza.puan}"
    } else {
        "${ceza.cezaTipi.gorunenAd} (${ceza.kirmiziOyuncuAdi}) ${ceza.puan}"
    }
}

@Composable
private fun CezaHucre(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .border(
                width = if (text.isNotBlank()) 1.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
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

@Composable
private fun TekRundeTablosu(
    oyuncuAdlari: List<String>,
    rundenListe: List<Int>,
    aktifTurNo: Int?,
    modifier: Modifier = Modifier,
    onSilinecekTurNoChange: (Int?) -> Unit,
    onRundenDetayClick: (Int) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 10.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabloHucre(text = "Runde", modifier = Modifier.width(60.dp), bold = true)
            TabloHucre(text = oyuncuAdlari.getOrElse(0) { "-" }, modifier = Modifier.width(70.dp), bold = true)
            TabloHucre(text = oyuncuAdlari.getOrElse(1) { "-" }, modifier = Modifier.width(70.dp), bold = true)
            TabloHucre(text = oyuncuAdlari.getOrElse(2) { "-" }, modifier = Modifier.width(70.dp), bold = true)
            TabloHucre(text = oyuncuAdlari.getOrElse(3) { "-" }, modifier = Modifier.width(70.dp), bold = true)
            TabloHucre(text = "", modifier = Modifier.width(56.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                )
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (rundenListe.isEmpty()) {
                Text(
                    text = "Noch keine Runden vorhanden.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                rundenListe.forEachIndexed { index, turNo ->
                    key(turNo) {
                        RundeSwipeSatiri(
                            turNo = turNo,
                            onSil = { onSilinecekTurNoChange(turNo) },
                            onBearbeiten = { onRundenDetayClick(turNo) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val gosterilenTurNo = rundenListe.size - index
                                TabloHucre(text = gosterilenTurNo.toString(), modifier = Modifier.width(60.dp))
                                repeat(4) {
                                    TabloHucre(
                                        text = if (aktifTurNo == turNo) "läuft" else "-",
                                        modifier = Modifier.width(70.dp)
                                    )
                                }
                                TabloHucre(text = "⋮", modifier = Modifier.width(56.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RundeSwipeSatiri(
    turNo: Int,
    onSil: () -> Unit,
    onBearbeiten: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { hedef ->
            if (hedef == SwipeToDismissBoxValue.EndToStart) {
                onSil()
                false
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart ||
                dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp)
                        .background(
                            color = androidx.compose.ui.graphics.Color.Red,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Löschen",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    ) {
        Box(
            modifier = Modifier.combinedClickable(
                onClick = {},
                onLongClick = { onBearbeiten() }
            )
        ) {
            content()
        }
    }
}

@Composable
private fun ButonAlani(
    solUstText: String,
    sagUstText: String,
    solAltText: String,
    sagAltText: String,
    solUstAktif: Boolean,
    sagUstAktif: Boolean,
    solAltAktif: Boolean,
    sagAltAktif: Boolean,
    onSolUstClick: () -> Unit,
    onSagUstClick: () -> Unit,
    onSolAltClick: () -> Unit,
    onSagAltClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ButtonBox(solUstText, solUstAktif, onSolUstClick)
            ButtonBox(sagUstText, sagUstAktif, onSagUstClick)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ButtonBox(solAltText, solAltAktif, onSolAltClick)
            ButtonBox(sagAltText, sagAltAktif, onSagAltClick)
        }
    }
}

@Composable
private fun ButtonBox(
    text: String,
    aktif: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = aktif,
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = if (aktif) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            }
        )
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }
}

@Composable
private fun OrtakAltToplamAlani(
    takim1Adi: String,
    takim2Adi: String,
    takim1Deger: Int?,
    takim2Deger: Int?,
    kazananTakimNo: Int?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ToplamKart(
            baslik = takim1Adi,
            degerText = takim1Deger?.toString() ?: "xxx",
            kazanan = kazananTakimNo == 1
        )
        ToplamKart(
            baslik = takim2Adi,
            degerText = takim2Deger?.toString() ?: "xxx",
            kazanan = kazananTakimNo == 2
        )
    }
}

@Composable
private fun ToplamKart(
    baslik: String,
    degerText: String,
    kazanan: Boolean
) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .background(
                color = if (kazanan) androidx.compose.ui.graphics.Color(0x332E7D32) else androidx.compose.ui.graphics.Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 2.dp,
                color = if (kazanan) androidx.compose.ui.graphics.Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { }
            .padding(14.dp)
    ) {
        Text(
            text = baslik,
            style = MaterialTheme.typography.titleMedium,
            color = if (kazanan) androidx.compose.ui.graphics.Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
        )
        Text(
            text = degerText,
            modifier = Modifier.padding(top = 10.dp),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TabloHucre(
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
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OyunDetayEkraniPreview() {
    NEX101Theme {
        OyunDetayEkrani(
            oyunId = 1,
            baslangicOzeti = OyunBaslangicOzeti(
                oyunId = 1,
                mod = "ortak",
                baslangicZamani = System.currentTimeMillis(),
                takim1Adi = "Mühendis",
                takim2Adi = "Gay",
                oyuncu1Id = 1,
                oyuncu2Id = 2,
                oyuncu3Id = 3,
                oyuncu4Id = 4,
                oyuncu1Adi = "Eren",
                oyuncu2Adi = "Semir",
                oyuncu3Adi = "Erol",
                oyuncu4Adi = "Eray"
            ),
            onGeriClick = {},
            onCezaClick = {},
            onRundenDetayClick = {},
            onOyunBitirildiClick = {},
            onTurSilClick = {},
            rundenListe = remember { mutableStateListOf(3, 2, 1) },
            aktifTurNo = 3,
            onAktifTurNoChange = {},
            ortakTurSonuclari = remember {
                mutableStateMapOf(
                    2 to (120 to 240),
                    1 to (-101 to 404)
                )
            },
            cezaKayitlari = remember {
                mutableStateMapOf(
                    3 to mutableStateListOf(
                        CezaKaydi(
                            id = 1,
                            turNo = 3,
                            cezaTipi = CezaTipi.TAS_CEKILDI,
                            puan = 50,
                            kirmiziOyuncuAdi = "Eren",
                            kirmiziTakimRengiArgb = 0xFF81D4FAL,
                            yesilOyuncuAdi = "Semir",
                            yesilTakimRengiArgb = 0xFFC62828L
                        ),
                        CezaKaydi(
                            id = 2,
                            turNo = 3,
                            cezaTipi = CezaTipi.OKEY_ATTI,
                            puan = 101,
                            kirmiziOyuncuAdi = "Semir",
                            kirmiziTakimRengiArgb = 0xFFC62828L
                        )
                    ),
                    2 to mutableStateListOf(
                        CezaKaydi(
                            id = 3,
                            turNo = 2,
                            cezaTipi = CezaTipi.ISLEK_ATTI,
                            puan = 101,
                            kirmiziOyuncuAdi = "Eray",
                            kirmiziTakimRengiArgb = 0xFFC62828L
                        )
                    )
                )
            }
        )
    }
}