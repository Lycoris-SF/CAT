package io.objectbox.example.kotlin

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.converter.PropertyConverter
import io.objectbox.relation.ToMany
import io.objectbox.relation.ToOne
import java.io.ByteArrayOutputStream
import java.util.Date

enum class ItemType {
        ITEM,
        COMMODITY,
        LOCATION,
}
enum class ItemLegal {
        NEUTRAL,
        LEGAL,
        CONTRABAND,
}

@Entity
data class Item(
        var itemBuyPrice: MutableList<String>? = null,
        var itemSellPrice: MutableList<String>? = null,
        var itemBuyStorage: MutableList<String>? = null,
        var itemSellStorage: MutableList<String>? = null,
        var buyList: MutableList<String>? = null,
        var sellList: MutableList<String>? = null,
        var extraTitle: MutableList<String>? = null,
        var extraContent: MutableList<String>? = null,
        var assessTitle: MutableList<String>? = null,
        var assessContent: MutableList<String>? = null,
        var imageData1: ByteArray?,
        var imageData2: ByteArray?,
        // Every @Entity requires a Long @Id property.
        // The default value 0 signals that this is a new Object.
        @Id
        var id: Long = 0,
        var type: String? = null,
        var title: String? = null,

        @Index // Improves query performance at the cost of storage space.
        var date: Date? = null,
        var description: String? = null,
        var legality: String? = null,

        ){
        constructor() : this(null, null, null,null, null, null, null, null, null, null, null, null,0, null, null, null, null , null){
                this.type = "Item"
                this.title = ""
                this.description = ""
                this.legality = ""
                this.date = Date()
                this.itemBuyPrice = mutableListOf()
                this.itemSellPrice = mutableListOf()
                this.itemBuyStorage = mutableListOf()
                this.itemSellStorage = mutableListOf()
                this.buyList = mutableListOf()
                this.sellList = mutableListOf()
                this.extraTitle = mutableListOf()
                this.extraContent = mutableListOf()
                this.assessTitle = mutableListOf()
                this.assessContent = mutableListOf()
                this.imageData1 = createDefaultImage()
                this.imageData2 = createDefaultImage()
        }
        constructor(type: String?, legality: String?, title: String?, description: String?, date: Date?) : this() {
                this.type = type
                this.legality = legality
                this.title = title
                this.description = description
                this.date = date
        }
        constructor(
                original: Item,
                itemBuyPrice: MutableList<String>? = null,
                itemSellPrice: MutableList<String>? = null,
                itemBuyStorage: MutableList<String>? = null,
                itemSellStorage: MutableList<String>? = null,
                buyList: MutableList<String>? = null,
                sellList: MutableList<String>? = null,
                extraTitle: MutableList<String>? = null,
                extraContent: MutableList<String>? = null,
                assessTitle: MutableList<String>? = null,
                assessContent: MutableList<String>? = null,
                imageData1: ByteArray? = null,
                imageData2: ByteArray? = null,
        ) : this(
                type = original.type,
                title = original.title,
                description = original.description,
                legality = original.legality,
                itemBuyPrice = itemBuyPrice,
                itemSellPrice = itemSellPrice,
                itemBuyStorage = itemBuyStorage,
                itemSellStorage = itemSellStorage,
                buyList = buyList,
                sellList = sellList,
                extraTitle = extraTitle,
                extraContent = extraContent,
                assessTitle = assessTitle,
                assessContent = assessContent,
                imageData1 = imageData1,
                imageData2 = imageData2,
        )
        companion object {
                fun createDefaultImage(): ByteArray {
                        val bitmap = Bitmap.createBitmap(360, 360, Bitmap.Config.ARGB_8888)

                        val canvas = Canvas(bitmap)
                        canvas.drawColor(Color.GRAY)

                        val paint = Paint()
                        paint.color = Color.RED
                        paint.textSize = 80f
                        paint.textAlign = Paint.Align.CENTER

                        val text = "404"

                        val x = bitmap.width / 2f
                        val y = (bitmap.height / 2f) - ((paint.descent() + paint.ascent()) / 2f)

                        canvas.drawText(text, x, y, paint)

                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        return stream.toByteArray()
                }
        }
}

@Entity
data class SearchHistory(
        @Id var id: Long = 0,
        val query: String,
        val timestamp: Date
)

//abandon
/*
@Entity
data class ImageEntity(
        @Id var id: Long = 0,
        var imageData: ByteArray?
) {
        lateinit var item: ToOne<Item>
        constructor() : this(0,null){
                this.imageData = createDefaultImage()
        }
        companion object {
                fun createDefaultImage(): ByteArray {
                        val bitmap = Bitmap.createBitmap(360, 360, Bitmap.Config.ARGB_8888)

                        val canvas = Canvas(bitmap)
                        canvas.drawColor(Color.GRAY)

                        val paint = Paint()
                        paint.color = Color.RED
                        paint.textSize = 80f
                        paint.textAlign = Paint.Align.CENTER

                        val text = "404"

                        val x = bitmap.width / 2f
                        val y = (bitmap.height / 2f) - ((paint.descent() + paint.ascent()) / 2f)

                        canvas.drawText(text, x, y, paint)

                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        return stream.toByteArray()
                }
        }
}
@Entity
data class ExtraData(
        var extraTitle: MutableList<String> = createDefaultList("System"),
        var extraContent: MutableList<String> = createDefaultList("Standon"),
        @Id var id: Long = 0,
) {
        lateinit var item: ToOne<Item>
        constructor(original: ExtraData) : this(
                id = 0,
                extraTitle = original.extraTitle.toMutableList(),
                extraContent = original.extraContent.toMutableList()
        )
        companion object {
                fun createDefaultList(str: String): MutableList<String>{
                        return mutableListOf(str)
                }
        }
}
@Entity
data class AssessmentData(
        var extraTitle: MutableList<String> = createDefaultList("Dangerous"),
        var extraContent: MutableList<String> = createDefaultList("Not"),
        @Id var id: Long = 0,
) {
        lateinit var item: ToOne<Item>
        constructor(original: AssessmentData) : this(
                id = 0,
                extraTitle = original.extraTitle.toMutableList(),
                extraContent = original.extraContent.toMutableList()
        )
        companion object {
                fun createDefaultList(str: String): MutableList<String>{
                        return mutableListOf(str)
                }
        }
}
@Entity
data class Commodity(
        var item: ToOne<Item>? = null,

        @Id(assignable = true)
        var id: Long = 0,

        @Relation(to = "commodities")
        var locations: ToMany<Location>? = null
)

annotation class Relation(val to: String)

@Entity
data class Location(
        var item: ToOne<Item>? = null,

        @Id(assignable = true)
        var id: Long = 0,

        @Relation(to = "locations")
        var commodities: ToMany<Commodity>? = null
)
*/