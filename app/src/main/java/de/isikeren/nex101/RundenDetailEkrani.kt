
package de.isikeren.nex101

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.isikeren.nex101.ui.theme.NEX101Theme

data class RundenDetailUiState(
    val turNo: Int,
    val takim1Adi: String,
    val takim2Adi: String,
    val takim1Puan: Int? = null,
    val takim2Puan: Int? = null,
    val oyuncu1Adi: String = "P1",
    val oyuncu2Adi: String = "P2",
    val oyuncu3Adi: String = "P3",
    val oyuncu4Adi: String = "P4",
    val oyuncu1Puan: Int? = null,
    val oyuncu2Puan: Int? = null,
    val oyuncu3Puan: Int? = null,
    val oyuncu4Puan: Int? = null,
    val oyuncu1SonucDetayi: String = "",
    val oyuncu2SonucDetayi: String = "",
    val oyuncu3SonucDetayi: String = "",
    val oyuncu4SonucDetayi: String = "",
    val cezaListesi: List<CezaKaydi> = emptyList()
)

@Composable
fun RundenDetailEkrani(
    uiState: RundenDetailUiState,
    onGeriClick: () -> Unit,
    onAyarlarClick: () -> Unit,
    onErgebnisClick: () -> Unit,
    onCezaClick: (CezaKaydi) -> Unit,
    onCezaSilClick: (CezaKaydi) -> Unit
) {
    BackHandler(onBack = onGeriClick)

    var silinecekCeza by remember { mutableStateOf<CezaKaydi?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 20.dp),
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
                text = "Runde ${uiState.turNo} Detail",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "⚙",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.clickable(onClick = onAyarlarClick)
            )
        }

        DetailBaslik(text = "Ergebnis")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.medium
                )
                .clickable(onClick = onErgebnisClick)
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TeamErgebnisBloku(
                    takimAdi = uiState.takim1Adi,
                    takimPuan = uiState.takim1Puan,
                    oyuncular = listOf(
                        OyuncuErgebnisSatiriUiState(
                            oyuncuAdi = uiState.oyuncu1Adi,
                            rundenSonuDetayi = uiState.oyuncu1SonucDetayi,
                            rundenSonuPuani = uiState.oyuncu1Puan ?: 0,
                            gercekSonuc = gercekOyuncuSonucu(uiState.oyuncu1Adi, uiState.oyuncu1Puan, uiState.cezaListesi)
                        ),
                        OyuncuErgebnisSatiriUiState(
                            oyuncuAdi = uiState.oyuncu3Adi,
                            rundenSonuDetayi = uiState.oyuncu3SonucDetayi,
                            rundenSonuPuani = uiState.oyuncu3Puan ?: 0,
                            gercekSonuc = gercekOyuncuSonucu(uiState.oyuncu3Adi, uiState.oyuncu3Puan, uiState.cezaListesi)
                        )
                    ),
                    onErgebnisClick = onErgebnisClick
                )

                Spacer(modifier = Modifier.height(14.dp))

                TeamErgebnisBloku(
                    takimAdi = uiState.takim2Adi,
                    takimPuan = uiState.takim2Puan,
                    oyuncular = listOf(
                        OyuncuErgebnisSatiriUiState(
                            oyuncuAdi = uiState.oyuncu2Adi,
                            rundenSonuDetayi = uiState.oyuncu2SonucDetayi,
                            rundenSonuPuani = uiState.oyuncu2Puan ?: 0,
                            gercekSonuc = gercekOyuncuSonucu(uiState.oyuncu2Adi, uiState.oyuncu2Puan, uiState.cezaListesi)
                        ),
                        OyuncuErgebnisSatiriUiState(
                            oyuncuAdi = uiState.oyuncu4Adi,
                            rundenSonuDetayi = uiState.oyuncu4SonucDetayi,
                            rundenSonuPuani = uiState.oyuncu4Puan ?: 0,
                            gercekSonuc = gercekOyuncuSonucu(uiState.oyuncu4Adi, uiState.oyuncu4Puan, uiState.cezaListesi)
                        )
                    ),
                    onErgebnisClick = onErgebnisClick
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        DetailBaslik(text = "Cezalar")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OyuncuDetayKolonu(
                modifier = Modifier.weight(1f),
                oyuncuAdi = uiState.oyuncu1Adi,
                toplamCeza = uiState.cezaListesi
                    .filter { it.kirmiziOyuncuAdi == uiState.oyuncu1Adi }
                    .sumOf { it.puan },
                cezaListesi = uiState.cezaListesi.filter { it.kirmiziOyuncuAdi == uiState.oyuncu1Adi },
                onCezaClick = onCezaClick,
                onCezaSilClick = { silinecekCeza = it }
            )
            OyuncuDetayKolonu(
                modifier = Modifier.weight(1f),
                oyuncuAdi = uiState.oyuncu2Adi,
                toplamCeza = uiState.cezaListesi
                    .filter { it.kirmiziOyuncuAdi == uiState.oyuncu2Adi }
                    .sumOf { it.puan },
                cezaListesi = uiState.cezaListesi.filter { it.kirmiziOyuncuAdi == uiState.oyuncu2Adi },
                onCezaClick = onCezaClick,
                onCezaSilClick = { silinecekCeza = it }
            )
            OyuncuDetayKolonu(
                modifier = Modifier.weight(1f),
                oyuncuAdi = uiState.oyuncu3Adi,
                toplamCeza = uiState.cezaListesi
                    .filter { it.kirmiziOyuncuAdi == uiState.oyuncu3Adi }
                    .sumOf { it.puan },
                cezaListesi = uiState.cezaListesi.filter { it.kirmiziOyuncuAdi == uiState.oyuncu3Adi },
                onCezaClick = onCezaClick,
                onCezaSilClick = { silinecekCeza = it }
            )
            OyuncuDetayKolonu(
                modifier = Modifier.weight(1f),
                oyuncuAdi = uiState.oyuncu4Adi,
                toplamCeza = uiState.cezaListesi
                    .filter { it.kirmiziOyuncuAdi == uiState.oyuncu4Adi }
                    .sumOf { it.puan },
                cezaListesi = uiState.cezaListesi.filter { it.kirmiziOyuncuAdi == uiState.oyuncu4Adi },
                onCezaClick = onCezaClick,
                onCezaSilClick = { silinecekCeza = it }
            )
        }
        silinecekCeza?.let { ceza ->
            AlertDialog(
                onDismissRequest = { silinecekCeza = null },
                title = { Text("Ceza löschen") },
                text = { Text("Wollen Sie diese Ceza löschen?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onCezaSilClick(ceza)
                            silinecekCeza = null
                        }
                    ) {
                        Text("Löschen")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { silinecekCeza = null }
                    ) {
                        Text("Abbrechen")
                    }
                }
            )
        }
    }
}

