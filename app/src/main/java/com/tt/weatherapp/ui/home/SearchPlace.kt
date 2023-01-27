package com.tt.weatherapp.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
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

    DisposableEffect(LocalLifecycleOwner.current) {
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
                IconButton(modifier = Modifier.size(24.dp),
                    onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        null
                    )
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(7.dp),
            colors = TextFieldDefaults.textFieldColors(
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
                    Divider()
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
