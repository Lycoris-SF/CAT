package io.objectbox.example.kotlin

import io.objectbox.query.QueryBuilder

enum class AssessInfo_C {
    RefreshRatio,
    AverageProfitRate,
    DangerLocationsCount,
    LegalLocationsNumber,
    LegalBuyStorage,
    LegalSellStorage,
    IlegalLocationsNumber,
    IlegalBuyStorage,
    IlegalSellStorage
}
enum class AssessInfo_L {
    NoFireZone,
    SecurityGuard,
    PirateCount,
    LegalCommoditiesNumber,
    LegalBuyStorage,
    LegalSellStorage,
    IlegalCommoditiesNumber,
    IlegalBuyStorage,
    IlegalSellStorage
}
enum class AssessResultInfo {
    LegalityRate,
    ThreatForSaleRate,
    ProfitChance,
    Legality,
    ThreatForSale,
    Profit
}

class AssessResult {
    var LegalityRate: Int = 50
    var ThreatForSaleRate: Int = 50
    var ProfitChance: Int = 50
    lateinit var Legality: String
    lateinit var ThreatForSale: String
    lateinit var Profit: String

    fun initializeTitles(existingItem: Item) {
        AssessResultInfo.values().forEach { assessInfo ->
            val title = assessInfo.name
            if (existingItem.assessTitle?.contains(title) == false) {
                existingItem.assessTitle?.add(title)
                existingItem.assessContent?.add("")
            }
        }
    }
    fun calResult(existingItem: Item) {
        when {
            LegalityRate < 25 -> {
                Legality = "Outlaw"
            }
            LegalityRate in 25..50 -> {
                Legality = "Contraband"
            }
            LegalityRate in 51..75 -> {
                Legality = "Risky"
            }
            LegalityRate > 75 -> {
                Legality = "Legal"
            }
        }
        var index = existingItem.assessTitle?.indexOf("LegalityRate")
        if (index != null) {
            existingItem.assessContent?.set(index,LegalityRate.toString())
        }
        index = existingItem.assessTitle?.indexOf("Legality")
        if (index != null) {
            existingItem.assessContent?.set(index,Legality)
        }
        when {
            ThreatForSaleRate < 25 -> {
                ThreatForSale = "Low Risk"
            }
            ThreatForSaleRate in 25..50 -> {
                ThreatForSale = "Cautious"
            }
            ThreatForSaleRate in 51..75 -> {
                ThreatForSale = "High Risk"
            }
            ThreatForSaleRate > 75 -> {
                ThreatForSale = "Outlaw"
            }
        }
        index = existingItem.assessTitle?.indexOf("ThreatForSaleRate")
        if (index != null) {
            existingItem.assessContent?.set(index,ThreatForSaleRate.toString())
        }
        index = existingItem.assessTitle?.indexOf("ThreatForSale")
        if (index != null) {
            existingItem.assessContent?.set(index,ThreatForSale)
        }
        // TODO
        when {
            ProfitChance < 25 -> {
                Profit = "Blood Loss"
            }
            ProfitChance in 25..50 -> {
                Profit = "Small Profit"
            }
            ProfitChance in 51..75 -> {
                Profit = "Profitable"
            }
            ProfitChance > 75 -> {
                Profit = "Parvenu"
            }
        }
        index = existingItem.assessTitle?.indexOf("ProfitChance")
        if (index != null) {
            existingItem.assessContent?.set(index,ProfitChance.toString())
        }
        index = existingItem.assessTitle?.indexOf("Profit")
        if (index != null) {
            existingItem.assessContent?.set(index,Profit)
        }
    }

}

class AssessmentActivity {
    private lateinit var existingItem: Item
    private lateinit var assessResult: AssessResult

    fun MakeAssessment(item: Item): Int {
        existingItem = item
        val assessTitle = existingItem.assessTitle
        assessResult = AssessResult()
        assessResult.initializeTitles(existingItem)

        when(existingItem.type){
            "COMMODITY" ->{
                ensureAllAssessInfoCPresent()
                assessTitle?.forEach { title ->
                    title_check_C(title)
                }
                assessResult.calResult(existingItem)
                saveData()
            }
            "LOCATION" ->{
                ensureAllAssessInfoLPresent()
                assessTitle?.forEach { title ->
                    title_check_L(title)
                }
                assessResult.calResult(existingItem)
                saveData()
            }
            else -> return 0
        }
        return 1
    }
    private fun saveData(){
        ObjectBoxSC.boxStore.boxFor(Item::class.java).put(existingItem)
    }
    
