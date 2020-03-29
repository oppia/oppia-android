package org.oppia.util.parser

import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import java.io.ByteArrayInputStream
import java.io.InputStream

// https://bumptech.github.io/glide/tut/custom-modelloader.html#an-empty-implementation.
/** ModelLoader for loading assets from the app's local asset repository. */
internal class RepositoryModelLoader : ModelLoader<ImageAssetFetcher, InputStream> {
  override fun buildLoadData(
    model: ImageAssetFetcher,
    width: Int,
    height: Int,
    options: Options
  ): ModelLoader.LoadData<InputStream>? {
    return ModelLoader.LoadData(ObjectKey(model.getImageIdentifier()), RepositoryDataFetcher(model))
  }

  override fun handles(model: ImageAssetFetcher): Boolean = true

  private class RepositoryDataFetcher(private val fetcher: ImageAssetFetcher) : DataFetcher<InputStream> {
    override fun getDataClass(): Class<InputStream> = InputStream::class.java

    override fun cleanup() {}

    override fun getDataSource(): DataSource = DataSource.REMOTE

    override fun cancel() {}

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
      val imageData = fetcher.fetchImage()
      callback.onDataReady(ByteArrayInputStream(imageData))
    }
  }

  /** [ModelLoaderFactory] for creating new [RepositoryModelLoader]s. */
  internal class Factory : ModelLoaderFactory<ImageAssetFetcher, InputStream> {
    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<ImageAssetFetcher, InputStream> {
      return RepositoryModelLoader()
    }

    override fun teardown() {}
  }
}
