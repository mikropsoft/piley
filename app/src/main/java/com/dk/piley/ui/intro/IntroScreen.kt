package com.dk.piley.ui.intro

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.dk.piley.R
import com.dk.piley.compose.PreviewMainScreen
import com.dk.piley.ui.nav.Screen
import com.dk.piley.ui.theme.PileyTheme
import com.dk.piley.util.usernameCharacterLimit

/**
 * Intro screen
 *
 * @param modifier generic modifier
 * @param navController generic nav controller
 * @param viewModel intro view model
 */
@Composable
fun IntroScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: IntroViewModel = hiltViewModel()
) {
    IntroScreen(
        modifier = modifier,
        onFinish = {
            viewModel.setUsername(it)
            navController.popBackStack()
            navController.navigate(Screen.Pile.route)
        },
        nameFieldValue = viewModel.registerName
    )
}

/**
 * Intro screen content
 *
 * @param modifier generic modifier
 * @param nameFieldValue the initial name value, if passed by the register screen
 * @param onFinish on intro screen completion
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IntroScreen(
    modifier: Modifier = Modifier,
    nameFieldValue: String = "",
    onFinish: (String) -> Unit = {},
) {
    val pages = listOf(
        IntroPage.Welcome,
        IntroPage.Pile,
        IntroPage.RecurringPile,
        IntroPage.Piles,
        IntroPage.Profile,
        IntroPage.End
    )
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) { pages.size }
    Column(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            modifier = Modifier.weight(10f),
            state = pagerState,
            verticalAlignment = Alignment.CenterVertically
        ) { position ->
            if (position != pages.lastIndex) {
                IntroPageContent(introPage = pages[position])
            } else {
                IntroPageTextFieldContent(
                    introPage = pages[position],
                    textFieldHint = stringResource(R.string.first_user_name_hint),
                    textMaxLength = usernameCharacterLimit,
                    buttonText = stringResource(R.string.finish_intro_button),
                    initialTextFieldValue = nameFieldValue,
                    onClickButton = onFinish
                )
            }
        }
        PagerIndicator(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .weight(1f),
            pagerState = pagerState
        )
    }
}

@PreviewMainScreen
@Composable
fun IntroScreenPreview() {
    PileyTheme {
        Surface {
            IntroScreen(onFinish = {})
        }
    }
}
