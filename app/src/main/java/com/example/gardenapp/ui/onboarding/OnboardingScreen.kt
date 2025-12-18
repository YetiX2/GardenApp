package com.example.gardenapp.ui.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gardenapp.ui.theme.GardenAppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pages = listOf(
        "Добро пожаловать!" to "Планируйте свой участок и отслеживайте рост растений.",
        "Напоминания об уходе" to "Никогда не забывайте полить или удобрить ваши растения.",
        "Все готово!" to "Нажмите кнопку ниже, чтобы начать работу."
    )
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope() // ADDED

    Scaffold {
        Column(
            modifier = Modifier.fillMaxSize().padding(it).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(pages[page].first, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Text(pages[page].second, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                }
            }

            // MODIFIED BLOCK
            if (pagerState.currentPage == pages.lastIndex) {
                Button(onClick = onFinished, modifier = Modifier.fillMaxWidth()) {
                    Text("Начать работу")
                }
            } else {
                Button(
                    onClick = { 
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Далее")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    GardenAppTheme {
        OnboardingScreen(onFinished = {})
    }
}
