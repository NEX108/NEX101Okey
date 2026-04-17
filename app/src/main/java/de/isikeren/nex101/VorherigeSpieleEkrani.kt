package de.isikeren.nex101

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.isikeren.nex101.ui.theme.NEX101Theme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.LinkedHashSet
import java.util.Locale

data class VorherigesSpielListeItem(
    val oyunId: Int,
    val tarihText: String,
    val saatText: String,
    val oyuncuText: String
)

@Composable
fun VorherigeSpieleEkrani(
    onGeriClick: () -> Unit,
    onOyunClick: (Int) -> Unit,
    onOyunSilClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val database = remember { DatabaseProvider.getDatabase(context) }
    val oyunDao = remember { database.oyunDao() }
    val oyunKatilimciDao = remember { database.oyunKatilimciDao() }
    val oyuncuDao = remember { database.oyuncuDao() }

    val prefs = remember {
        context.getSharedPreferences("vorherige_spiele_prefs", android.content.Context.MODE_PRIVATE)
    }
    val korunanOyunAnahtari = "korunan_oyunlar"
    BackHandler(onBack = onGeriClick)

    val bitenOyunlar by oyunDao.bitenOyunlariGetirFlow().collectAsState(initial = emptyList())
    val tumOyuncular by oyuncuDao.tumOyunculariGetir().collectAsState(initial = emptyList())

    val oncekiOyunlar by produceState<List<VorherigesSpielListeItem>>(
        initialValue = emptyList(),
        key1 = bitenOyunlar,
        key2 = tumOyuncular
    ) {
        val oyuncuMap = tumOyuncular.associateBy { it.id }

        value = bitenOyunlar.map { oyun ->
            val katilimcilar = oyunKatilimciDao.oyununKatilimcilariniGetir(oyun.id)
            val oyuncuAdlari = katilimcilar
                .sortedBy { it.pozisyon }
                .mapNotNull { katilimci -> oyuncuMap[katilimci.oyuncuId]?.ad }

            VorherigesSpielListeItem(
                oyunId = oyun.id,
                tarihText = oyun.bitisZamani?.let {
                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))
                } ?: oyun.baslangicZamani.let {
                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))
                },
                saatText = oyun.bitisZamani?.let {
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
                } ?: oyun.baslangicZamani.let {
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
                },
                oyuncuText = oyuncuAdlari.joinToString(", ")
            )
        }
    }
    var silinecekOyunId by remember { mutableStateOf<Int?>(null) }
    val seciliOyunlar = remember {
        mutableStateListOf<Int>().apply {
            val kayitliIdler = prefs.getStringSet(korunanOyunAnahtari, emptySet())
                .orEmpty()
                .mapNotNull { it.toIntOrNull() }
            addAll(kayitliIdler)
        }
    }

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
                    text = "Vorherige Spiele",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (oncekiOyunlar.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Noch keine beendeten Spiele vorhanden.",
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(oncekiOyunlar) { oyun ->
                        VorherigesSpielSatiri(
                            item = oyun,
                            secili = seciliOyunlar.contains(oyun.oyunId),
                            onClick = { onOyunClick(oyun.oyunId) },
                            onLongClick = {
                                if (seciliOyunlar.contains(oyun.oyunId)) {
                                    seciliOyunlar.remove(oyun.oyunId)
                                } else {
                                    seciliOyunlar.add(oyun.oyunId)
                                }
                                prefs.edit()
                                    .putStringSet(
                                        korunanOyunAnahtari,
                                        LinkedHashSet(seciliOyunlar.map { it.toString() })
                                    )
                                    .apply()
                            },
                            onSilClick = { silinecekOyunId = oyun.oyunId }
                        )
                    }
                }
            }
            silinecekOyunId?.let { oyunId ->
                AlertDialog(
                    onDismissRequest = { silinecekOyunId = null },
                    title = { Text("Spiel löschen") },
                    text = {
                        Text(
                            "Soll dieses Spiel wirklich gelöscht werden?\nDamit werden die Spiele aus der Statistik gelöscht"
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onOyunSilClick(oyunId)
                                seciliOyunlar.remove(oyunId)
                                prefs.edit()
                                    .putStringSet(
                                        korunanOyunAnahtari,
                                        LinkedHashSet(seciliOyunlar.map { it.toString() })
                                    )
                                    .apply()
                                silinecekOyunId = null
                            }
                        ) {
                            Text("Löschen")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { silinecekOyunId = null }
                        ) {
                            Text("Abbrechen")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VorherigesSpielSatiri(
    item: VorherigesSpielListeItem,
    secili: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSilClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (secili) androidx.compose.ui.graphics.Color(0x332E7D32) else MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.medium
            )
            .border(
                width = 2.dp,
                color = if (secili) androidx.compose.ui.graphics.Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.tarihText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (secili) androidx.compose.ui.graphics.Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
            )
            Text(
                text = item.saatText,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.oyuncuText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            if (secili) {
                Text(
                    text = "Geschützt",
                    style = MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = "Löschen",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.clickable(onClick = onSilClick)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun VorherigeSpieleEkraniPreview() {
    NEX101Theme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "←",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                Text(
                    text = "Vorherige Spiele",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            VorherigesSpielSatiri(
                item = VorherigesSpielListeItem(
                    oyunId = 1,
                    tarihText = "06.04.2026",
                    saatText = "19:45",
                    oyuncuText = "Eren, Semir, Erol, Eray"
                ),
                secili = false,
                onClick = {},
                onLongClick = {},
                onSilClick = {}
            )

            VorherigesSpielSatiri(
                item = VorherigesSpielListeItem(
                    oyunId = 2,
                    tarihText = "05.04.2026",
                    saatText = "21:10",
                    oyuncuText = "Eren, Semir, Erol, Eray"
                ),
                secili = true,
                onClick = {},
                onLongClick = {},
                onSilClick = {}
            )
        }
    }
}
