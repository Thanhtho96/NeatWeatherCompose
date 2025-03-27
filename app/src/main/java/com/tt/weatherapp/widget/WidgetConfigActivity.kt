package com.tt.weatherapp.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.tt.weatherapp.R
import com.tt.weatherapp.common.BaseActivity
import com.tt.weatherapp.data.local.WeatherDao
import com.tt.weatherapp.model.Location
import com.tt.weatherapp.model.LocationType
import com.tt.weatherapp.model.WidgetLocation
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel

class WidgetConfigActivity : BaseActivity<WidgetConfigViewModel>() {

    private val dao by inject<WeatherDao>()

    private suspend fun publishWidget(
        context: Context,
        appWidgetId: Int,
        glanceId: GlanceId,
        location: Location
    ) {
        setResult(RESULT_OK, this.intent)

        dao.insertWidgetLocation(WidgetLocation(appWidgetId, location))

        WidgetUtil.setWidgetState(
            context,
            glanceId,
            WeatherInfo.Available(location)
        )

        finish()
    }

    @Composable
    override fun InitView() {
        val context = LocalContext.current
        val manager = GlanceAppWidgetManager(context)
        val listLocation = viewModel.listLocation.collectAsState()
        val scope = rememberCoroutineScope()

        setResult(RESULT_CANCELED)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        val glanceId = manager.getGlanceIdBy(appWidgetId)

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        Scaffold(
            content = {
                Column(
                    modifier = Modifier
                        .padding(it)
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PaddingValues(vertical = 7.dp, horizontal = 12.dp))
                            .defaultMinSize(minHeight = dimensionResource(id = R.dimen.actionBarSize)),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = stringResource(id = R.string.please_choose_location),
                            color = Color.White,
                            fontSize = 23.sp,
                        )
                    }

                    LazyColumn(contentPadding = PaddingValues(bottom = 17.dp)) {
                        items(listLocation.value) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            publishWidget(
                                                context,
                                                manager.getAppWidgetId(glanceId),
                                                glanceId,
                                                it
                                            )
                                        }
                                    }
                            ) {
                                Spacer(modifier = Modifier.size(12.dp))
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            fontSize = 17.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontWeight = FontWeight.Bold,
                                            text = it.searchName.split(",").first().trim(),
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = it.searchName.split(",").last().trim(),
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontWeight = FontWeight.Light,
                                            color = Color.White
                                        )
                                    }

                                    if (it.type == LocationType.GPS) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            null,
                                            Modifier.size(24.dp),
                                            Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.size(12.dp))
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
        )

    }

    override fun viewModelClass() = getViewModel<WidgetConfigViewModel>()
}
