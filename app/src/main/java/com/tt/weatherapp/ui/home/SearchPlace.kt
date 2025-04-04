package com.tt.weatherapp.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tt.weatherapp.R
import com.tt.weatherapp.model.LocationSuggestion
import com.tt.weatherapp.ui.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun SearchPlace(
    navController: NavController,
    viewModel: MainViewModel,
    onClickSuggestion: (LocationSuggestion) -> Unit
) {
    var inputLocation by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(inputLocation) {
        delay(100)
        viewModel.searchPlaceWithKeyword(inputLocation)
    }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    DisposableEffect(androidx.lifecycle.compose.LocalLifecycleOwner.current) {
        onDispose {
            viewModel.searchPlaceWithKeyword("")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        TextField(
            value = inputLocation,
            onValueChange = { inputLocation = it },
            placeholder = { Text(text = stringResource(id = R.string.enter_location)) },
            leadingIcon = {
                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        null
                    )
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(7.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth()
                .padding(12.dp)
        )

        LazyColumn(contentPadding = PaddingValues(bottom = 12.dp)) {
            items(viewModel.listSuggestion) {
                Column(
                    Modifier.clickable {
                        navController.popBackStack()
                        onClickSuggestion.invoke(it)
                    }) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        modifier = Modifier.padding(start = 12.dp),
                        text = it.title,
                        fontSize = 17.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        modifier = Modifier.padding(start = 12.dp),
                        text = it.detail,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Light
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    HorizontalDivider()
                }
            }
            item {
                Spacer(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .imePadding()
                )
            }
        }
    }
}
