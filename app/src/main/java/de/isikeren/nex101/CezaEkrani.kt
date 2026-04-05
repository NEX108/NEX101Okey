
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.isikeren.nex101.ui.theme.NEX101Theme

@Composable
fun CezaEkrani(
    uiState: CezaEkraniUiState,
    onGeriClick: () -> Unit,
    onKaydetVeKapat: (CezaEkraniUiState) -> Unit,
    onSatiraEkle: (CezaEkraniUiState) -> Unit = {}
) {
    var localState by remember(uiState) { mutableStateOf(uiState.cezaKurallariniUygula()) }
    var hataMesaji by remember { mutableStateOf<String?>(null) }

    BackHandler(onBack = onGeriClick)

    fun localStateGuncelle(guncelleyici: (CezaEkraniUiState) -> CezaEkraniUiState) {
        localState = guncelleyici(localState).cezaKurallariniUygula()
    }

    fun secimHatasi(durum: CezaEkraniUiState): String? {
        val kirmiziVar = durum.hedefOyuncular.any { it.secimRolu == CezaSecimRolu.KIRMIZI }
        val yesilVar = durum.hedefOyuncular.any { it.secimRolu == CezaSecimRolu.YESIL }

        return when {
            durum.seciliCezaTipi == null -> "Hangi ceza?"
            !kirmiziVar -> "ceza kim yedi? (kirmizi)"
            cezaTipiYesilDesteklerMi(durum.seciliCezaTipi) && !yesilVar -> "cezayi kim yedirdi? (yesil)"
            else -> null
        }
    }

    fun bosCezaDurumu(durum: CezaEkraniUiState): CezaEkraniUiState {
        return CezaEkraniUiState(
            turNo = durum.turNo,
            mod = durum.mod,
            hedefOyuncular = durum.hedefOyuncular.map { it.copy(secimRolu = CezaSecimRolu.YOK) }
        )
    }

    fun kaydetmedenOnceKontrolEt(durum: CezaEkraniUiState, basariliysa: (CezaEkraniUiState) -> Unit) {
        val hata = secimHatasi(durum)
        if (hata != null) {
            hataMesaji = hata
        } else {
            basariliysa(durum)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        val kompakt = maxHeight < 780.dp
        val blokAralik = if (kompakt) 12.dp else 18.dp
        val oyuncuAralik = if (kompakt) 10.dp else 16.dp

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(blokAralik)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
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
                    text = "Runde ${localState.turNo} Ceza",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(oyuncuAralik)
                ) {
                    localState.hedefOyuncular.filterIndexed { index, _ -> index % 2 == 0 }.forEachIndexed { grupIndex, oyuncu ->
                        OyuncuSecimButonu(
                            modifier = Modifier.fillMaxWidth(),
                            oyuncu = oyuncu,
                            onClick = {
                                localStateGuncelle { durum ->
                                    durum.copy(
                                        hedefOyuncular = cezaOyuncuSeciminiGuncelle(
                                            oyuncular = durum.hedefOyuncular,
                                            tiklananGlobalIndex = grupIndex * 2,
                                            seciliCezaTipi = durum.seciliCezaTipi,
                                            mod = durum.mod
                                        )
                                    )
                                }
                            }
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(oyuncuAralik)
                ) {
                    localState.hedefOyuncular.filterIndexed { index, _ -> index % 2 == 1 }.forEachIndexed { grupIndex, oyuncu ->
                        OyuncuSecimButonu(
                            modifier = Modifier.fillMaxWidth(),
                            oyuncu = oyuncu,
                            onClick = {
                                localStateGuncelle { durum ->
                                    durum.copy(
                                        hedefOyuncular = cezaOyuncuSeciminiGuncelle(
                                            oyuncular = durum.hedefOyuncular,
                                            tiklananGlobalIndex = grupIndex * 2 + 1,
                                            seciliCezaTipi = durum.seciliCezaTipi,
                                            mod = durum.mod
                                        )
                                    )
                                }
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = localState.puanText,
                    onValueChange = {
                        localStateGuncelle { durum ->
                            durum.copy(
                                puanText = it.filter { ch -> ch.isDigit() || ch == '-' },
                                digerDegerText = if (durum.seciliCezaTipi == CezaTipi.DIGER) it.filter { ch -> ch.isDigit() || ch == '-' } else durum.digerDegerText
                            )
                        }
                    },
                    modifier = Modifier.width(220.dp),
                    singleLine = true,
                    label = { Text("Ceza") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (localState.seciliCezaTipi == CezaTipi.DIGER) KeyboardType.Number else KeyboardType.Text
                    )
                )

                Box(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(48.dp)
                        .border(2.dp, Color(0xFF2E7D32), MaterialTheme.shapes.medium)
                        .clickable {
                            kaydetmedenOnceKontrolEt(localState) { gecerliDurum ->
                                onSatiraEkle(gecerliDurum)
                                localState = bosCezaDurumu(gecerliDurum)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = Color(0xFF2E7D32),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (localState.seciliCezaTipi == CezaTipi.TAS_CEKILDI) {
                SayiSecimAlani(
                    seciliSayi = localState.tasDegeriText,
                    onSayiClick = { sayi ->
                        localStateGuncelle { durum ->
                            durum.copy(
                                tasDegeriText = sayi.toString(),
                                puanText = (sayi * 10).toString()
                            )
                        }
                    }
                )

                BuyukSecimButonu(
                    text = "tas cekildi",
                    onClick = {
                        localStateGuncelle { durum ->
                            durum.copy(
                                seciliCezaTipi = null,
                                puanText = "",
                                tasDegeriText = ""
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                CezaButonGrid(
                    seciliCezaTipi = localState.seciliCezaTipi,
                    onCezaTipiSec = { tip ->
                        localStateGuncelle { durum ->
                            durum.copy(seciliCezaTipi = tip)
                        }
                    }
                )
            }

            Button(
                onClick = {
                    kaydetmedenOnceKontrolEt(localState) { gecerliDurum ->
                        onKaydetVeKapat(gecerliDurum)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Speichern", modifier = Modifier.padding(vertical = 6.dp))
            }
        }
    }

    hataMesaji?.let { mesaj ->
        AlertDialog(
            onDismissRequest = { hataMesaji = null },
            title = { Text("Uyari") },
            text = { Text(mesaj) },
            confirmButton = {
                TextButton(onClick = { hataMesaji = null }) {
                    Text("OK")
                }
            }
        )
    }
}

private fun CezaEkraniUiState.cezaKurallariniUygula(): CezaEkraniUiState {
    val yeniOyuncular = cezaOyuncuSecimleriniNormalleştir(
        oyuncular = hedefOyuncular,
        seciliCezaTipi = seciliCezaTipi,
        mod = mod
    )

    return when (seciliCezaTipi) {
        CezaTipi.ISLEK_ATTI -> copy(hedefOyuncular = yeniOyuncular, puanText = "101", tasDegeriText = "", digerDegerText = "")
        CezaTipi.OKEY_ATTI -> copy(hedefOyuncular = yeniOyuncular, puanText = "101", tasDegeriText = "", digerDegerText = "")
        CezaTipi.ACAMADI_CEZA -> copy(hedefOyuncular = yeniOyuncular, puanText = "101", tasDegeriText = "", digerDegerText = "")
        CezaTipi.OKEY_ELDE_PATLADI -> copy(hedefOyuncular = yeniOyuncular, puanText = "101", tasDegeriText = "", digerDegerText = "")
        CezaTipi.OKEY_CALDIRDI -> copy(hedefOyuncular = yeniOyuncular, puanText = "101", tasDegeriText = "", digerDegerText = "")
        CezaTipi.TAS_CEKILDI -> {
            val sayi = tasDegeriText.toIntOrNull()
            copy(hedefOyuncular = yeniOyuncular, puanText = if (sayi != null) (sayi * 10).toString() else "")
        }
        CezaTipi.DIGER -> copy(
            hedefOyuncular = yeniOyuncular,
            puanText = digerDegerText.filter { it.isDigit() || it == '-' }
        )
        null -> copy(hedefOyuncular = yeniOyuncular)
    }
}

private fun cezaTipiYesilDesteklerMi(seciliCezaTipi: CezaTipi?): Boolean {
    return seciliCezaTipi == CezaTipi.TAS_CEKILDI || seciliCezaTipi == CezaTipi.OKEY_CALDIRDI
}

private fun ayniTakimMi(index1: Int, index2: Int, mod: String): Boolean {
    return if (mod == "ortak") {
        (index1 % 2) == (index2 % 2)
    } else {
        index1 == index2
    }
}

private fun cezaOyuncuSeciminiGuncelle(
    oyuncular: List<CezaOyuncuSecimi>,
    tiklananGlobalIndex: Int,
    seciliCezaTipi: CezaTipi?,
    mod: String
): List<CezaOyuncuSecimi> {
    if (tiklananGlobalIndex !in oyuncular.indices) return oyuncular

    val tiklanan = oyuncular[tiklananGlobalIndex]
    val yesilDestekleniyor = cezaTipiYesilDesteklerMi(seciliCezaTipi)

    val kirmiziIndex = oyuncular.indexOfFirst { it.secimRolu == CezaSecimRolu.KIRMIZI }
    val yesilIndex = oyuncular.indexOfFirst { it.secimRolu == CezaSecimRolu.YESIL }

    return when (tiklanan.secimRolu) {
        CezaSecimRolu.KIRMIZI -> {
            oyuncular.map { it.copy(secimRolu = CezaSecimRolu.YOK) }
        }

        CezaSecimRolu.YESIL -> {
            oyuncular.mapIndexed { index, oyuncu ->
                if (index == tiklananGlobalIndex) oyuncu.copy(secimRolu = CezaSecimRolu.YOK) else oyuncu
            }
        }

        CezaSecimRolu.YOK -> {
            when {
                kirmiziIndex == -1 -> {
                    oyuncular.mapIndexed { index, oyuncu ->
                        if (index == tiklananGlobalIndex) {
                            oyuncu.copy(secimRolu = CezaSecimRolu.KIRMIZI)
                        } else {
                            oyuncu.copy(secimRolu = CezaSecimRolu.YOK)
                        }
                    }
                }

                !yesilDestekleniyor -> {
                    oyuncular
                }

                yesilIndex != -1 -> {
                    oyuncular
                }

                ayniTakimMi(kirmiziIndex, tiklananGlobalIndex, mod) -> {
                    oyuncular
                }

                else -> {
                    oyuncular.mapIndexed { index, oyuncu ->
                        when {
                            index == kirmiziIndex -> oyuncu.copy(secimRolu = CezaSecimRolu.KIRMIZI)
                            index == tiklananGlobalIndex -> oyuncu.copy(secimRolu = CezaSecimRolu.YESIL)
                            else -> oyuncu.copy(secimRolu = CezaSecimRolu.YOK)
                        }
                    }
                }
            }
        }
    }
}

private fun cezaOyuncuSecimleriniNormalleştir(
    oyuncular: List<CezaOyuncuSecimi>,
    seciliCezaTipi: CezaTipi?,
    mod: String
): List<CezaOyuncuSecimi> {
    val yesilDestekleniyor = cezaTipiYesilDesteklerMi(seciliCezaTipi)
    val kirmiziIndex = oyuncular.indexOfFirst { it.secimRolu == CezaSecimRolu.KIRMIZI }
    val yesilIndex = oyuncular.indexOfFirst { it.secimRolu == CezaSecimRolu.YESIL }

    return oyuncular.mapIndexed { index, oyuncu ->
        when {
            kirmiziIndex == -1 && oyuncu.secimRolu == CezaSecimRolu.YESIL -> {
                oyuncu.copy(secimRolu = CezaSecimRolu.YOK)
            }
            oyuncu.secimRolu == CezaSecimRolu.YESIL && !yesilDestekleniyor -> {
                oyuncu.copy(secimRolu = CezaSecimRolu.YOK)
            }
            kirmiziIndex != -1 && yesilIndex != -1 && index == yesilIndex && ayniTakimMi(kirmiziIndex, yesilIndex, mod) -> {
                oyuncu.copy(secimRolu = CezaSecimRolu.YOK)
            }
            oyuncu.secimRolu == CezaSecimRolu.KIRMIZI && index != kirmiziIndex -> {
                oyuncu.copy(secimRolu = CezaSecimRolu.YOK)
            }
            oyuncu.secimRolu == CezaSecimRolu.YESIL && index != yesilIndex -> {
                oyuncu.copy(secimRolu = CezaSecimRolu.YOK)
            }
            else -> oyuncu
        }
    }
}

@Composable
private fun OyuncuSecimButonu(
    modifier: Modifier = Modifier,
    oyuncu: CezaOyuncuSecimi,
    onClick: () -> Unit
) {
    val takimRengi = Color(oyuncu.takimRengiArgb)
    val arkaPlanRengi = when (oyuncu.secimRolu) {
        CezaSecimRolu.KIRMIZI -> Color(0xFFC62828).copy(alpha = 0.22f)
        CezaSecimRolu.YESIL -> Color(0xFF2E7D32).copy(alpha = 0.22f)
        CezaSecimRolu.YOK -> Color.Transparent
    }

    Box(
        modifier = modifier
            .height(64.dp)
            .border(2.dp, takimRengi, MaterialTheme.shapes.medium)
            .background(
                arkaPlanRengi,
                MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = oyuncu.oyuncuAdi.ifBlank { "-" },
            color = takimRengi,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CezaButonGrid(
    seciliCezaTipi: CezaTipi?,
    onCezaTipiSec: (CezaTipi) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BuyukSecimButonu(
            text = CezaTipi.TAS_CEKILDI.gorunenAd,
            secili = seciliCezaTipi == CezaTipi.TAS_CEKILDI,
            onClick = { onCezaTipiSec(CezaTipi.TAS_CEKILDI) },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KucukSecimButonu(
                text = CezaTipi.ISLEK_ATTI.gorunenAd,
                secili = seciliCezaTipi == CezaTipi.ISLEK_ATTI,
                onClick = { onCezaTipiSec(CezaTipi.ISLEK_ATTI) },
                modifier = Modifier.weight(1f)
            )
            KucukSecimButonu(
                text = CezaTipi.OKEY_ATTI.gorunenAd,
                secili = seciliCezaTipi == CezaTipi.OKEY_ATTI,
                onClick = { onCezaTipiSec(CezaTipi.OKEY_ATTI) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KucukSecimButonu(
                text = CezaTipi.ACAMADI_CEZA.gorunenAd,
                secili = seciliCezaTipi == CezaTipi.ACAMADI_CEZA,
                onClick = { onCezaTipiSec(CezaTipi.ACAMADI_CEZA) },
                modifier = Modifier.weight(1f)
            )
            KucukSecimButonu(
                text = CezaTipi.OKEY_ELDE_PATLADI.gorunenAd,
                secili = seciliCezaTipi == CezaTipi.OKEY_ELDE_PATLADI,
                onClick = { onCezaTipiSec(CezaTipi.OKEY_ELDE_PATLADI) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KucukSecimButonu(
                text = CezaTipi.OKEY_CALDIRDI.gorunenAd,
                secili = seciliCezaTipi == CezaTipi.OKEY_CALDIRDI,
                onClick = { onCezaTipiSec(CezaTipi.OKEY_CALDIRDI) },
                modifier = Modifier.weight(1f)
            )
            KucukSecimButonu(
                text = CezaTipi.DIGER.gorunenAd,
                secili = seciliCezaTipi == CezaTipi.DIGER,
                onClick = { onCezaTipiSec(CezaTipi.DIGER) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SayiSecimAlani(
    seciliSayi: String,
    onSayiClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        listOf(
            listOf(1, 2, 3),
            listOf(4, 5, 6),
            listOf(7, 8, 9),
            listOf(10, 11, 12)
        ).forEach { satir ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(34.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                satir.forEach { sayi ->
                    SayiButonu(
                        sayi = sayi,
                        secili = seciliSayi == sayi.toString(),
                        onClick = { onSayiClick(sayi) }
                    )
                }
            }
        }
        SayiButonu(
            sayi = 13,
            secili = seciliSayi == "13",
            onClick = { onSayiClick(13) }
        )
    }
}

@Composable
private fun SayiButonu(
    sayi: Int,
    secili: Boolean,
    onClick: () -> Unit
) {
    val borderRenk = if (secili) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
    Box(
        modifier = Modifier
            .size(56.dp)
            .border(2.dp, borderRenk, MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = sayi.toString(),
            color = borderRenk,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun BuyukSecimButonu(
    text: String,
    secili: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(62.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (secili) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Text(text, textAlign = TextAlign.Center)
    }
}

@Composable
private fun KucukSecimButonu(
    text: String,
    secili: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(86.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (secili) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Text(text, textAlign = TextAlign.Center)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CezaEkraniPreview() {
    NEX101Theme {
        CezaEkrani(
            uiState = CezaEkraniUiState(
                turNo = 4,
                mod = "ortak",
                hedefOyuncular = listOf(
                    CezaOyuncuSecimi(oyuncuAdi = "Eren", takimRengiArgb = 0xFF81D4FAL, secimRolu = CezaSecimRolu.KIRMIZI),
                    CezaOyuncuSecimi(oyuncuAdi = "Semir", takimRengiArgb = 0xFFC62828L),
                    CezaOyuncuSecimi(oyuncuAdi = "Erol", takimRengiArgb = 0xFF81D4FAL, secimRolu = CezaSecimRolu.YESIL),
                    CezaOyuncuSecimi(oyuncuAdi = "Eray", takimRengiArgb = 0xFFC62828L)
                )
            ),
            onGeriClick = {},
            onKaydetVeKapat = {},
            onSatiraEkle = {}
        )
    }
}