@Composable
private fun DetailBaslik(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun TeamErgebnisBloku(
    takimAdi: String,
    takimPuan: Int?,
    oyuncular: List<OyuncuErgebnisSatiriUiState>,
    onErgebnisClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = takimAdi,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = sonucDegerMetni(takimPuan ?: 0, oyuncular.sumOf { it.gercekSonuc }),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        oyuncular.forEachIndexed { index, oyuncu ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onErgebnisClick)
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = oyuncuDetaySatiri(oyuncu.oyuncuAdi, oyuncu.rundenSonuDetayi))
                Text(text = sonucDegerMetni(oyuncu.rundenSonuPuani, oyuncu.gercekSonuc))
            }
            if (index != oyuncular.lastIndex) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

data class OyuncuErgebnisSatiriUiState(
    val oyuncuAdi: String,
    val rundenSonuDetayi: String,
    val rundenSonuPuani: Int,
    val gercekSonuc: Int
)

private fun gercekOyuncuSonucu(
    oyuncuAdi: String,
    oyuncuPuan: Int?,
    cezaListesi: List<CezaKaydi>
): Int {
    return (oyuncuPuan ?: 0) + cezaListesi
        .filter { it.kirmiziOyuncuAdi == oyuncuAdi }
        .sumOf { it.puan }
}


