package io.objectbox.example.kotlin

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.relation.ToMany
import io.objectbox.relation.ToOne
import java.io.ByteArrayOutputStream
import java.util.Date

/*
@Entity
data class ExtraData(
        var extraTitle: String = "",
        var extraContent: String = "",
        @Id var id: Long = 0,
){
        lateinit var item: ToOne<Item>
}

@Entity
data class AssessmentData(
        var extraTitle: String = "",
        var extraContent: String = "",
        @Id var id: Long = 0,
){
        lateinit var item: ToOne<Item>
}

@Entity
data class ImageEntity(
        @Id var id: Long = 0,
        var imageData: ByteArray = createDefaultImage(),
) {
        lateinit var item: ToOne<Item>
        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as ImageEntity

                if (id != other.id) return false
                if (!imageData.contentEquals(other.imageData)) return false

                return true
        }
        override fun hashCode(): Int {
                var result = id.hashCode()
                result = 31 * result + imageData.contentHashCode()
                return result
        }
        companion object {
                fun createDefaultImage(): ByteArray {
                        val bitmap = Bitmap.createBitmap(360, 360, Bitmap.Config.ARGB_8888)

                        val canvas = Canvas(bitmap)

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
data class Item(
        var itemBuyPrice: MutableList<String> = mutableListOf(),
        var itemSellPrice: MutableList<String> = mutableListOf(),
        var itemBuyStorage: MutableList<String> = mutableListOf(),
        var itemSellStorage: MutableList<String> = mutableListOf(),
        var buyList: MutableList<String> = mutableListOf(),
        var sellList: MutableList<String> = mutableListOf(),

        @Id var id: Long = 0,
        var type: String = "",
        var title: String = "",

        @Index var date: Date = Date(),
        var description: String = "",
) {
        @Backlink(to = "item") lateinit var extraData: ToMany<ExtraData>
        @Backlink(to = "item") lateinit var assessmentData: ToMany<AssessmentData>
        @Backlink(to = "item") lateinit var Image1: ToMany<ImageEntity>
}




@Entity
data class SearchHistory(
        @Id var id: Long = 0,
        val query: String,
        val timestamp: Date
)

//abandon
/*@Entity
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