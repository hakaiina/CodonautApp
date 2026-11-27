package ru.codonaut.app.features.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.codonaut.app.CodonautApplication
import ru.codonaut.app.R
import ru.codonaut.app.core.ui.theme.CodonautColors
import ru.codonaut.app.domain.model.UserProfile
import ru.codonaut.app.domain.repository.SessionRepository
import ru.codonaut.app.domain.repository.SelectedLanguageRepository
import ru.codonaut.app.domain.usecase.AccountSessionCoordinator
import ru.codonaut.app.domain.model.ProgrammingLanguage

data class SessionDebugUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val selectedLanguageIds: List<String> = emptyList(),
    val error: String? = null,
    val isSignedOut: Boolean = false
)

class SessionDebugViewModel(
    private val sessionRepository: SessionRepository,
    private val coordinator: AccountSessionCoordinator,
    private val selectedLanguageRepository: SelectedLanguageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionDebugUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                val profile = sessionRepository.getActiveProfile()

                _uiState.value = SessionDebugUiState(
                    isLoading = false,
                    profile = profile,
                    error = if (profile == null) {
                        "Активный профиль не найден"
                    } else null
                )
                if (profile != null) {
                    selectedLanguageRepository
                        .observeSelectedLanguageIds(profile.profileId)
                        .collect { ids ->
                            _uiState.value = _uiState.value.copy(
                                selectedLanguageIds = ids
                            )
                        }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = SessionDebugUiState(
                    isLoading = false,
                    error = "Не удалось загрузить профиль"
                )
            }
        }
    }

    fun signOut() {
        if (_uiState.value.isLoading) return

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            try {
                coordinator.signOut()

                _uiState.value = SessionDebugUiState(
                    isLoading = false,
                    isSignedOut = true
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Не удалось выйти из профиля"
                )
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[
                    ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                ] as CodonautApplication

                SessionDebugViewModel(
                    sessionRepository =
                        application.container.sessionRepository,
                    coordinator =
                        application.container.accountSessionCoordinator,
                    selectedLanguageRepository =
                        application.container.selectedLanguageRepository
                )
            }
        }
    }
}

@Composable
fun SessionDebugScreen(
    onSignedOut: () -> Unit,
    onRegisterGuest: () -> Unit,
    viewModel: SessionDebugViewModel = viewModel(
        factory = SessionDebugViewModel.Factory
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSignedOut) {
        if (state.isSignedOut) onSignedOut()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CodonautColors.WarmMist)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.session_debug_title),
            style = MaterialTheme.typography.headlineSmall,
            color = CodonautColors.EspressoBlack
        )

        if (state.isLoading) {
            CircularProgressIndicator(color = CodonautColors.DeepTeal)
        } else {
            val profile = state.profile
            if (profile != null) {
                val selectedLanguageNames = buildList {
                    if (ProgrammingLanguage.PYTHON.id in state.selectedLanguageIds) {
                        add(stringResource(R.string.language_python))
                    }
                    if (ProgrammingLanguage.JAVASCRIPT.id in state.selectedLanguageIds) {
                        add(stringResource(R.string.language_javascript))
                    }
                    if (ProgrammingLanguage.JAVA.id in state.selectedLanguageIds) {
                        add(stringResource(R.string.language_java))
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CodonautColors.SoftCream)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(
                                if (profile.authUid == null) R.string.session_debug_guest
                                else R.string.session_debug_registered
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = CodonautColors.DeepTeal
                        )
                        Text(stringResource(R.string.session_debug_nickname, profile.nickname ?: stringResource(R.string.session_debug_not_set)))
                        Text(stringResource(R.string.session_debug_profile_id, profile.profileId))
                        Text(stringResource(R.string.session_debug_firebase_uid, profile.authUid ?: stringResource(R.string.session_debug_not_linked)))
                        Text(
                            stringResource(
                                R.string.session_debug_languages,
                                selectedLanguageNames.joinToString()
                                    .ifBlank { stringResource(R.string.session_debug_no_languages) }
                            )
                        )
                    }
                }

                if (profile.authUid == null) {
                    Button(onClick = onRegisterGuest, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.session_debug_register_guest))
                    }
                }
            }

            if (state.error != null) {
                Text(
                    text = stringResource(R.string.session_debug_error),
                    color = CodonautColors.Error
                )
            }

            Button(
                onClick = viewModel::signOut,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.session_debug_sign_out))
            }
        }
    }
}