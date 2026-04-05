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
    RUNDE_DETAY
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        setContent {
            NEX101Theme {
                var aktifEkran by rememberSaveable { mutableStateOf(Ekran.ANA_SAYFA) }
                var aktifOyunId by rememberSaveable { mutableStateOf<Int?>(null) }
                var aktifOyunOzeti by remember { mutableStateOf<OyunBaslangicOzeti?>(null) }
                var rundeBeendenUiState by remember { mutableStateOf<RundeBeendenUiState?>(null) }
                var cezaUiState by remember { mutableStateOf<CezaEkraniUiState?>(null) }
                var seciliTurDetayNo by remember { mutableStateOf<Int?>(null) }
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
                        onOyunuFortsetzenClick = { aktifEkran = Ekran.OYUN_DETAY }
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
                            aktifEkran = Ekran.OYUN_DETAY
                        }
                    )

                    Ekran.OYUNCU_YONETIM -> OyuncuYonetimEkrani(
                        onGeriClick = { aktifEkran = Ekran.ANA_SAYFA }
                    )

                    Ekran.OYUN_DETAY -> OyunDetayEkrani(
                        oyunId = aktifOyunId ?: 0,
                        baslangicOzeti = aktifOyunOzeti,
                        onGeriClick = { aktifEkran = Ekran.YENI_OYUN },
                        onRundeBeendenClick = { turNo ->
                            rundeBeendenUiState = RundeBeendenUiState(
                                turNo = turNo,
                                mod = aktifOyunOzeti?.mod ?: "ortak",
                                oyuncu1 = SpielerRundenEndeUiState(
                                    spielerName = aktifOyunOzeti?.oyuncu1Adi ?: "P1",
                                    multiplikatorText = "×1"
                                ),
                                oyuncu2 = SpielerRundenEndeUiState(
                                    spielerName = aktifOyunOzeti?.oyuncu2Adi ?: "P2",
                                    multiplikatorText = "×1"
                                ),
                                oyuncu3 = SpielerRundenEndeUiState(
                                    spielerName = aktifOyunOzeti?.oyuncu3Adi ?: "P3",
                                    multiplikatorText = "×1"
                                ),
                                oyuncu4 = SpielerRundenEndeUiState(
                                    spielerName = aktifOyunOzeti?.oyuncu4Adi ?: "P4",
                                    multiplikatorText = "×1"
                                )
                            )
                            aktifEkran = Ekran.RUNDE_BEENDEN
                        },
                        onCezaClick = { turNo ->
                            cezaUiState = CezaEkraniUiState(
                                turNo = turNo,
                                mod = aktifOyunOzeti?.mod ?: "ortak",
                                hedefOyuncular = listOf(
                                    CezaOyuncuSecimi(
                                        oyuncuAdi = aktifOyunOzeti?.oyuncu1Adi ?: "P1",
                                        takimRengiArgb = 0xFF81D4FAL
                                    ),
                                    CezaOyuncuSecimi(
                                        oyuncuAdi = aktifOyunOzeti?.oyuncu2Adi ?: "P2",
                                        takimRengiArgb = 0xFFC62828L
                                    ),
                                    CezaOyuncuSecimi(
                                        oyuncuAdi = aktifOyunOzeti?.oyuncu3Adi ?: "P3",
                                        takimRengiArgb = 0xFF81D4FAL
                                    ),
                                    CezaOyuncuSecimi(
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
                                onSpeichernClick = {
                                    rundeBeendenUiState = it
                                    if (it.mod == "ortak") {
                                        ortakTurSonuclari[it.turNo] = ortakTakimToplamlariniHesapla(it)
                                    }
                                    aktifTurNoDurumu = null
                                    aktifEkran = Ekran.OYUN_DETAY
                                }
                            )
                        }
                    }

                    Ekran.CEZA -> {
                        val uiState = cezaUiState
                        if (uiState != null) {
                            CezaEkrani(
                                uiState = uiState,
                                onGeriClick = { aktifEkran = Ekran.OYUN_DETAY },
                                onKaydetVeKapat = {
                                    it.toCezaKaydi()?.let { kayit ->
                                        val mevcutListe = cezaKayitlari[it.turNo]?.toMutableList() ?: mutableListOf()
                                        mevcutListe.add(kayit)
                                        cezaKayitlari[it.turNo] = mevcutListe
                                    }
                                    cezaUiState = it.sifirlanmisKopya()
                                    aktifEkran = Ekran.OYUN_DETAY
                                },
                                onSatiraEkle = {
                                    it.toCezaKaydi()?.let { kayit ->
                                        val mevcutListe = cezaKayitlari[it.turNo]?.toMutableList() ?: mutableListOf()
                                        mevcutListe.add(kayit)
                                        cezaKayitlari[it.turNo] = mevcutListe
                                    }
                                    cezaUiState = it.sifirlanmisKopya()
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
                                onCezaClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun CezaEkraniUiState.toCezaKaydi(): CezaKaydi? {
    val cezaTipiDegeri = seciliCezaTipi ?: return null
    val kirmiziOyuncu = hedefOyuncular.firstOrNull { it.secimRolu == CezaSecimRolu.KIRMIZI } ?: return null
    val yesilOyuncu = hedefOyuncular.firstOrNull { it.secimRolu == CezaSecimRolu.YESIL }

    return CezaKaydi(
        turNo = turNo,
        cezaTipi = cezaTipiDegeri,
        puan = puanText.toIntOrNull() ?: 0,
        kirmiziOyuncuAdi = kirmiziOyuncu.oyuncuAdi,
        kirmiziTakimRengiArgb = kirmiziOyuncu.takimRengiArgb,
        yesilOyuncuAdi = yesilOyuncu?.oyuncuAdi,
        yesilTakimRengiArgb = yesilOyuncu?.takimRengiArgb
    )
}

private fun CezaEkraniUiState.sifirlanmisKopya(): CezaEkraniUiState {
    return CezaEkraniUiState(
        turNo = turNo,
        mod = mod,
        hedefOyuncular = hedefOyuncular.map { it.copy(secimRolu = CezaSecimRolu.YOK) }
    )
}