package com.vitorpamplona.amethyst.ui.note

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.flowlayout.FlowRow
import com.vitorpamplona.amethyst.NotificationCache
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.ui.screen.LikeSetCard
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LikeSetCompose(likeSetCard: LikeSetCard, isInnerNote: Boolean = false, routeForLastRead: String, accountViewModel: AccountViewModel, navController: NavController) {
    val noteState by likeSetCard.note.live().metadata.observeAsState()
    val note = noteState?.note

    val accountState by accountViewModel.accountLiveData.observeAsState()
    val account = accountState?.account ?: return

    val noteEvent = note?.event
    var popupExpanded by remember { mutableStateOf(false) }

    if (note == null) {
        BlankNote(Modifier, isInnerNote)
    } else {
        var isNew by remember { mutableStateOf<Boolean>(false) }

        LaunchedEffect(key1 = likeSetCard) {
            withContext(Dispatchers.IO) {
                isNew = likeSetCard.createdAt > NotificationCache.load(routeForLastRead)

                NotificationCache.markAsRead(routeForLastRead, likeSetCard.createdAt)
            }
        }

        val backgroundColor = if (isNew) {
            MaterialTheme.colors.primary.copy(0.12f).compositeOver(MaterialTheme.colors.background)
        } else {
            MaterialTheme.colors.background
        }

        Column(
            modifier = Modifier.background(backgroundColor).combinedClickable(
                onClick = {
                    routeFor(note, account.userProfile())?.let { navController.navigate(it) }
                },
                onLongClick = { popupExpanded = true }
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        start = if (!isInnerNote) 12.dp else 0.dp,
                        end = if (!isInnerNote) 12.dp else 0.dp,
                        top = 10.dp
                    )
            ) {
                // Draws the like picture outside the boosted card.
                if (!isInnerNote) {
                    Box(
                        modifier = Modifier
                            .width(55.dp)
                            .padding(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_liked),
                            null,
                            modifier = Modifier.size(16.dp).align(Alignment.TopEnd),
                            tint = Color.Unspecified
                        )
                    }
                }

                Column(modifier = Modifier.padding(start = if (!isInnerNote) 10.dp else 0.dp)) {
                    FlowRow() {
                        likeSetCard.likeEvents.forEach {
                            NoteAuthorPicture(
                                baseNote = it,
                                navController = navController,
                                userAccount = account.userProfile(),
                                size = 35.dp
                            )
                        }
                    }

                    NoteCompose(
                        baseNote = note,
                        routeForLastRead = null,
                        modifier = Modifier.padding(top = 5.dp),
                        isBoostedNote = true,
                        parentBackgroundColor = backgroundColor,
                        accountViewModel = accountViewModel,
                        navController = navController
                    )

                    NoteDropDownMenu(note, popupExpanded, { popupExpanded = false }, accountViewModel)
                }
            }
        }
    }
}
