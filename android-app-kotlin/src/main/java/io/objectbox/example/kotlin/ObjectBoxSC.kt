package io.objectbox.example.kotlin

import android.content.Context
import android.util.Log
import io.objectbox.BoxStore
import io.objectbox.BoxStoreBuilder
import io.objectbox.android.Admin
import io.objectbox.android.ObjectBoxLiveData
import io.objectbox.exception.DbException
import io.objectbox.exception.FileCorruptException
import io.objectbox.sync.Sync
import java.io.File
import java.util.*
import java.util.zip.GZIPOutputStream

object ObjectBoxSC {
    val Demo: Boolean = false
    lateinit var boxStore: BoxStore
        private set

    /**
     * If building the [boxStore] failed, contains the thrown error message.
     */
    var dbExceptionMessage: String? = null
        private set

    lateinit var itemsLiveData: ObjectBoxLiveData<Item>
        private set

    fun init(context: Context) {
        // On Android make sure to pass a Context when building the Store.
        boxStore = try {
            MyObjectBox.builder()
                .androidContext(context.applicationContext)
                .build()
        } catch (e: DbException) {
            if (e.javaClass.equals(DbException::class.java) || e is FileCorruptException) {
                // Failed to build BoxStore due to database file issue, store message;
                // checked in ItemListActivity to notify user.
                dbExceptionMessage = e.toString()
                return
            } else {
                // Failed to build BoxStore due to developer error.
                throw e
            }
        }

        if (BuildConfig.DEBUG) {
            var syncAvailable = if (Sync.isAvailable()) "available" else "unavailable"
            Log.d(App.TAG,
                "Using ObjectBox ${BoxStore.getVersion()} (${BoxStore.getVersionNative()}, sync $syncAvailable)")
            // Enable ObjectBox Admin on debug builds.
            // https://docs.objectbox.io/data-browser
            Admin(boxStore).start(context.applicationContext)
        }

        // Prepare a Query for all items, sorted by their date.
        // The Query is not run until find() is called or
        // it is subscribed to (like ObjectBoxLiveData below does).
        // https://docs.objectbox.io/queries
        val itemsQuery = boxStore.boxFor(Item::class.java).query()
            // Sort items by most recent first.
            .orderDesc(Item_.date)
            // ToOne/ToMany by default is loaded on access,
            // so pre-fetch the ToOne to avoid this happening while view binding.
            .build()

        // Wrap Query in a LiveData that subscribes to it only when there are active observers.
        // If only used by a single activity or fragment, maybe keep this in their ViewModel.
        itemsLiveData = ObjectBoxLiveData(itemsQuery)

        //Demo only
        if(Demo) boxStore.removeAllObjects()

        // Add some demo data if the Boxes are empty.
        if (boxStore.boxFor(Item::class.java).isEmpty) {
            replaceWithDemoData()
        }
    }

    private fun replaceWithDemoData() {
        // See that each Item above has a new Author set in its ToOne?
        // When the Item is put, its Author will automatically be put into the Author Box.
        // Both ToOne and ToMany automatically put new Objects when the Object owning them is put.
        // But what if the Author is in the Box already?
        // Then just the relation (Object ID) is updated.

        val item = Item(type="ITEM", legality = "NEUTRAL", title="new", description = "Welcome to CAT", date = Date())
        boxStore.boxFor(Item::class.java).put(item)

        // Sample data for COMMODITY
        val commodity1 = Item(type = "COMMODITY", legality = "NEUTRAL", title = "Commodity 1", description = "Description 1", date = Date())
        val commodity2 = Item(type = "COMMODITY", legality = "NEUTRAL", title = "Commodity 2", description = "Description 2", date = Date())

        // Sample data for LOCATION
        val location1 = Item(type = "LOCATION", legality = "NEUTRAL", title = "Location 1", description = "Description 1", date = Date())
        val location2 = Item(type = "LOCATION", legality = "NEUTRAL", title = "Location 2", description = "Description 2", date = Date())

        // Sample data for buyList and sellList
        val buyList_C = listOf(location1.title.toString(), location2.title.toString()).toMutableList()
        val sellList_C = listOf(location1.title.toString(), location2.title.toString()).toMutableList()
        val buyList_L = listOf(commodity1.title.toString(), commodity2.title.toString()).toMutableList()
        val sellList_L = listOf(commodity1.title.toString(), commodity2.title.toString()).toMutableList()

        // Update the commodity items with the sample data
        commodity1.buyList = buyList_C
        commodity1.sellList = sellList_C

        commodity2.buyList = buyList_C
        commodity2.sellList = sellList_C

        // Update the location items with the sample data
        location1.buyList = buyList_L
        location1.sellList = sellList_L

        location2.buyList = buyList_L
        location2.sellList = sellList_L

        // Assuming you have some specific values for itemBuyPrice, itemSellPrice, itemBuyStorage, itemSellStorage
        // Replace these sample values with your actual data
        val itemBuyPrice = listOf("8.0", "12.0").toMutableList()
        val itemSellPrice = listOf("10.0", "15.0").toMutableList()
        val itemBuyPrice2 = listOf("77.0", "70.0").toMutableList()
        val itemSellPrice2 = listOf("99.0", "90.0").toMutableList()
        val itemBuyStorage = listOf("80", "120").toMutableList()
        val itemSellStorage = listOf("100", "150").toMutableList()

        // Update the item with the sample data
        commodity1.itemBuyPrice = itemBuyPrice
        commodity1.itemSellPrice = itemSellPrice
        commodity2.itemBuyPrice = itemBuyPrice2
        commodity2.itemSellPrice = itemSellPrice2
        commodity1.itemBuyStorage = itemBuyStorage
        commodity1.itemSellStorage = itemSellStorage
        commodity2.itemBuyStorage = itemBuyStorage
        commodity2.itemSellStorage = itemSellStorage

        commodity1.extraTitle = listOf("System").toMutableList()
        commodity1.extraContent = listOf("Stanton").toMutableList()

        location1.itemBuyPrice = itemBuyPrice
        location1.itemSellPrice = itemSellPrice
        location2.itemBuyPrice = itemBuyPrice2
        location2.itemSellPrice = itemSellPrice2
        location1.itemBuyStorage = itemBuyStorage
        location1.itemSellStorage = itemSellStorage
        location2.itemBuyStorage = itemBuyStorage
        location2.itemSellStorage = itemSellStorage

        boxStore.boxFor(Item::class.java).put(commodity1)
        boxStore.boxFor(Item::class.java).put(commodity2)
        boxStore.boxFor(Item::class.java).put(location1)
        boxStore.boxFor(Item::class.java).put(location2)

    }

    /**
     * If the database file is not in use, compresses (GZIP) and copies it to the given [target].
     */
    fun copyAndGzipDatabaseFileTo(target: File, context: Context): Boolean {
        if (BoxStore.isDatabaseOpen(context, null)) {
            // Do not copy if database file is still in use.
            // If it would be open, the copy will likely get corrupted
            // as BoxStore may currently write data to the file.
            Log.e(App.TAG, "Database file is still in use, can not copy.")
            return false
        }

        // If a name was given when building BoxStore use that instead of the default below.
        val dbName = BoxStoreBuilder.DEFAULT_NAME
        File(context.filesDir, "objectbox/$dbName/data.mdb").inputStream().use { input ->
            target.parentFile?.mkdirs()
            GZIPOutputStream(target.outputStream()).use { output ->
                input.copyTo(output, DEFAULT_BUFFER_SIZE)
            }
        }
        return true
    }
}