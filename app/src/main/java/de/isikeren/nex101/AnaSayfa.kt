package de.isikeren.nex101

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.isikeren.nex101.ui.theme.NEX101Theme

@Composable
fun AnaSayfa(
    modifier: Modifier = Modifier,
    oyunDevamEdiyor: Boolean,
    onYeniOyunClick: () -> Unit,
    onOyuncuYonetClick: () -> Unit,
    onOyunuFortsetzenClick: () -> Unit,
    onVorherigeSpieleClick: () -> Unit
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(start = 24.dp, end = 24.dp, top = 56.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UstBar()

            Spacer(modifier = Modifier.height(40.dp))

            LogoAlani()

            Spacer(modifier = Modifier.height(52.dp))
            AltMenu(
                oyunDevamEdiyor = oyunDevamEdiyor,
                onYeniOyunClick = onYeniOyunClick,
                onOyuncuYonetClick = onOyuncuYonetClick,
                onOyunuFortsetzenClick = onOyunuFortsetzenClick,
                onVorherigeSpieleClick = onVorherigeSpieleClick
            )
        }
    }
}

@Composable
private fun UstBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(44.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LogoAlani() {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(320.dp)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun AltMenu(
    oyunDevamEdiyor: Boolean,
    onYeniOyunClick: () -> Unit,
    onOyuncuYonetClick: () -> Unit,
    onOyunuFortsetzenClick: () -> Unit,
    onVorherigeSpieleClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            AnaMenuButonu(text = "Spieler verwalten", onClick = onOyuncuYonetClick)
            AnaMenuButonu(text = "Statistiken", onClick = { })
            AnaMenuButonu(text = "Vorherige Spiele", onClick = onVorherigeSpieleClick)
        }

        if (oyunDevamEdiyor) {
            Column(
                modifier = Modifier
                    .weight(0.38f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                YeniOyunButonu(
                    onClick = onYeniOyunClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f)
                )

                AnaMenuButonu(
                    text = "Zum\nSpiel",
                    onClick = onOyunuFortsetzenClick
                )
            }
        } else {
            YeniOyunButonu(
                onClick = onYeniOyunClick,
                modifier = Modifier
                    .weight(0.38f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
fun AnaMenuButonu(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }
}

@Composable
private fun YeniOyunButonu(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Light
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AnaSayfaPreview() {
    NEX101Theme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnaSayfa(
                oyunDevamEdiyor = true,
                onYeniOyunClick = {},
                onOyuncuYonetClick = {},
                onOyunuFortsetzenClick = {},
                onVorherigeSpieleClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AnaSayfaOhneAktivesSpielPreview() {
    NEX101Theme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnaSayfa(
                oyunDevamEdiyor = false,
                onYeniOyunClick = {},
                onOyuncuYonetClick = {},
                onOyunuFortsetzenClick = {},
                onVorherigeSpieleClick = {}
            )
        }
    }
}