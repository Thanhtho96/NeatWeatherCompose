package com.tt.weatherapp.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
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

@ExperimentalFoundationApi
@Composable
fun SearchPlace(
    navController: NavController,
    viewModel: MainViewModel,
    onClickSuggestion: (LocationSuggestion) -> Unit
) {
    var inputLocation by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(inputLocation) {
        delay(170)
        viewModel.searchPlaceWithKeyword(inputLocation)
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
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
                .padding(horizontal = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.padding(top = 7.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(viewModel.listSuggestion, key = { it.title + it.detail }) {
                Column(
                    Modifier
                        .animateItemPlacement()
                        .clickable {
                            navController.popBackStack()
                            onClickSuggestion.invoke(it)
                        }) {
                    Spacer(modifier = Modifier.height(9.dp))
                    Text(
                        modifier = Modifier.padding(start = 12.dp),
                        text = it.title,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        modifier = Modifier.padding(start = 12.dp),
                        text = it.detail,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Light
                    )
                    Spacer(modifier = Modifier.height(13.dp))
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
