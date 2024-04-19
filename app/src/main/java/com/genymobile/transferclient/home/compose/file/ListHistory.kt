package com.genymobile.transferclient.home.compose.file

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
fun ListHistory(vm: MainVm){
    LazyColumn(modifier = Modifier.padding(top = 10.dp)) {
        vm.listHistory.forEach { item ->
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            //调用外部文件夹
                            val file = File(item.downloadPath).parentFile
                            Log.d("FilesContainer", "start: ")
                            val path: Uri
                            val intent = Intent(Intent.ACTION_VIEW);
                            path = FileProvider.getUriForFile(
                                vm.mContext, "com.genymobile.transferclient.fileprovider",
                                file
                            );
                            //注意intent用addFlags 如果intent在这行代码下使用setFlags会导致其他应用没有权限打开你的文件
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            val type = FilesTools.getMimeType(file);
                            intent.setDataAndType(path, type);
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = item.fileName, fontSize = 16.sp)
                        Text(
                            text = FileUtils.formatFileSize(item.fileSize),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                        Text(
                            text = SimpleDateFormat(
                                "MM月dd日 HH:mm:ss",
                                Locale.getDefault()
                            ).format(
                                item.downloadTime
                            ),
                            color = Color.Gray, fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}