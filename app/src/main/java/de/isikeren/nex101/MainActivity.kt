package de.isikeren.nex101

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import de.isikeren.nex101.ui.theme.NEX101Theme

private enum class Ekran {
    ANA_SAYFA,
    YENI_OYUN,
    OYUNCU_YONETIM,
    OYUN_DETAY,
    RUNDE_BEENDEN,
    CEZA,
    RUNDE_DETAY,
    VORHERIGE_SPIELE,
    VORHERIGES_OYUN_DETAY,
    VORHERIGES_RUNDE_DETAY
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
                var aktifEkran by rememberSaveable { mutableStateOf(Ekran.ANA_SAYFA) }
                var aktifOyunId by rememberSaveable { mutableStateOf<Int?>(null) }
                var aktifOyunOzeti by remember { mutableStateOf<OyunBaslangicOzeti?>(null) }
                var rundeBeendenUiState by remember { mutableStateOf<RundeBeendenUiState?>(null) }
                var cezaUiState by remember { mutableStateOf<CezaEkraniUiState?>(null) }
                var duzenlenenCezaKaydi by remember { mutableStateOf<CezaKaydi?>(null) }
                var seciliTurDetayNo by remember { mutableStateOf<Int?>(null) }
                var seciliVorherigesOyunId by remember { mutableStateOf<Int?>(null) }
                var seciliVorherigeTurNo by remember { mutableStateOf<Int?>(null) }
                val aktifRundenListe = remember { mutableStateListOf<Int>() }
                var aktifTurNoDurumu by remember { mutableStateOf<Int?>(null) }
                val ortakTurSonuclari = remember { mutableStateMapOf<Int, Pair<Int, Int>>() }
                val cezaKayitlari = remember { mutableStateMapOf<Int, MutableList<CezaKaydi>>() }
                var yeniOyunTaslagi by remember { mutableStateOf(YeniOyunTaslakDurumu()) }

                when (aktifEkran) {
                    Ekran.ANA_SAYFA -> AnaSayfa(
                        oyunDevamEdiyor = aktifOyunId != null,
                        onYeniOyunClick = { aktifEkran = Ekran.YENI_OYUN },
                        onOyuncuYonetClick = { aktifEkran = Ekran.OYUNCU_YONETIM },
                        onOyunuFortsetzenClick = { aktifEkran = Ekran.OYUN_DETAY },
                        onVorherigeSpieleClick = { aktifEkran = Ekran.VORHERIGE_SPIELE }
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
                                onErgebnisClick = {},
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