    private fun ensureAllAssessInfoCPresent() {
        AssessInfo_C.values().forEach { assessInfo ->
            val title = assessInfo.name
            if (existingItem.assessTitle?.contains(title) == false) {
                existingItem.assessTitle?.add(title)
                existingItem.assessContent?.add("")
            }
        }
    }
    private fun ensureAllAssessInfoLPresent() {
        AssessInfo_L.values().forEach { assessInfo ->
            val title = assessInfo.name
            if (existingItem.assessTitle?.contains(title) == false) {
                existingItem.assessTitle?.add(title)
                existingItem.assessContent?.add("")
            }
        }
    }

    private fun title_check_C(title: String){
        val index = existingItem.assessTitle?.indexOf(title)
        var assessContent = index?.let { existingItem.assessContent?.get(it) }

        try {
            val assessInfo = enumValueOf<AssessInfo_C>(title)
            when(assessInfo){
                AssessInfo_C.RefreshRatio -> {
                    //pass
                }
                AssessInfo_C.AverageProfitRate -> {
                    if (index != null) {
                        val averageProfitRate = calculateAverageProfitRate().toString()
                        existingItem.assessContent?.set(index, averageProfitRate)
                    }
                }
                AssessInfo_C.DangerLocationsCount -> {
                    //not yet
                }
                AssessInfo_C.LegalLocationsNumber ->{
                    if (index != null) {
                        val result = LegalityCount("LEGAL")
                        existingItem.assessContent?.set(index, result.toString())
                    }
                }
                AssessInfo_C.LegalBuyStorage ->{
                    if (index != null) {
                        val result = LegalityCountStorage("LEGAL",true)
                        existingItem.assessContent?.set(index, result.toString())
                    }
                }
                AssessInfo_C.LegalSellStorage ->{
                    if (index != null) {
                        val result = LegalityCountStorage("LEGAL",false)
                        existingItem.assessContent?.set(index, result.toString())
                    }
                }
                AssessInfo_C.IlegalLocationsNumber ->{
                    if (index != null) {
                        val result = LegalityCount("CONTRABAND")
                        existingItem.assessContent?.set(index, result.toString())
                    }
                }
                AssessInfo_C.IlegalBuyStorage ->{
                    if (index != null) {
                        val result = LegalityCountStorage("CONTRABAND",true)
                        existingItem.assessContent?.set(index, result.toString())
                    }
                }
                AssessInfo_C.IlegalSellStorage ->{
                    if (index != null) {
                        val result = LegalityCountStorage("CONTRABAND",false)
                        existingItem.assessContent?.set(index, result.toString())
                    }
                }
            }
        } catch (e: IllegalArgumentException) {

        }
    }
    private fun title_check_L(title: String){
        val index = existingItem.assessTitle?.indexOf(title)
        val assessContent = index?.let { existingItem.assessContent?.get(it) }

        try {
            val assessInfo = enumValueOf<AssessInfo_L>(title)
            when(assessInfo){
                AssessInfo_L.NoFireZone -> {
                    if (index != null){
                        if(assessContent == "Yes" || assessContent == "YES" || assessContent == "TRUE" || assessContent == "True" || assessContent == "1")
                            assessResult.ThreatForSaleRate -= 5
                        else if(assessContent == "No" || assessContent == "NO" || assessContent == "FALSE" || assessContent == "false" || assessContent == "0")
                            assessResult.ThreatForSaleRate += 2
                    }
                }
                AssessInfo_L.SecurityGuard -> {
                    if (index != null){
                        if(assessContent == "Yes" || assessContent == "YES" || assessContent == "TRUE" || assessContent == "True" || assessContent == "1")
                            assessResult.ThreatForSaleRate -= 3
                        else if(assessContent == "No" || assessContent == "NO" || assessContent == "FALSE" || assessContent == "false" || assessContent == "0")
                            assessResult.ThreatForSaleRate += 3
                    }
                }
                AssessInfo_L.PirateCount -> {
                    if (index != null) {
                        if (assessContent != null) {
                            assessResult.ThreatForSaleRate += 7*assessContent.toInt()
                        }
                    }
                }
                AssessInfo_L.LegalCommoditiesNumber -> {
                    if (index != null) {
                        val result = LegalityCount("LEGAL")
                        existingItem.assessContent?.set(index, result.toString())
                        assessResult.LegalityRate += result*3
                    }
                }
                AssessInfo_L.LegalBuyStorage -> {
                    if (index != null) {
                        val result = LegalityCountStorage("LEGAL",true)
                        existingItem.assessContent?.set(index, result.toString())
                    }
                }
                AssessInfo_L.LegalSellStorage -> {
                    if (index != null) {
                        val result = LegalityCountStorage("LEGAL",false)
                        existingItem.assessContent?.set(index, result.toString())
                    }
                }
                AssessInfo_L.IlegalCommoditiesNumber -> {
                    if (index != null) {
                        val result = LegalityCount("CONTRABAND")
                        existingItem.assessContent?.set(index, result.toString())
                        assessResult.LegalityRate -= result*3
                    }
                }
                AssessInfo_L.IlegalBuyStorage -> {
                    if (index != null) {
                        val result = LegalityCountStorage("CONTRABAND",true)
                        existingItem.assessContent?.set(index, result.toString())
                    }
                }
                AssessInfo_L.IlegalSellStorage -> {
                    if (index != null) {
                        val result = LegalityCountStorage("CONTRABAND",false)
                        existingItem.assessContent?.set(index, result.toString())
                    }
                }
            }
        } catch (e: IllegalArgumentException) {

        }
    }
    
