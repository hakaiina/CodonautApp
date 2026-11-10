package ru.codonaut.app.features.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.codonaut.app.R
import ru.codonaut.app.core.ui.components.BaseButton
import ru.codonaut.app.core.ui.theme.CodonautColors
import ru.codonaut.app.core.ui.theme.CodonautTheme

// класс для хранения данных, Int - указывает на ресурс, который необходимо загрузить

private data class OnboardingPage(
    @DrawableRes val imageRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
)

private val onboardingPages = listOf(
    OnboardingPage(
        imageRes = R.drawable.onboarding_1,
        titleRes = R.string.onboarding_title_1,
        descriptionRes = R.string.onboarding_description_1
    ),
    OnboardingPage(
        imageRes = R.drawable.onboarding_2,
        titleRes = R.string.onboarding_title_2,
        descriptionRes = R.string.onboarding_description_2
    ),
    OnboardingPage(
        imageRes = R.drawable.onboarding_3,
        titleRes = R.string.onboarding_title_3,
        descriptionRes = R.string.onboarding_description_3
    )
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    // // pagerState хранит информацию о состоянии перелистываемого контейнера
    val pagerState = rememberPagerState(
        pageCount = { onboardingPages.size }
    )
    val coroutineScope = rememberCoroutineScope() // создаем корутину
    val isLastPage = pagerState.currentPage == onboardingPages.lastIndex

    Column(
        // настройка внешнего вида, размеров, расположения и поведения компонента
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        // кнопка пропуска в верхней части
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onFinished,
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Text(
                    text = stringResource(R.string.onboarding_skip),
                    style = MaterialTheme.typography.bodySmall,
                    color = CodonautColors.MochaGray
                )
            }
        }

        // три страницы с перелистыванием
        // HorizontalPager организует горизонтальное перелистывание страниц
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f) // передаем Pager оставшееся доступное пространство после измерения остальных элементов Column
                .fillMaxWidth()
        ) { pageIndex ->
            OnboardingPageContent(
                page = onboardingPages[pageIndex]
            )
        }

        // индикатор текущей страницы
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(onboardingPages.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == index) {
                                CodonautColors.DeepTeal
                            } else {
                                CodonautColors.WarmStone
                            }
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        BaseButton(
            text = stringResource(
                if (isLastPage) {
                    R.string.onboarding_start
                } else {
                    R.string.onboarding_next
                }
            ),
            // обработчик основной кнопки
            // корутина нужна, чтобы не блокировать основной поток при анимации
            onClick = {
                if (isLastPage) {
                    onFinished()
                } else {
                    coroutineScope.launch { // launch запускает корутину в соответствующей области выполнения
                        pagerState.animateScrollToPage( // анимированное перелистывание
                            pagerState.currentPage + 1
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Иллюстрация занимает свободное пространство
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(page.imageRes), // загружаем графический ресурс по идентификатору
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.94f),
                contentScale = ContentScale.Fit // вписываем изображение, сохраняя его пропорции
            )
        }

        // заголовок и описание
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 178.dp)
        ) {
            Text( // загружаем текст
                text = stringResource(page.titleRes),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(page.descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = CodonautColors.MochaGray
            )
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun OnboardingPreview() {
    CodonautTheme {
        OnboardingScreen(onFinished = {})
    }
}
