package com.tt.weatherapp.utils

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tt.weatherapp.R

object AlertDialogUtils {
    fun showOkCancelDialog(
        context: Context,
        message: String?,
        okListener: DialogInterface.OnClickListener?,
    ) {
        AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton(context.getString(R.string.ok), okListener)
            .setNegativeButton(context.getString(R.string.cancel), okListener)
            .create().apply {
                setCancelable(false)
                show()
            }
    }

    fun showOkDialog(
        context: Context,
        message: String?,
        okListener: DialogInterface.OnClickListener?
    ) {
        AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton(context.getString(R.string.ok), okListener)
            .create()
            .show()
    }
}

@Composable
fun CustomOkDialog(message: String?, onDismiss: () -> Unit) {
    if (message == null) return
    AlertDialog(
        onDismissRequest = {
        },
        title = {
            Row(
                modifier = Modifier
                    .padding(all = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = message)
            }
        },
        buttons = {
            Row(
                modifier = Modifier.padding(all = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onDismiss.invoke() }
                ) {
                    Text(stringResource(id = R.string.ok))
                }
            }
        },
    )
}

@Composable
fun CustomOkCancelDialog(message: String, showDialog: Boolean, setShowDialog: (Boolean) -> Unit) {
    if (showDialog.not()) return
    AlertDialog(
        onDismissRequest = {
        },
        title = {
            Row(
                modifier = Modifier.padding(all = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = message)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Change the state to close the dialog
                    setShowDialog(false)
                },
            ) {
                Text(stringResource(id = R.string.ok))
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    // Change the state to close the dialog
                    setShowDialog(false)
                },
            ) {
                Text(stringResource(id = R.string.cancel))
            }
        },
    )
}

@Composable
fun LoadingDialog(isShow: Boolean) {
    if (isShow.not()) return
    Dialog(
        onDismissRequest = {
        },
        content = { CircularProgressIndicator() }
    )
}