    fun calculateAverageProfitRate(): Float {
        val buyPrices = existingItem.itemBuyPrice?.mapNotNull { it.toFloatOrNull() }
        val sellPrices = existingItem.itemSellPrice?.mapNotNull { it.toFloatOrNull() }

        if (buyPrices != null) {
            if (sellPrices != null) {
                if (buyPrices.isEmpty() || sellPrices.isEmpty()) {
                    return 0f
                }
            }
        }

        val averageBuyPrice = buyPrices?.average()?.toFloat()
        val averageSellPrice = sellPrices?.average()?.toFloat()

        if (averageBuyPrice != null) {
            if (averageSellPrice != null) {
                return if (averageBuyPrice > 0) {
                    ((averageSellPrice - averageBuyPrice) / averageBuyPrice) * 100
                } else {
                    0f
                }
            }
        }
        return 0f
    }
    fun LegalityCount(type: String): Int {
        val buyList = existingItem.buyList
        val sellList = existingItem.sellList
        var sum = 0

        try {
            if (buyList != null) {
                for (query in buyList) {
                    val searchQuery = ObjectBoxSC.boxStore.boxFor(Item::class.java)
                        .query().equal(Item_.title, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).build()
                    val result = searchQuery.find()
                    searchQuery.close()

                    if(result.size != 0)
                        if (type == result[0].legality) sum++
                }
            }
            if (sellList != null) {
                for (query in sellList) {
                    val searchQuery = ObjectBoxSC.boxStore.boxFor(Item::class.java)
                        .query().equal(Item_.title, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).build()
                    val result = searchQuery.find()
                    searchQuery.close()

                    if(result.size != 0)
                        if (type == result[0].legality) sum++
                }
            }
        } catch (e: Exception) {
            return -1
        }
        return sum
    }
    fun LegalityCountStorage(type: String, BuyOrSell: Boolean): Int {
        val buyList = existingItem.buyList
        val sellList = existingItem.sellList
        val buyStorage = existingItem.itemBuyStorage
        val sellStorage = existingItem.itemSellStorage
        var sum = 0

        try {
            if(BuyOrSell){
                if (buyStorage != null&& buyList!= null) {
                    for (query in buyList) {
                        val searchQuery = ObjectBoxSC.boxStore.boxFor(Item::class.java)
                            .query().equal(Item_.title, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).build()
                        val result = searchQuery.find()
                        searchQuery.close()

                        if(result.size != 0){
                            val index = buyList.indexOf(query)
                            if (type == result[0].legality) sum += buyStorage[index].toInt()
                        }
                    }
                }
            }
            else{
                if (sellStorage != null&& sellList!= null) {
                    for (query in sellList) {
                        val searchQuery = ObjectBoxSC.boxStore.boxFor(Item::class.java)
                            .query().equal(Item_.title, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).build()
                        val result = searchQuery.find()
                        searchQuery.close()

                        if(result.size != 0){
                            val index = sellList.indexOf(query)
                            if (type == result[0].legality) sum += sellStorage[index].toInt()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            return -1
        }
        return sum
    }
}