private fun sonucDegerMetni(
    rundenSonuPuani: Int,
    gercekSonuc: Int
): String {
    return "$rundenSonuPuani ($gercekSonuc)"
}

private fun oyuncuDetaySatiri(
    oyuncuAdi: String,
    rundenSonuDetayi: String
): String {
    return if (rundenSonuDetayi.isBlank()) {
        oyuncuAdi
    } else {
        "$oyuncuAdi ($rundenSonuDetayi)"
    }
}


@Composable
private fun OyuncuDetayKolonu(
    modifier: Modifier = Modifier,
    oyuncuAdi: String,
    toplamCeza: Int,
    cezaListesi: List<CezaKaydi>,
    onCezaClick: (CezaKaydi) -> Unit,
    onCezaSilClick: (CezaKaydi) -> Unit
) {
    Column(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium
            )
            .padding(6.dp)
    ) {
        Text(
            text = oyuncuAdi,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (cezaListesi.isEmpty()) "0" else toplamCeza.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            if (cezaListesi.isEmpty()) {
                Text(
                    text = "-",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    cezaListesi.forEach { ceza ->
                        CezaDetaySatiri(
                            ceza = ceza,
                            onClick = { onCezaClick(ceza) },
                            onLongClick = { onCezaSilClick(ceza) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CezaDetaySatiri(
    ceza: CezaKaydi,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                shape = MaterialTheme.shapes.small
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(6.dp)
    ) {
        Text(
            text = ceza.cezaTipi.gorunenAd,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        if (ceza.yesilOyuncuAdi != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = ceza.yesilOyuncuAdi,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = ceza.puan.toString(),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RundenDetailEkraniPreview() {
    NEX101Theme {
        Surface {
            RundenDetailEkrani(
                uiState = RundenDetailUiState(
                    turNo = 4,
                    takim1Adi = "Mühendis",
                    takim2Adi = "Gay",
                    takim1Puan = 120,
                    takim2Puan = 242,
                    oyuncu1Adi = "Eren",
                    oyuncu2Adi = "Semir",
                    oyuncu3Adi = "Erol",
                    oyuncu4Adi = "Eray",
                    oyuncu1Puan = -101,
                    oyuncu2Puan = 70,
                    oyuncu3Puan = 0,
                    oyuncu4Puan = 172,
                    oyuncu1SonucDetayi = "bitti, okeyle",
                    oyuncu2SonucDetayi = "",
                    oyuncu3SonucDetayi = "partner",
                    oyuncu4SonucDetayi = "acamadi",
                    cezaListesi = listOf(
                        CezaKaydi(
                            id = 1,
                            turNo = 4,
                            cezaTipi = CezaTipi.TAS_CEKILDI,
                            puan = 50,
                            kirmiziOyuncuAdi = "Eren",
                            kirmiziTakimRengiArgb = 0xFF81D4FAL,
                            yesilOyuncuAdi = "Semir",
                            yesilTakimRengiArgb = 0xFFC62828L
                        ),
                        CezaKaydi(
                            id = 2,
                            turNo = 4,
                            cezaTipi = CezaTipi.OKEY_ATTI,
                            puan = 101,
                            kirmiziOyuncuAdi = "Eray",
                            kirmiziTakimRengiArgb = 0xFFC62828L
                        ),
                        CezaKaydi(
                            id = 3,
                            turNo = 4,
                            cezaTipi = CezaTipi.DIGER,
                            puan = 70,
                            kirmiziOyuncuAdi = "Semir",
                            kirmiziTakimRengiArgb = 0xFFC62828L
                        )
                    )
                ),
                onGeriClick = {},
                onAyarlarClick = {},
                onErgebnisClick = {},
                onCezaClick = {},
                onCezaSilClick = {}
            )
        }
    }
}

