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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.isikeren.nex101.ui.theme.NEX101Theme

data class OyuncuIstatistikOzet(
    val oyuncuId: Int,
    val oyuncuAdi: String,
    val oyunSayisi: Int,
    val turSayisi: Int,
    val toplamPuan: Int
)

@Composable
fun StatistikEkrani(
    onGeriClick: () -> Unit,
    onOyuncuClick: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val database = remember { DatabaseProvider.getDatabase(context) }
    val oyuncuDao = remember { database.oyuncuDao() }
    val oyunKatilimciDao = remember { database.oyunKatilimciDao() }
    val turOyuncuSonucDao = remember { database.turOyuncuSonucDao() }
    val cezaDao = remember { database.cezaDao() }
    BackHandler(onBack = onGeriClick)

    val oyuncular by oyuncuDao.tumOyunculariGetir().collectAsState(initial = emptyList())

    val istatistikler by produceState<List<OyuncuIstatistikOzet>>(
        initialValue = emptyList(),
        key1 = oyuncular
    ) {
        value = oyuncular.map { oyuncu ->
            val oyunSayisi = oyunKatilimciDao.oyuncununOyunSayisiniGetir(oyuncu.id)
            val turSayisi = turOyuncuSonucDao.oyuncununOynadigiTurSayisiniGetir(oyuncu.id)
            val turSonuclari = turOyuncuSonucDao.oyuncununTumTurSonuclariniGetir(oyuncu.id)
            val turPuanToplami = turSonuclari.sumOf { it.sonucPuani }
            val cezaPuanToplami = cezaDao.oyuncununCezaPuanToplaminiGetir(oyuncu.id)

            OyuncuIstatistikOzet(
                oyuncuId = oyuncu.id,
                oyuncuAdi = oyuncu.ad,
                oyunSayisi = oyunSayisi,
                turSayisi = turSayisi,
                toplamPuan = turPuanToplami + cezaPuanToplami
            )
        }.sortedWith(
            compareBy<OyuncuIstatistikOzet> { it.turSayisi == 0 }
                .thenBy { if (it.turSayisi == 0) Double.POSITIVE_INFINITY else it.toplamPuan.toDouble() / it.turSayisi.toDouble() }
                .thenBy { it.oyuncuAdi.lowercase() }
        )
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            StatistikBaslik(onGeriClick = onGeriClick)

            Spacer(modifier = Modifier.height(16.dp))

            StatistikTabloBaslik()

            Spacer(modifier = Modifier.height(8.dp))

            if (istatistikler.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Noch keine Statistikdaten vorhanden.",
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(istatistikler) { item ->
                        OyuncuStatistikSatiri(
                            item = item,
                            onClick = { onOyuncuClick(item.oyuncuId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatistikBaslik(
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
            text = "Statistiken",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun StatistikTabloBaslik() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatistikHucre(
            text = "Spieler",
            modifier = Modifier.weight(1f),
            bold = true,
            textAlign = TextAlign.Start
        )
        StatistikHucre(
            text = "Spiele",
            modifier = Modifier.width(68.dp),
            bold = true
        )
        StatistikHucre(
            text = "Runden",
            modifier = Modifier.width(74.dp),
            bold = true
        )
        StatistikHucre(
            text = "Pkt/Runde",
            modifier = Modifier.width(86.dp),
            bold = true
        )
    }
}

@Composable
private fun OyuncuStatistikSatiri(
    item: OyuncuIstatistikOzet,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.oyuncuAdi,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Tippen für mehr",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
            )
        }

        StatistikHucre(
            text = item.oyunSayisi.toString(),
            modifier = Modifier.width(68.dp)
        )
        StatistikHucre(
            text = item.turSayisi.toString(),
            modifier = Modifier.width(74.dp)
        )
        StatistikHucre(
            text = if (item.turSayisi == 0) "-" else String.format(java.util.Locale.US, "%.2f", item.toplamPuan.toDouble() / item.turSayisi.toDouble()),
            modifier = Modifier.width(86.dp),
            bold = true
        )
    }
}

@Composable
private fun StatistikHucre(
    text: String,
    modifier: Modifier = Modifier,
    bold: Boolean = false,
    textAlign: TextAlign = TextAlign.Center
) {
    Box(
        modifier = modifier.padding(horizontal = 4.dp),
        contentAlignment = when (textAlign) {
            TextAlign.Start -> Alignment.CenterStart
            TextAlign.End -> Alignment.CenterEnd
            else -> Alignment.Center
        }
    ) {
        Text(
            text = text,
            textAlign = textAlign,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StatistikEkraniPreview() {
    NEX101Theme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            StatistikBaslik(onGeriClick = {})
            Spacer(modifier = Modifier.height(16.dp))
            StatistikTabloBaslik()
            Spacer(modifier = Modifier.height(8.dp))
            OyuncuStatistikSatiri(
                item = OyuncuIstatistikOzet(
                    oyuncuId = 1,
                    oyuncuAdi = "Eren",
                    oyunSayisi = 12,
                    turSayisi = 84,
                    toplamPuan = 252
                ),
                onClick = {}
            )
            Spacer(modifier = Modifier.height(8.dp))
            OyuncuStatistikSatiri(
                item = OyuncuIstatistikOzet(
                    oyuncuId = 2,
                    oyuncuAdi = "Semir",
                    oyunSayisi = 10,
                    turSayisi = 73,
                    toplamPuan = 365
                ),
                onClick = {}
            )
        }
    }
}
