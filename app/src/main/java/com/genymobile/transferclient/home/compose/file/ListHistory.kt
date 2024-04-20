package com.genymobile.transferclient.home.compose.file

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.genymobile.transferclient.R
import com.genymobile.transferclient.home.MainVm
import com.genymobile.transferclient.tools.FileUtils
import com.genymobile.transferclient.tools.FilesTools
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HorizontalProgressBar(progress: Float) {
    LinearProgressIndicator(
        modifier = Modifier.fillMaxWidth(),
        progress = progress,
        color = Color.Red,
    )
}

@Preview
@Composable
fun ListHistory(vm: MainVm = MainVm(Activity())) {
    LazyColumn(modifier = Modifier.padding(top = 10.dp)) {
        vm.listHistory.forEach { item ->
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            Log.d("FilesContainer", "start: path=${item.downloadPath}")
                            val path: Uri = Uri.parse(item.downloadPath)
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.setDataAndType(null, "*/*");
                            Log.d("FilesContainer", "middle: ")
                            vm.mContext.startActivity(intent)
                            Log.d("FilesContainer", "end: ")
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xffebeaf3))
                        .padding(10.dp)
                ) {
                    Image(painter = painterResource(id = R.drawable.file), contentDescription = "")
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.fileName,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = FileUtils.formatFileSize(item.fileSize),
                            fontSize = 12.sp,
                            color = Color.Gray,
                        )
                    }
                    Column(
                        modifier = Modifier.height(40.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = item.status,
                            fontSize = 14.sp,
                        )
                        Text(
                            text = SimpleDateFormat(
                                "MM月dd日 HH:mm:ss",
                                Locale.getDefault()
                            ).format(
                                item.downloadTime
                            ),
                            color = Color.Gray, fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}