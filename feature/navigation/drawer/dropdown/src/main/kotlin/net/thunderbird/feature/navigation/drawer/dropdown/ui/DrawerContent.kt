package net.thunderbird.feature.navigation.drawer.dropdown.ui

import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import net.thunderbird.components.ui.bolt.atom.DividerHorizontal
import net.thunderbird.components.ui.bolt.atom.Surface
import net.thunderbird.components.ui.bolt.atom.button.ButtonIcon
import net.thunderbird.components.ui.bolt.atom.icon.Icons
import net.thunderbird.components.ui.bolt.atom.text.TextBodySmall
import net.thunderbird.components.ui.bolt.atom.text.TextHeadlineMedium
import net.thunderbird.components.ui.bolt.theme.BoltTheme
import net.thunderbird.feature.navigation.drawer.dropdown.R
import net.thunderbird.feature.navigation.drawer.dropdown.domain.entity.DisplayAccount
import net.thunderbird.feature.navigation.drawer.dropdown.domain.entity.UnifiedDisplayAccount
import net.thunderbird.feature.navigation.drawer.dropdown.ui.DrawerContract.Event
import net.thunderbird.feature.navigation.drawer.dropdown.ui.DrawerContract.State
import net.thunderbird.feature.navigation.drawer.dropdown.ui.account.AccountList
import net.thunderbird.feature.navigation.drawer.dropdown.ui.account.AccountView
import net.thunderbird.feature.navigation.drawer.dropdown.ui.common.DRAWER_WIDTH
import net.thunderbird.feature.navigation.drawer.dropdown.ui.common.getAdditionalWidth
import net.thunderbird.feature.navigation.drawer.dropdown.ui.folder.FolderList
import net.thunderbird.feature.navigation.drawer.dropdown.ui.setting.AccountSettingList
import net.thunderbird.feature.navigation.drawer.dropdown.ui.setting.FolderSettingList

private const val ANIMATION_DURATION_MS = 300

@Composable
private fun areSystemAnimationsEnabled(): Boolean {
    return Settings.Global.getFloat(
        LocalContext.current.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
    ) != 0f
}

private fun accountSelectorTransitionSpec(
    areAnimationsEnabled: Boolean,
): AnimatedContentTransitionScope<Boolean>.() -> ContentTransform = {
    if (areAnimationsEnabled) {
        val animationSpec = tween<IntOffset>(durationMillis = ANIMATION_DURATION_MS, easing = FastOutSlowInEasing)
        if (targetState) {
            slideInVertically(animationSpec = animationSpec) { -it } togetherWith
                slideOutVertically(animationSpec = animationSpec) { it }
        } else {
            slideInVertically(animationSpec = animationSpec) { it } togetherWith
                slideOutVertically(animationSpec = animationSpec) { -it }
        }
    } else {
        slideInVertically(animationSpec = snap()) { 0 } togetherWith
            slideOutVertically(animationSpec = snap()) { 0 }
    }
}

@Composable
internal fun DrawerContent(
    state: State,
    onEvent: (Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val additionalWidth = getAdditionalWidth()
    val areAnimationsEnabled = areSystemAnimationsEnabled()

    Surface(
        modifier = modifier
            .width(DRAWER_WIDTH + additionalWidth)
            .fillMaxHeight()
            .testTag("DrawerContent"),
        color = BoltTheme.colors.surface,
    ) {
        val selectedAccount = state.accounts.firstOrNull { it.id == state.selectedAccountId }

        Column(
            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = BoltTheme.spacings.triple,
                        end = BoltTheme.spacings.default,
                        top = BoltTheme.spacings.double,
                        bottom = BoltTheme.spacings.half,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextHeadlineMedium(
                    text = stringResource(R.string.navigation_drawer_dropdown_header_mail),
                    color = BoltTheme.colors.onSurface,
                )
                ButtonIcon(
                    onClick = { onEvent(Event.OnSettingsClick) },
                    imageVector = Icons.Outlined.Settings,
                )
            }

            TextBodySmall(
                text = stringResource(R.string.navigation_drawer_dropdown_current_account),
                color = BoltTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = BoltTheme.spacings.triple,
                    bottom = BoltTheme.spacings.half,
                ),
            )

            selectedAccount?.let {
                AccountView(
                    account = selectedAccount,
                    onClick = { onEvent(Event.OnAccountSelectorClick) },
                    onAvatarClick = { onEvent(Event.OnAccountViewClick(selectedAccount)) },
                    showAccountSelection = state.showAccountSelection,
                    isShowAnimations = areAnimationsEnabled,
                )

                DividerHorizontal(
                    color = BoltTheme.colors.outlineVariant,
                    modifier = Modifier.padding(horizontal = BoltTheme.spacings.double),
                )
            }
            AnimatedContent(
                targetState = state.showAccountSelection,
                label = "AccountSelectorVisibility",
                transitionSpec = accountSelectorTransitionSpec(areAnimationsEnabled),
            ) { targetState ->
                if (targetState) {
                    AccountContent(
                        state = state,
                        onEvent = onEvent,
                        selectedAccount = selectedAccount,
                    )
                } else {
                    FolderContent(
                        state = state,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountContent(
    state: State,
    onEvent: (Event) -> Unit,
    selectedAccount: DisplayAccount?,
) {
    Surface(
        color = BoltTheme.colors.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            AccountList(
                accounts = state.accounts,
                selectedAccount = selectedAccount,
                onAccountClick = { onEvent(Event.OnAccountClick(it)) },
                showStarredCount = state.config.showStarredCount,
                modifier = Modifier.weight(1f),
            )
            DividerHorizontal()
            AccountSettingList(
                onAddAccountClick = { onEvent(Event.OnAddAccountClick) },
                onSyncAllAccountsClick = { onEvent(Event.OnSyncAllAccounts) },
                onSettingsClick = { onEvent(Event.OnSettingsClick) },
                isLoading = state.isLoading,
            )
        }
    }
}

@Composable
private fun FolderContent(
    state: State,
    onEvent: (Event) -> Unit,
) {
    val isUnifiedAccount = state.accounts.firstOrNull { it.id == state.selectedAccountId } is UnifiedDisplayAccount

    Surface(
        color = BoltTheme.colors.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            FolderList(
                isExpandedInitial = state.config.expandAllFolder,
                rootFolder = state.rootFolder,
                selectedFolder = state.selectedFolder,
                onFolderClick = { folder ->
                    onEvent(Event.OnFolderClick(folder))
                },
                showStarredCount = state.config.showStarredCount,
                modifier = Modifier.weight(1f),
            )
            DividerHorizontal()
            FolderSettingList(
                onSyncAccountClick = { onEvent(Event.OnSyncAccount) },
                onSyncAllAccountsClick = { onEvent(Event.OnSyncAllAccounts) },
                onSettingsClick = { onEvent(Event.OnSettingsClick) },
                isUnifiedAccount = isUnifiedAccount,
                isLoading = state.isLoading,
            )
        }
    }
}
