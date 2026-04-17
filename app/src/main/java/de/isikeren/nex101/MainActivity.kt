package de.isikeren.nex101

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import de.isikeren.nex101.ui.theme.NEX101Theme

private enum class Ekran {
    NEX_INTRO,
    ANA_SAYFA,
    YENI_OYUN,
    OYUNCU_YONETIM,
    OYUN_DETAY,
    RUNDE_BEENDEN,
    CEZA,
    RUNDE_DETAY,
    VORHERIGE_SPIELE,
    VORHERIGES_OYUN_DETAY,
    VORHERIGES_RUNDE_DETAY,
    STATISTIK,
    OYUNCU_STATISTIK,
    OYUNCU_STATISTIK_TUR_LISTE,
    OYUNCU_STATISTIK_OYUNLAR_LISTE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        setContent {
            NEX101Theme {
                val db = remember { DatabaseProvider.getDatabase(applicationContext) }
                val coroutineScope = rememberCoroutineScope()
                var aktifEkran by rememberSaveable { mutableStateOf(Ekran.NEX_INTRO) }
                var aktifOyunId by rememberSaveable { mutableStateOf<Int?>(null) }
                var aktifOyunOzeti by remember { mutableStateOf<OyunBaslangicOzeti?>(null) }
                var rundeBeendenUiState by remember { mutableStateOf<RundeBeendenUiState?>(null) }
                var cezaUiState by remember { mutableStateOf<CezaEkraniUiState?>(null) }
                var duzenlenenCezaKaydi by remember { mutableStateOf<CezaKaydi?>(null) }
                var seciliTurDetayNo by remember { mutableStateOf<Int?>(null) }
                var seciliVorherigesOyunId by remember { mutableStateOf<Int?>(null) }
                var seciliVorherigeTurNo by remember { mutableStateOf<Int?>(null) }
                var seciliIstatistikOyuncuId by remember { mutableStateOf<Int?>(null) }
                var seciliTurFiltreTipi by remember { mutableStateOf<OyuncuTurFiltreTipi?>(null) }
                val aktifRundenListe = remember { mutableStateListOf<Int>() }
                var aktifTurNoDurumu by remember { mutableStateOf<Int?>(null) }
                val ortakTurSonuclari = remember { mutableStateMapOf<Int, Pair<Int, Int>>() }
                val cezaKayitlari = remember { mutableStateMapOf<Int, MutableList<CezaKaydi>>() }
                var yeniOyunTaslagi by remember { mutableStateOf(YeniOyunTaslakDurumu()) }
                val tumOyuncular by db.oyuncuDao().tumOyunculariGetir().collectAsState(initial = emptyList())

                when (aktifEkran) {
                    Ekran.NEX_INTRO -> NexIntroScreenAnimated(
                        onFinished = { aktifEkran = Ekran.ANA_SAYFA }
                    )

                    Ekran.ANA_SAYFA -> AnaSayfa(
                        oyunDevamEdiyor = aktifOyunId != null,
                        onYeniOyunClick = { aktifEkran = Ekran.YENI_OYUN },
                        onOyuncuYonetClick = { aktifEkran = Ekran.OYUNCU_YONETIM },
                        onOyunuFortsetzenClick = { aktifEkran = Ekran.OYUN_DETAY },
                        onVorherigeSpieleClick = { aktifEkran = Ekran.VORHERIGE_SPIELE },
                        onStatistikClick = { aktifEkran = Ekran.STATISTIK }
                    )

                    Ekran.YENI_OYUN -> YeniOyunEkrani(
                        taslakDurumu = yeniOyunTaslagi,
                        onTaslakDurumuDegisti = { yeniOyunTaslagi = it },
                        onGeriClick = { aktifEkran = Ekran.ANA_SAYFA },
                        onOyunBasladi = { oyunOzeti ->
                            aktifOyunId = oyunOzeti.oyunId
                            aktifOyunOzeti = oyunOzeti
                            aktifRundenListe.clear()
                            ortakTurSonuclari.clear()
                            aktifTurNoDurumu = null
                            cezaUiState = null
                            cezaKayitlari.clear()
                            seciliTurDetayNo = null
                            duzenlenenCezaKaydi = null
                            aktifEkran = Ekran.OYUN_DETAY
                        }
                    )

                    Ekran.OYUNCU_YONETIM -> OyuncuYonetimEkrani(
                        onGeriClick = { aktifEkran = Ekran.ANA_SAYFA }
                    )

                    Ekran.OYUN_DETAY -> OyunDetayEkrani(
                        oyunId = aktifOyunId ?: 0,
                        baslangicOzeti = aktifOyunOzeti,
                        onGeriClick = { aktifEkran = Ekran.ANA_SAYFA },
                        onRundeBeendenClick = { turNo ->
                            rundeBeendenUiState = RundeBeendenUiState(
                                turNo = turNo,
                                mod = aktifOyunOzeti?.mod ?: "ortak",
                                oyuncu1 = SpielerRundenEndeUiState(
                                    spielerId = aktifOyunOzeti?.oyuncu1Id,
                                    spielerName = aktifOyunOzeti?.oyuncu1Adi ?: "P1",
                                    multiplikatorText = "×1"
                                ),
                                oyuncu2 = SpielerRundenEndeUiState(
                                    spielerId = aktifOyunOzeti?.oyuncu2Id,
                                    spielerName = aktifOyunOzeti?.oyuncu2Adi ?: "P2",
                                    multiplikatorText = "×1"
                                ),
                                oyuncu3 = SpielerRundenEndeUiState(
                                    spielerId = aktifOyunOzeti?.oyuncu3Id,
                                    spielerName = aktifOyunOzeti?.oyuncu3Adi ?: "P3",
                                    multiplikatorText = "×1"
                                ),
                                oyuncu4 = SpielerRundenEndeUiState(
                                    spielerId = aktifOyunOzeti?.oyuncu4Id,
                                    spielerName = aktifOyunOzeti?.oyuncu4Adi ?: "P4",
                                    multiplikatorText = "×1"
                                )
                            )
                            aktifEkran = Ekran.RUNDE_BEENDEN
                        },
                        onCezaClick = { turNo ->
                            duzenlenenCezaKaydi = null
                            cezaUiState = CezaEkraniUiState(
                                turNo = turNo,
                                mod = aktifOyunOzeti?.mod ?: "ortak",
                                hedefOyuncular = listOf(
                                    CezaOyuncuSecimi(
                                        oyuncuId = aktifOyunOzeti?.oyuncu1Id,
                                        oyuncuAdi = aktifOyunOzeti?.oyuncu1Adi ?: "P1",
                                        takimRengiArgb = 0xFF81D4FAL
                                    ),
                                    CezaOyuncuSecimi(
                                        oyuncuId = aktifOyunOzeti?.oyuncu2Id,
                                        oyuncuAdi = aktifOyunOzeti?.oyuncu2Adi ?: "P2",
                                        takimRengiArgb = 0xFFC62828L
                                    ),
                                    CezaOyuncuSecimi(
                                        oyuncuId = aktifOyunOzeti?.oyuncu3Id,
                                        oyuncuAdi = aktifOyunOzeti?.oyuncu3Adi ?: "P3",
                                        takimRengiArgb = 0xFF81D4FAL
                                    ),
                                    CezaOyuncuSecimi(
                                        oyuncuId = aktifOyunOzeti?.oyuncu4Id,
                                        oyuncuAdi = aktifOyunOzeti?.oyuncu4Adi ?: "P4",
                                        takimRengiArgb = 0xFFC62828L
                                    )
                                )
                            )
                            aktifEkran = Ekran.CEZA
                        },
                        onRundenDetayClick = { turNo ->
                            seciliTurDetayNo = turNo
                            aktifEkran = Ekran.RUNDE_DETAY
                        },
                        onOyunBitirildiClick = {
                            val oyunId = aktifOyunId
                            val bitisZamani = System.currentTimeMillis()

                            coroutineScope.launch {
                                if (oyunId != null) {
                                    withContext(Dispatchers.IO) {
                                        db.oyunDao().oyunDurumunuGuncelle(
                                            oyunId = oyunId,
                                            durum = "bitti",
                                            bitisZamani = bitisZamani
                                        )
                                    }
                                }
                            }

                            aktifOyunId = null
                            aktifOyunOzeti = null
                            aktifTurNoDurumu = null
                            rundeBeendenUiState = null
                            cezaUiState = null
                            duzenlenenCezaKaydi = null
                            seciliTurDetayNo = null
                            aktifRundenListe.clear()
                            ortakTurSonuclari.clear()
                            cezaKayitlari.clear()
                            aktifEkran = Ekran.ANA_SAYFA
                        },
                        onTurSilClick = { turNo ->
                            val oyunId = aktifOyunId

                            coroutineScope.launch {
                                if (oyunId != null) {
                                    withContext(Dispatchers.IO) {
                                        db.turDao().oyunVeTurNoIleTurSil(oyunId, turNo)
                                    }
                                }
                            }

                            val silinenIndex = aktifRundenListe.indexOf(turNo)
                            if (silinenIndex != -1) {
                                aktifRundenListe.removeAt(silinenIndex)
                            }
                            ortakTurSonuclari.remove(turNo)
                            cezaKayitlari.remove(turNo)

                            if (aktifTurNoDurumu == turNo) {
                                aktifTurNoDurumu = null
                            }
                            if (seciliTurDetayNo == turNo) {
                                seciliTurDetayNo = null
                                if (aktifEkran == Ekran.RUNDE_DETAY) {
                                    aktifEkran = Ekran.OYUN_DETAY
                                }
                            }
                        },
                        onTakimAdlariDegisti = { yeniTakim1, yeniTakim2 ->
                            aktifOyunOzeti = aktifOyunOzeti?.copy(
                                takim1Adi = yeniTakim1,
                                takim2Adi = yeniTakim2
                            )

                            val oyunId = aktifOyunId
                            if (oyunId != null) {
                                coroutineScope.launch {
                                    withContext(Dispatchers.IO) {
                                        db.oyunKatilimciDao().takimAdiniGuncelle(
                                            oyunId = oyunId,
                                            takimNo = 1,
                                            takimAdi = yeniTakim1
                                        )
                                        db.oyunKatilimciDao().takimAdiniGuncelle(
                                            oyunId = oyunId,
                                            takimNo = 2,
                                            takimAdi = yeniTakim2
                                        )
                                    }
                                }
                            }
                        },
                        rundenListe = aktifRundenListe,
                        aktifTurNo = aktifTurNoDurumu,
                        onAktifTurNoChange = { aktifTurNoDurumu = it },
                        ortakTurSonuclari = ortakTurSonuclari,
                        cezaKayitlari = cezaKayitlari
                    )

                    Ekran.RUNDE_BEENDEN -> {
                        val uiState = rundeBeendenUiState
                        if (uiState != null) {
                            RundeBeendenEkrani(
                                uiState = uiState,
                                onGeriClick = { aktifEkran = Ekran.OYUN_DETAY },
                                onSpeichernClick = { uiState ->
                                    coroutineScope.launch {
                                        val oyunId = aktifOyunId ?: return@launch
                                        val kaydedilecekUiState = uiState.kimlikleriTamamla(aktifOyunOzeti)
                                        if (!kaydedilecekUiState.tumOyuncuKimlikleriHazir()) {
                                            rundeBeendenUiState = kaydedilecekUiState
                                            return@launch
                                        }
                                        val kayitZamani = System.currentTimeMillis()

                                        withContext(Dispatchers.IO) {
                                            val mevcutTur = db.turDao().oyunVeTurNoIleTurGetir(oyunId, kaydedilecekUiState.turNo)
                                            val turId = if (mevcutTur != null) {
                                                db.turDao().turGuncelle(
                                                    mevcutTur.copy(
                                                        durum = "bitti",
                                                        bitisZamani = kayitZamani
                                                    )
                                                )
                                                mevcutTur.id
                                            } else {
                                                db.turDao().turEkle(
                                                    TurEntity(
                                                        oyunId = oyunId,
                                                        turNo = kaydedilecekUiState.turNo,
                                                        durum = "bitti",
                                                        olusturmaZamani = kayitZamani,
                                                        bitisZamani = kayitZamani
                                                    )
                                                ).toInt()
                                            }

                                            val mevcutSonuclar = db.turOyuncuSonucDao().turunOyuncuSonuclariniGetirListe(turId)
                                            mevcutSonuclar.forEach { sonuc ->
                                                db.turOyuncuSonucDao().turOyuncuSonucuSil(sonuc)
                                            }

                                            db.turOyuncuSonucDao().turOyuncuSonuclariniEkle(
                                                listOf(
                                                    kaydedilecekUiState.oyuncu1.toTurOyuncuSonucEntity(turId = turId, pozisyon = 1, mod = kaydedilecekUiState.mod),
                                                    kaydedilecekUiState.oyuncu2.toTurOyuncuSonucEntity(turId = turId, pozisyon = 2, mod = kaydedilecekUiState.mod),
                                                    kaydedilecekUiState.oyuncu3.toTurOyuncuSonucEntity(turId = turId, pozisyon = 3, mod = kaydedilecekUiState.mod),
                                                    kaydedilecekUiState.oyuncu4.toTurOyuncuSonucEntity(turId = turId, pozisyon = 4, mod = kaydedilecekUiState.mod)
                                                )
                                            )
                                        }

                                        rundeBeendenUiState = kaydedilecekUiState
                                        if (kaydedilecekUiState.mod == "ortak") {
                                            ortakTurSonuclari[kaydedilecekUiState.turNo] = ortakTakimToplamlariniHesapla(kaydedilecekUiState)
                                        }
                                        aktifTurNoDurumu = null
                                        aktifEkran = Ekran.OYUN_DETAY
                                    }
                                }
                            )
                        }
                    }

                    Ekran.CEZA -> {
                        val uiState = cezaUiState
                        if (uiState != null) {
                            CezaEkrani(
                                uiState = uiState,
                                onGeriClick = {
                                    duzenlenenCezaKaydi = null
                                    aktifEkran = if (uiState.duzenlenenCezaId != null) Ekran.RUNDE_DETAY else Ekran.OYUN_DETAY
                                },
                                onKaydetVeKapat = { gelenUiState ->
                                    val kaydedilecekUiState = gelenUiState.kimlikleriTamamla(aktifOyunOzeti)
                                    if (!kaydedilecekUiState.seciliKimliklerHazir()) {
                                        cezaUiState = kaydedilecekUiState
                                    } else {
                                        val kayit = kaydetVeyaGuncelleCeza(kaydedilecekUiState, cezaKayitlari)
                                        if (kayit != null) {
                                            coroutineScope.launch {
                                                val oyunId = aktifOyunId ?: return@launch
                                                withContext(Dispatchers.IO) {
                                                    kaydetVeyaGuncelleCezaRoom(
                                                        db = db,
                                                        oyunId = oyunId,
                                                        kayit = kayit,
                                                        guncelListe = cezaKayitlari[kaydedilecekUiState.turNo].orEmpty()
                                                    )
                                                }
                                            }
                                        }
                                        duzenlenenCezaKaydi = null
                                        cezaUiState = kaydedilecekUiState.sifirlanmisKopya()
                                        aktifEkran = if (kaydedilecekUiState.duzenlenenCezaId != null) Ekran.RUNDE_DETAY else Ekran.OYUN_DETAY
                                    }
                                },
                                onSatiraEkle = { gelenUiState ->
                                    val kaydedilecekUiState = gelenUiState.kimlikleriTamamla(aktifOyunOzeti)
                                    if (!kaydedilecekUiState.seciliKimliklerHazir()) {
                                        cezaUiState = kaydedilecekUiState
                                    } else {
                                        val kayit = kaydetVeyaGuncelleCeza(kaydedilecekUiState, cezaKayitlari)
                                        if (kayit != null) {
                                            coroutineScope.launch {
                                                val oyunId = aktifOyunId ?: return@launch
                                                withContext(Dispatchers.IO) {
                                                    kaydetVeyaGuncelleCezaRoom(
                                                        db = db,
                                                        oyunId = oyunId,
                                                        kayit = kayit,
                                                        guncelListe = cezaKayitlari[kaydedilecekUiState.turNo].orEmpty()
                                                    )
                                                }
                                            }
                                        }
                                        duzenlenenCezaKaydi = null
                                        cezaUiState = kaydedilecekUiState.sifirlanmisKopya()
                                    }
                                }
                            )
                        }
                    }

                    Ekran.RUNDE_DETAY -> {
                        val turNo = seciliTurDetayNo
                        if (turNo != null) {
                            val takimSonuclari = ortakTurSonuclari[turNo]
                            RundenDetailEkrani(
                                uiState = RundenDetailUiState(
                                    turNo = turNo,
                                    takim1Adi = aktifOyunOzeti?.takim1Adi ?: "Team 1",
                                    takim2Adi = aktifOyunOzeti?.takim2Adi ?: "Team 2",
                                    takim1Puan = takimSonuclari?.first,
                                    takim2Puan = takimSonuclari?.second,
                                    oyuncu1Adi = aktifOyunOzeti?.oyuncu1Adi ?: "P1",
                                    oyuncu2Adi = aktifOyunOzeti?.oyuncu2Adi ?: "P2",
                                    oyuncu3Adi = aktifOyunOzeti?.oyuncu3Adi ?: "P3",
                                    oyuncu4Adi = aktifOyunOzeti?.oyuncu4Adi ?: "P4",
                                    oyuncu1Puan = 0,
                                    oyuncu2Puan = 0,
                                    oyuncu3Puan = 0,
                                    oyuncu4Puan = 0,
                                    oyuncu1SonucDetayi = "",
                                    oyuncu2SonucDetayi = "",
                                    oyuncu3SonucDetayi = "",
                                    oyuncu4SonucDetayi = "",
                                    cezaListesi = cezaKayitlari[turNo].orEmpty()
                                ),
                                onGeriClick = { aktifEkran = Ekran.OYUN_DETAY },
                                onAyarlarClick = {},
                                onErgebnisClick = {
                                    val oyunId = aktifOyunId ?: return@RundenDetailEkrani
                                    coroutineScope.launch {
                                        val duzenlemeUiState = withContext(Dispatchers.IO) {
                                            val tur = db.turDao().oyunVeTurNoIleTurGetir(oyunId, turNo) ?: return@withContext null
                                            val sonuclar = db.turOyuncuSonucDao().turunOyuncuSonuclariniGetirListe(tur.id)
                                            val sonucMap = sonuclar.associateBy { it.pozisyon }
                                            val mod = aktifOyunOzeti?.mod ?: "ortak"

                                            RundeBeendenUiState(
                                                turNo = turNo,
                                                mod = mod,
                                                oyuncu1 = sonucMap[1].toSpielerRundenEndeUiState(
                                                    fallbackId = aktifOyunOzeti?.oyuncu1Id,
                                                    fallbackAd = aktifOyunOzeti?.oyuncu1Adi ?: "P1"
                                                ),
                                                oyuncu2 = sonucMap[2].toSpielerRundenEndeUiState(
                                                    fallbackId = aktifOyunOzeti?.oyuncu2Id,
                                                    fallbackAd = aktifOyunOzeti?.oyuncu2Adi ?: "P2"
                                                ),
                                                oyuncu3 = sonucMap[3].toSpielerRundenEndeUiState(
                                                    fallbackId = aktifOyunOzeti?.oyuncu3Id,
                                                    fallbackAd = aktifOyunOzeti?.oyuncu3Adi ?: "P3"
                                                ),
                                                oyuncu4 = sonucMap[4].toSpielerRundenEndeUiState(
                                                    fallbackId = aktifOyunOzeti?.oyuncu4Id,
                                                    fallbackAd = aktifOyunOzeti?.oyuncu4Adi ?: "P4"
                                                )
                                            )
                                        }

                                        if (duzenlemeUiState != null) {
                                            rundeBeendenUiState = duzenlemeUiState
                                            aktifEkran = Ekran.RUNDE_BEENDEN
                                        }
                                    }
                                },
                                onCezaClick = { ceza ->
                                    duzenlenenCezaKaydi = ceza
                                    cezaUiState = ceza.toCezaEkraniUiState(
                                        mod = aktifOyunOzeti?.mod ?: "ortak",
                                        hedefOyuncular = listOf(
                                            CezaOyuncuSecimi(
                                                oyuncuId = aktifOyunOzeti?.oyuncu1Id,
                                                oyuncuAdi = aktifOyunOzeti?.oyuncu1Adi ?: "P1",
                                                takimRengiArgb = 0xFF81D4FAL
                                            ),
                                            CezaOyuncuSecimi(
                                                oyuncuId = aktifOyunOzeti?.oyuncu2Id,
                                                oyuncuAdi = aktifOyunOzeti?.oyuncu2Adi ?: "P2",
                                                takimRengiArgb = 0xFFC62828L
                                            ),
                                            CezaOyuncuSecimi(
                                                oyuncuId = aktifOyunOzeti?.oyuncu3Id,
                                                oyuncuAdi = aktifOyunOzeti?.oyuncu3Adi ?: "P3",
                                                takimRengiArgb = 0xFF81D4FAL
                                            ),
                                            CezaOyuncuSecimi(
                                                oyuncuId = aktifOyunOzeti?.oyuncu4Id,
                                                oyuncuAdi = aktifOyunOzeti?.oyuncu4Adi ?: "P4",
                                                takimRengiArgb = 0xFFC62828L
                                            )
                                        )
                                    )
                                    aktifEkran = Ekran.CEZA
                                },
                                onCezaSilClick = { ceza ->
                                    val mevcutListe = cezaKayitlari[turNo]?.toMutableList() ?: mutableListOf()
                                    mevcutListe.removeAll { it.id == ceza.id }
                                    cezaKayitlari[turNo] = mevcutListe

                                    coroutineScope.launch {
                                        withContext(Dispatchers.IO) {
                                            val silinecekCeza = db.cezaDao().cezaGetir(ceza.id)
                                            if (silinecekCeza != null) {
                                                db.cezaDao().cezaSil(silinecekCeza)
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }

                    Ekran.VORHERIGE_SPIELE -> VorherigeSpieleEkrani(
                        onGeriClick = { aktifEkran = Ekran.ANA_SAYFA },
                        onOyunClick = { oyunId ->
                            seciliVorherigesOyunId = oyunId
                            aktifEkran = Ekran.VORHERIGES_OYUN_DETAY
                        },
                        onOyunSilClick = { oyunId ->
                            coroutineScope.launch {
                                withContext(Dispatchers.IO) {
                                    db.oyunKatilimciDao().oyununKatilimcilariniSil(oyunId)
                                    db.oyunDao().oyunSil(oyunId)
                                }
                            }
                            if (seciliVorherigesOyunId == oyunId) {
                                seciliVorherigesOyunId = null
                                seciliVorherigeTurNo = null
                                if (aktifEkran == Ekran.VORHERIGES_OYUN_DETAY || aktifEkran == Ekran.VORHERIGES_RUNDE_DETAY) {
                                    aktifEkran = Ekran.VORHERIGE_SPIELE
                                }
                            }
                        }
                    )

                    Ekran.VORHERIGES_OYUN_DETAY -> {
                        val oyunId = seciliVorherigesOyunId
                        if (oyunId != null) {
                            VorherigesOyunDetayEkrani(
                                oyunId = oyunId,
                                onGeriClick = { aktifEkran = Ekran.VORHERIGE_SPIELE },
                                onRundeClick = { turNo ->
                                    seciliVorherigeTurNo = turNo
                                    aktifEkran = Ekran.VORHERIGES_RUNDE_DETAY
                                }
                            )
                        }
                    }

                    Ekran.VORHERIGES_RUNDE_DETAY -> {
                        val oyunId = seciliVorherigesOyunId
                        val turNo = seciliVorherigeTurNo
                        if (oyunId != null && turNo != null) {
                            VorherigesRundenDetayEkrani(
                                oyunId = oyunId,
                                turNo = turNo,
                                onGeriClick = { aktifEkran = Ekran.VORHERIGES_OYUN_DETAY }
                            )
                        }
                    }

                    Ekran.STATISTIK -> StatistikEkrani(
                        onGeriClick = { aktifEkran = Ekran.ANA_SAYFA },
                        onOyuncuClick = { oyuncuId ->
                            seciliIstatistikOyuncuId = oyuncuId
                            aktifEkran = Ekran.OYUNCU_STATISTIK
                        }
                    )

                    Ekran.OYUNCU_STATISTIK -> {
                        val oyuncuId = seciliIstatistikOyuncuId
                        if (oyuncuId != null) {
                            val uiState by produceState<OyuncuStatistikUiState?>(
                                initialValue = null,
                                key1 = oyuncuId,
                                key2 = tumOyuncular
                            ) {
                                val oyuncu = tumOyuncular.firstOrNull { it.id == oyuncuId }
                                if (oyuncu == null) {
                                    value = null
                                    return@produceState
                                }

                                value = withContext(Dispatchers.IO) {
                                    val oyunSayisi = db.oyunKatilimciDao().oyuncununOyunSayisiniGetir(oyuncuId)
                                    val turSayisi = db.turOyuncuSonucDao().oyuncununOynadigiTurSayisiniGetir(oyuncuId)
                                    val turSonuclari = db.turOyuncuSonucDao().oyuncununTumTurSonuclariniGetir(oyuncuId)
                                    val toplamEndstandPuani = turSonuclari.sumOf { it.sonucPuani }
                                    val rotCezalar = db.cezaDao().oyuncununVerdigiCezalariGetir(oyuncuId)
                                    val yesilCezalar = db.cezaDao().oyuncununSebepOlduguCezalariGetir(oyuncuId)
                                    val acdiSayisi = turSonuclari.count { sonuc ->
                                        sonuc.girilenDeger != 0 &&
                                            sonuc.sonucPuani != 0 &&
                                            !sonuc.bitti &&
                                            !sonuc.okeyle &&
                                            !sonuc.eldenBitti &&
                                            !sonuc.cift &&
                                            !sonuc.acamadi
                                    }

                                    OyuncuStatistikUiState(
                                        oyuncuAdi = oyuncu.ad,
                                        oyunSayisi = oyunSayisi,
                                        turSayisi = turSayisi,
                                        rank = hesaplaOyuncuRanki(
                                            tumOyuncular = tumOyuncular,
                                            hedefOyuncuId = oyuncuId,
                                            db = db
                                        ),
                                        kazanilanOyunSayisi = hesaplaKazanilanOyunSayisi(
                                            oyuncuId = oyuncuId,
                                            db = db
                                        ),
                                        toplamEndstandPuani = toplamEndstandPuani,
                                        rotCezaAdet = rotCezalar.size,
                                        rotCezaPuan = rotCezalar.sumOf { it.puan },
                                        yesilCezaAdet = yesilCezalar.size,
                                        yesilCezaPuan = yesilCezalar.sumOf { it.puan },
                                        acdiSayisi = acdiSayisi,
                                        bittiSayisi = db.turOyuncuSonucDao().oyuncununBittiSayisiniGetir(oyuncuId),
                                        okeyleBitirmeSayisi = db.turOyuncuSonucDao().oyuncununOkeyleBitirmeSayisiniGetir(oyuncuId),
                                        eldenBitirmeSayisi = db.turOyuncuSonucDao().oyuncununEldenBitirmeSayisiniGetir(oyuncuId),
                                        ciftSayisi = db.turOyuncuSonucDao().oyuncununCiftSayisiniGetir(oyuncuId),
                                        acamadiSayisi = db.turOyuncuSonucDao().oyuncununAcamadiSayisiniGetir(oyuncuId)
                                    )
                                }
                            }

                            if (uiState != null) {
                                OyuncuStatistikEkrani(
                                    uiState = uiState!!,
                                    onGeriClick = { aktifEkran = Ekran.STATISTIK },
                                    onSpieleClick = {
                                        aktifEkran = Ekran.OYUNCU_STATISTIK_OYUNLAR_LISTE
                                    },
                                    onAcdiClick = {
                                        seciliTurFiltreTipi = OyuncuTurFiltreTipi.ACDI
                                        aktifEkran = Ekran.OYUNCU_STATISTIK_TUR_LISTE
                                    },
                                    onBittiClick = {
                                        seciliTurFiltreTipi = OyuncuTurFiltreTipi.BITTI
                                        aktifEkran = Ekran.OYUNCU_STATISTIK_TUR_LISTE
                                    },
                                    onOkeyleClick = {
                                        seciliTurFiltreTipi = OyuncuTurFiltreTipi.OKEYLE
                                        aktifEkran = Ekran.OYUNCU_STATISTIK_TUR_LISTE
                                    },
                                    onEldenClick = {
                                        seciliTurFiltreTipi = OyuncuTurFiltreTipi.ELDEN
                                        aktifEkran = Ekran.OYUNCU_STATISTIK_TUR_LISTE
                                    },
                                    onCiftClick = {
                                        seciliTurFiltreTipi = OyuncuTurFiltreTipi.CIFT
                                        aktifEkran = Ekran.OYUNCU_STATISTIK_TUR_LISTE
                                    },
                                    onAcamadiClick = {
                                        seciliTurFiltreTipi = OyuncuTurFiltreTipi.ACAMADI
                                        aktifEkran = Ekran.OYUNCU_STATISTIK_TUR_LISTE
                                    }
                                )
                            }
                        }
                    }

                    Ekran.OYUNCU_STATISTIK_TUR_LISTE -> {
                        val oyuncuId = seciliIstatistikOyuncuId
                        val filtreTipi = seciliTurFiltreTipi
                        if (oyuncuId != null && filtreTipi != null) {
                            val uiState by produceState<OyuncuStatistikTurListeUiState?>(
                                initialValue = null,
                                key1 = oyuncuId,
                                key2 = filtreTipi,
                                key3 = tumOyuncular
                            ) {
                                val oyuncu = tumOyuncular.firstOrNull { it.id == oyuncuId }
                                if (oyuncu == null) {
                                    value = null
                                    return@produceState
                                }

                                value = withContext(Dispatchers.IO) {
                                    val tumSonuclar = db.turOyuncuSonucDao().oyuncununTumTurSonuclariniGetir(oyuncuId)
                                    val uygunSonuclar = tumSonuclar.filter { sonuc ->
                                        when (filtreTipi) {
                                            OyuncuTurFiltreTipi.ACDI -> {
                                                sonuc.girilenDeger != 0 &&
                                                        sonuc.sonucPuani != 0 &&
                                                        !sonuc.bitti &&
                                                        !sonuc.okeyle &&
                                                        !sonuc.eldenBitti &&
                                                        !sonuc.cift &&
                                                        !sonuc.acamadi
                                            }
                                            OyuncuTurFiltreTipi.BITTI -> sonuc.bitti
                                            OyuncuTurFiltreTipi.OKEYLE -> sonuc.okeyle
                                            OyuncuTurFiltreTipi.ELDEN -> sonuc.eldenBitti
                                            OyuncuTurFiltreTipi.CIFT -> sonuc.cift
                                            OyuncuTurFiltreTipi.ACAMADI -> sonuc.acamadi
                                        }
                                    }

                                    val tarihFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                                    val liste = uygunSonuclar.mapNotNull { sonuc ->
                                        val tur = db.turDao().turGetir(sonuc.turId) ?: return@mapNotNull null
                                        val oyun = db.oyunDao().oyunGetir(tur.oyunId) ?: return@mapNotNull null
                                        val katilimcilar = db.oyunKatilimciDao().oyununKatilimcilariniGetir(oyun.id)
                                        val takim1Adi = katilimcilar.firstOrNull { it.takimNo == 1 }?.takimAdi ?: "Team 1"
                                        val takim2Adi = katilimcilar.firstOrNull { it.takimNo == 2 }?.takimAdi ?: "Team 2"
                                        val tarihKaynak = oyun.bitisZamani ?: oyun.baslangicZamani

                                        Triple(
                                            tarihKaynak,
                                            tur.turNo,
                                            OyuncuTurListeItem(
                                                oyunId = oyun.id,
                                                turNo = tur.turNo,
                                                tarihText = tarihFormatter.format(Date(tarihKaynak)),
                                                aciklamaText = if (oyun.mod == "ortak") "$takim1Adi vs $takim2Adi" else "Einzelspiel"
                                            )
                                        )
                                    }.sortedWith(
                                        compareByDescending<Triple<Long, Int, OyuncuTurListeItem>> { it.first }
                                            .thenByDescending { it.second }
                                    ).map { it.third }

                                    OyuncuStatistikTurListeUiState(
                                        oyuncuAdi = oyuncu.ad,
                                        filtreTipi = filtreTipi,
                                        toplamAdet = liste.size,
                                        turlar = liste
                                    )
                                }
                            }

                            if (uiState != null) {
                                OyuncuStatistikTurListeEkrani(
                                    uiState = uiState!!,
                                    onGeriClick = { aktifEkran = Ekran.OYUNCU_STATISTIK }
                                )
                            }
                        }
                    }

                    Ekran.OYUNCU_STATISTIK_OYUNLAR_LISTE -> {
                        val oyuncuId = seciliIstatistikOyuncuId
                        if (oyuncuId != null) {
                            val uiState by produceState<OyuncuStatistikOyunlarListeUiState?>(
                                initialValue = null,
                                key1 = oyuncuId,
                                key2 = tumOyuncular
                            ) {
                                val oyuncu = tumOyuncular.firstOrNull { it.id == oyuncuId }
                                if (oyuncu == null) {
                                    value = null
                                    return@produceState
                                }

                                value = withContext(Dispatchers.IO) {
                                    val bitenOyunlar = db.oyunDao().bitenOyunlariGetirFlow().first()
                                    val tarihFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

                                    val oyunlar = bitenOyunlar.mapNotNull { oyun ->
                                        val katilimcilar = db.oyunKatilimciDao().oyununKatilimcilariniGetir(oyun.id)
                                        val benimKatilimim = katilimcilar.firstOrNull { it.oyuncuId == oyuncuId } ?: return@mapNotNull null
                                        val turlar = db.turDao().oyununTurlariniGetirListe(oyun.id)

                                        val tumOyunSonuclari = turlar.flatMap { tur ->
                                            db.turOyuncuSonucDao().turunOyuncuSonuclariniGetirListe(tur.id)
                                        }
                                        val benimSonuclarim = tumOyunSonuclari.filter { it.oyuncuId == oyuncuId }
                                        val tumCezalar = turlar.flatMap { tur ->
                                            db.cezaDao().turunCezalariniGetirListe(tur.id)
                                        }

                                        val endstandPuani = benimSonuclarim.sumOf { it.sonucPuani }
                                        val rotCezalar = tumCezalar.filter { it.kirmiziOyuncuId == oyuncuId }
                                        val yesilCezalar = tumCezalar.filter { it.yesilOyuncuId == oyuncuId }
                                        val puanProRundeText = if (benimSonuclarim.isEmpty()) {
                                            "-"
                                        } else {
                                            String.format(
                                                Locale.GERMANY,
                                                "%.2f",
                                                endstandPuani.toDouble() / benimSonuclarim.size.toDouble()
                                            )
                                        }

                                        val kazanildi = if (oyun.mod == "ortak") {
                                            val takim1Oyuncular = katilimcilar.filter { it.takimNo == 1 }.map { it.oyuncuId }.toSet()
                                            val takim2Oyuncular = katilimcilar.filter { it.takimNo == 2 }.map { it.oyuncuId }.toSet()
                                            var takim1Toplam = 0
                                            var takim2Toplam = 0

                                            turlar.forEach { tur ->
                                                val sonuclar = db.turOyuncuSonucDao().turunOyuncuSonuclariniGetirListe(tur.id)
                                                val cezalar = db.cezaDao().turunCezalariniGetirListe(tur.id)

                                                takim1Toplam += sonuclar.filter { it.oyuncuId in takim1Oyuncular }.sumOf { it.sonucPuani }
                                                takim2Toplam += sonuclar.filter { it.oyuncuId in takim2Oyuncular }.sumOf { it.sonucPuani }
                                                takim1Toplam += cezalar.filter { it.kirmiziOyuncuId in takim1Oyuncular }.sumOf { it.puan }
                                                takim2Toplam += cezalar.filter { it.kirmiziOyuncuId in takim2Oyuncular }.sumOf { it.puan }
                                            }

                                            val kazananTakimNo = when {
                                                takim1Toplam < takim2Toplam -> 1
                                                takim2Toplam < takim1Toplam -> 2
                                                else -> null
                                            }
                                            kazananTakimNo != null && benimKatilimim.takimNo == kazananTakimNo
                                        } else {
                                            val oyuncuToplamlari = katilimcilar.associate { it.oyuncuId to 0 }.toMutableMap()

                                            turlar.forEach { tur ->
                                                val sonuclar = db.turOyuncuSonucDao().turunOyuncuSonuclariniGetirListe(tur.id)
                                                val cezalar = db.cezaDao().turunCezalariniGetirListe(tur.id)

                                                sonuclar.forEach { sonuc ->
                                                    oyuncuToplamlari[sonuc.oyuncuId] = (oyuncuToplamlari[sonuc.oyuncuId] ?: 0) + sonuc.sonucPuani
                                                }
                                                cezalar.forEach { ceza ->
                                                    oyuncuToplamlari[ceza.kirmiziOyuncuId] = (oyuncuToplamlari[ceza.kirmiziOyuncuId] ?: 0) + ceza.puan
                                                }
                                            }

                                            val enIyiDeger = oyuncuToplamlari.values.minOrNull()
                                            val tekKazanan = enIyiDeger != null && oyuncuToplamlari.values.count { it == enIyiDeger } == 1
                                            tekKazanan && oyuncuToplamlari[oyuncuId] == enIyiDeger
                                        }

                                        val baslikText = if (oyun.mod == "ortak") {
                                            val benimTakimNo = benimKatilimim.takimNo
                                            val benimTakimAdi = katilimcilar.firstOrNull { it.takimNo == benimTakimNo }?.takimAdi ?: "Mein Team"
                                            val rakipTakimAdi = katilimcilar.firstOrNull { it.takimNo != benimTakimNo }?.takimAdi ?: "Gegner"
                                            "$benimTakimAdi vs. $rakipTakimAdi"
                                        } else {
                                            val digerAdlar = katilimcilar
                                                .filter { it.oyuncuId != oyuncuId }
                                                .mapNotNull { katilimci -> tumOyuncular.firstOrNull { it.id == katilimci.oyuncuId }?.ad }
                                            if (digerAdlar.isEmpty()) "Einzelspiel" else "vs. ${digerAdlar.joinToString(", ")}"
                                        }

                                        Pair(
                                            oyun.bitisZamani ?: oyun.baslangicZamani,
                                            OyuncuOyunListeItem(
                                                oyunId = oyun.id,
                                                tarihText = tarihFormatter.format(Date(oyun.bitisZamani ?: oyun.baslangicZamani)),
                                                baslikText = baslikText,
                                                kazanildi = kazanildi,
                                                turSayisi = benimSonuclarim.size,
                                                endstandPuani = endstandPuani,
                                                rotCezaAdet = rotCezalar.size,
                                                rotCezaPuan = rotCezalar.sumOf { it.puan },
                                                yesilCezaAdet = yesilCezalar.size,
                                                yesilCezaPuan = yesilCezalar.sumOf { it.puan },
                                                puanProRundeText = puanProRundeText
                                            )
                                        )
                                    }.sortedByDescending { it.first }
                                        .map { it.second }

                                    OyuncuStatistikOyunlarListeUiState(
                                        oyuncuAdi = oyuncu.ad,
                                        toplamOyunSayisi = oyunlar.size,
                                        oyunlar = oyunlar
                                    )
                                }
                            }

                            if (uiState != null) {
                                OyuncuStatistikOyunlarListeEkrani(
                                    uiState = uiState!!,
                                    onGeriClick = { aktifEkran = Ekran.OYUNCU_STATISTIK }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun kaydetVeyaGuncelleCeza(
    uiState: CezaEkraniUiState,
    cezaKayitlari: MutableMap<Int, MutableList<CezaKaydi>>
): CezaKaydi? {
    val mevcutListe = cezaKayitlari[uiState.turNo]?.toMutableList() ?: mutableListOf()
    val yeniId = uiState.duzenlenenCezaId ?: ((cezaKayitlari.values.flatten().maxOfOrNull { it.id } ?: 0) + 1)
    val kayit = uiState.toCezaKaydi(yeniId) ?: return null

    val mevcutIndex = mevcutListe.indexOfFirst { it.id == kayit.id }
    if (mevcutIndex >= 0) {
        mevcutListe[mevcutIndex] = kayit
    } else {
        mevcutListe.add(kayit)
    }

    cezaKayitlari[uiState.turNo] = mevcutListe
    return kayit
}


private fun CezaEkraniUiState.kimlikleriTamamla(
    ozet: OyunBaslangicOzeti?
): CezaEkraniUiState {
    val guncelOyuncular = hedefOyuncular.map { oyuncu ->
        when (oyuncu.oyuncuAdi) {
            ozet?.oyuncu1Adi -> oyuncu.copy(oyuncuId = oyuncu.oyuncuId ?: ozet.oyuncu1Id)
            ozet?.oyuncu2Adi -> oyuncu.copy(oyuncuId = oyuncu.oyuncuId ?: ozet.oyuncu2Id)
            ozet?.oyuncu3Adi -> oyuncu.copy(oyuncuId = oyuncu.oyuncuId ?: ozet.oyuncu3Id)
            ozet?.oyuncu4Adi -> oyuncu.copy(oyuncuId = oyuncu.oyuncuId ?: ozet.oyuncu4Id)
            else -> oyuncu
        }
    }
    return copy(hedefOyuncular = guncelOyuncular)
}

private fun CezaEkraniUiState.seciliKimliklerHazir(): Boolean {
    val kirmizi = hedefOyuncular.firstOrNull { it.secimRolu == CezaSecimRolu.KIRMIZI }
    val yesil = hedefOyuncular.firstOrNull { it.secimRolu == CezaSecimRolu.YESIL }
    val kirmiziHazir = kirmizi?.oyuncuId != null && kirmizi.oyuncuId > 0
    val yesilHazir = yesil == null || (yesil.oyuncuId != null && yesil.oyuncuId > 0)
    return kirmiziHazir && yesilHazir
}

private suspend fun kaydetVeyaGuncelleCezaRoom(
    db: AppDatabase,
    oyunId: Int,
    kayit: CezaKaydi,
    guncelListe: List<CezaKaydi>
) {
    val mevcutTur = db.turDao().oyunVeTurNoIleTurGetir(oyunId, kayit.turNo)
    val simdi = System.currentTimeMillis()
    val turId = if (mevcutTur != null) {
        mevcutTur.id
    } else {
        db.turDao().turEkle(
            TurEntity(
                oyunId = oyunId,
                turNo = kayit.turNo,
                durum = "devam_ediyor",
                olusturmaZamani = simdi,
                bitisZamani = null
            )
        ).toInt()
    }

    val siraNo = guncelListe.indexOfFirst { it.id == kayit.id }
        .let { if (it >= 0) it + 1 else guncelListe.size }

    db.cezaDao().cezaEkle(
        kayit.toCezaEntity(
            turId = turId,
            siraNo = siraNo
        )
    )
}

private fun CezaKaydi.toCezaEntity(
    turId: Int,
    siraNo: Int
): CezaEntity {
    return CezaEntity(
        id = id,
        turId = turId,
        cezaTipi = cezaTipi.name,
        puan = puan,
        kirmiziOyuncuId = kirmiziOyuncuId ?: 0,
        yesilOyuncuId = yesilOyuncuId,
        tasDegeri = if (cezaTipi == CezaTipi.TAS_CEKILDI && puan % 10 == 0) puan / 10 else null,
        siraNo = siraNo
    )
}

private fun CezaEkraniUiState.toCezaKaydi(id: Int): CezaKaydi? {
    val cezaTipiDegeri = seciliCezaTipi ?: return null
    val kirmiziOyuncu = hedefOyuncular.firstOrNull { it.secimRolu == CezaSecimRolu.KIRMIZI } ?: return null
    val yesilOyuncu = hedefOyuncular.firstOrNull { it.secimRolu == CezaSecimRolu.YESIL }

    return CezaKaydi(
        id = id,
        turNo = turNo,
        cezaTipi = cezaTipiDegeri,
        puan = puanText.toIntOrNull() ?: 0,
        kirmiziOyuncuId = kirmiziOyuncu.oyuncuId,
        kirmiziOyuncuAdi = kirmiziOyuncu.oyuncuAdi,
        kirmiziTakimRengiArgb = kirmiziOyuncu.takimRengiArgb,
        yesilOyuncuId = yesilOyuncu?.oyuncuId,
        yesilOyuncuAdi = yesilOyuncu?.oyuncuAdi,
        yesilTakimRengiArgb = yesilOyuncu?.takimRengiArgb
    )
}

private fun CezaKaydi.toCezaEkraniUiState(
    mod: String,
    hedefOyuncular: List<CezaOyuncuSecimi>
): CezaEkraniUiState {
    val yeniOyuncular = hedefOyuncular.map { oyuncu ->
        when (oyuncu.oyuncuAdi) {
            kirmiziOyuncuAdi -> oyuncu.copy(secimRolu = CezaSecimRolu.KIRMIZI)
            yesilOyuncuAdi -> oyuncu.copy(secimRolu = CezaSecimRolu.YESIL)
            else -> oyuncu.copy(secimRolu = CezaSecimRolu.YOK)
        }
    }

    return CezaEkraniUiState(
        turNo = turNo,
        mod = mod,
        duzenlenenCezaId = id,
        seciliCezaTipi = cezaTipi,
        hedefOyuncular = yeniOyuncular,
        puanText = puan.toString(),
        tasDegeriText = if (cezaTipi == CezaTipi.TAS_CEKILDI && puan % 10 == 0) (puan / 10).toString() else "",
        digerDegerText = if (cezaTipi == CezaTipi.DIGER) puan.toString() else ""
    )
}

private fun CezaEkraniUiState.sifirlanmisKopya(): CezaEkraniUiState {
    return CezaEkraniUiState(
        turNo = turNo,
        mod = mod,
        duzenlenenCezaId = null,
        hedefOyuncular = hedefOyuncular.map { it.copy(secimRolu = CezaSecimRolu.YOK) }
    )
}

private fun SpielerRundenEndeUiState.toTurOyuncuSonucEntity(
    turId: Int,
    pozisyon: Int,
    mod: String
): TurOyuncuSonucEntity {
    val girilenDegerInt = eingabeText.toIntOrNull() ?: 0
    val multiplikatorInt = multiplikatorText.removePrefix("×").toIntOrNull() ?: 1
    val takimNoDegeri = if (mod == "ortak") {
        when (pozisyon) {
            1, 3 -> 1
            2, 4 -> 2
            else -> null
        }
    } else {
        null
    }

    return TurOyuncuSonucEntity(
        turId = turId,
        oyuncuId = spielerId ?: 0,
        pozisyon = pozisyon,
        takimNo = takimNoDegeri,
        girilenDeger = girilenDegerInt,
        sonucPuani = oyuncuSonPuaniHesapla(this),
        multiplikator = multiplikatorInt,
        cift = cift,
        bitti = bitti,
        okeyle = okeyle,
        eldenBitti = eldenBitti,
        acamadi = acamadi
    )
}

private fun RundeBeendenUiState.kimlikleriTamamla(
    ozet: OyunBaslangicOzeti?
): RundeBeendenUiState {
    return copy(
        oyuncu1 = oyuncu1.copy(spielerId = oyuncu1.spielerId ?: ozet?.oyuncu1Id),
        oyuncu2 = oyuncu2.copy(spielerId = oyuncu2.spielerId ?: ozet?.oyuncu2Id),
        oyuncu3 = oyuncu3.copy(spielerId = oyuncu3.spielerId ?: ozet?.oyuncu3Id),
        oyuncu4 = oyuncu4.copy(spielerId = oyuncu4.spielerId ?: ozet?.oyuncu4Id)
    )
}

private fun RundeBeendenUiState.tumOyuncuKimlikleriHazir(): Boolean {
    return listOf(
        oyuncu1.spielerId,
        oyuncu2.spielerId,
        oyuncu3.spielerId,
        oyuncu4.spielerId
    ).all { it != null && it > 0 }
}

@Composable
private fun NexIntroScreenAnimated(
    onFinished: () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.toFloat()
    val nexOffsetX = remember { Animatable(-screenWidth) }
    var showEntertainment by remember { mutableStateOf(false) }
    var visibleLetters by remember { mutableStateOf(0) }

    val word = "Entertainment"

    LaunchedEffect(Unit) {
        nexOffsetX.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = 700,
                easing = FastOutSlowInEasing
            )
        )

        kotlinx.coroutines.delay(150)
        showEntertainment = true

        word.forEachIndexed { index, _ ->
            visibleLetters = index + 1
            kotlinx.coroutines.delay(45)
        }

        kotlinx.coroutines.delay(900)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F1115),
                        Color(0xFF050608)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                text = "NEX",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 6.sp,
                modifier = Modifier.offset(x = nexOffsetX.value.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(visible = showEntertainment) {
                Text(
                    text = word.take(visibleLetters),
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.2.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
// Helper to map TurOyuncuSonucEntity? to SpielerRundenEndeUiState
private fun TurOyuncuSonucEntity?.toSpielerRundenEndeUiState(
    fallbackId: Int?,
    fallbackAd: String
): SpielerRundenEndeUiState {
    return SpielerRundenEndeUiState(
        spielerId = this?.oyuncuId ?: fallbackId,
        spielerName = fallbackAd,
        eingabeText = this?.girilenDeger?.toString() ?: "",
        multiplikatorText = "×${this?.multiplikator ?: 1}",
        cift = this?.cift ?: false,
        bitti = this?.bitti ?: false,
        okeyle = this?.okeyle ?: false,
        eldenBitti = this?.eldenBitti ?: false,
        acamadi = this?.acamadi ?: false
    )
}
private suspend fun hesaplaOyuncuRanki(
    tumOyuncular: List<OyuncuEntity>,
    hedefOyuncuId: Int,
    db: AppDatabase
): Int {
    data class RankItem(
        val oyuncuId: Int,
        val oyuncuAdi: String,
        val turSayisi: Int,
        val toplamPuan: Int
    )

    val liste = tumOyuncular.map { oyuncu ->
        val turSayisi = db.turOyuncuSonucDao().oyuncununOynadigiTurSayisiniGetir(oyuncu.id)
        val turSonuclari = db.turOyuncuSonucDao().oyuncununTumTurSonuclariniGetir(oyuncu.id)
        val toplamPuan = turSonuclari.sumOf { it.sonucPuani } + db.cezaDao().oyuncununCezaPuanToplaminiGetir(oyuncu.id)

        RankItem(
            oyuncuId = oyuncu.id,
            oyuncuAdi = oyuncu.ad,
            turSayisi = turSayisi,
            toplamPuan = toplamPuan
        )
    }.sortedWith(
        compareBy<RankItem> { it.turSayisi == 0 }
            .thenBy { if (it.turSayisi == 0) Double.POSITIVE_INFINITY else it.toplamPuan.toDouble() / it.turSayisi.toDouble() }
            .thenBy { it.oyuncuAdi.lowercase() }
    )

    return liste.indexOfFirst { it.oyuncuId == hedefOyuncuId }
        .let { if (it >= 0) it + 1 else 0 }
}

private suspend fun hesaplaKazanilanOyunSayisi(
    oyuncuId: Int,
    db: AppDatabase
): Int {
    val bitenOyunlar = db.oyunDao().bitenOyunlariGetirFlow().first()
    var kazanilanOyunSayisi = 0

    bitenOyunlar.forEach { oyun ->
        val katilimcilar = db.oyunKatilimciDao().oyununKatilimcilariniGetir(oyun.id)
        val benimKatilimim = katilimcilar.firstOrNull { it.oyuncuId == oyuncuId } ?: return@forEach
        val turlar = db.turDao().oyununTurlariniGetirListe(oyun.id)

        if (oyun.mod == "ortak") {
            val takim1Oyuncular = katilimcilar.filter { it.takimNo == 1 }.map { it.oyuncuId }.toSet()
            val takim2Oyuncular = katilimcilar.filter { it.takimNo == 2 }.map { it.oyuncuId }.toSet()
            var takim1Toplam = 0
            var takim2Toplam = 0

            turlar.forEach { tur ->
                val sonuclar = db.turOyuncuSonucDao().turunOyuncuSonuclariniGetirListe(tur.id)
                val cezalar = db.cezaDao().turunCezalariniGetirListe(tur.id)

                takim1Toplam += sonuclar.filter { it.oyuncuId in takim1Oyuncular }.sumOf { it.sonucPuani }
                takim2Toplam += sonuclar.filter { it.oyuncuId in takim2Oyuncular }.sumOf { it.sonucPuani }
                takim1Toplam += cezalar.filter { it.kirmiziOyuncuId in takim1Oyuncular }.sumOf { it.puan }
                takim2Toplam += cezalar.filter { it.kirmiziOyuncuId in takim2Oyuncular }.sumOf { it.puan }
            }

            val kazananTakimNo = when {
                takim1Toplam < takim2Toplam -> 1
                takim2Toplam < takim1Toplam -> 2
                else -> null
            }

            if (kazananTakimNo != null && benimKatilimim.takimNo == kazananTakimNo) {
                kazanilanOyunSayisi++
            }
        } else {
            val oyuncuToplamlari = katilimcilar.associate { it.oyuncuId to 0 }.toMutableMap()

            turlar.forEach { tur ->
                val sonuclar = db.turOyuncuSonucDao().turunOyuncuSonuclariniGetirListe(tur.id)
                val cezalar = db.cezaDao().turunCezalariniGetirListe(tur.id)

                sonuclar.forEach { sonuc ->
                    oyuncuToplamlari[sonuc.oyuncuId] = (oyuncuToplamlari[sonuc.oyuncuId] ?: 0) + sonuc.sonucPuani
                }
                cezalar.forEach { ceza ->
                    oyuncuToplamlari[ceza.kirmiziOyuncuId] = (oyuncuToplamlari[ceza.kirmiziOyuncuId] ?: 0) + ceza.puan
                }
            }

            val enIyiDeger = oyuncuToplamlari.values.minOrNull()
            val tekKazanan = enIyiDeger != null && oyuncuToplamlari.values.count { it == enIyiDeger } == 1
            if (tekKazanan && oyuncuToplamlari[oyuncuId] == enIyiDeger) {
                kazanilanOyunSayisi++
            }
        }
    }

    return kazanilanOyunSayisi
}


// (Stray/duplicate OYUNCU_STATISTIK_OYUNLAR_LISTE block removed if present)