package org.oppia.android.app.classroom

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun ThumbnailImage(modifier: Modifier = Modifier, imageResId: Int) {
  Image(
    painter = painterResource(id = imageResId),
    contentDescription = null,
    modifier = modifier.size(100.dp),
    contentScale = ContentScale.Crop
  )
}
