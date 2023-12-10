package io.objectbox.example.kotlin

import java.util.Date
import io.objectbox.query.QueryBuilder

class QueryActivity {
    companion object {
        fun searchForItemByName(name: String): List<Item> {
            val lowerCaseName = name.lowercase()

            val itemBox = ObjectBoxSC.boxStore.boxFor(Item::class.java)
            val searchQuery = itemBox.query()
                .equal(Item_.title, lowerCaseName, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                .build()

            try {
                return searchQuery.find()
            } finally {
                searchQuery.close()
            }
        }

        fun tempAddNewItem(existingItem: Item, newItemTitle: String, price: String, storage: String, isBuy: Boolean){
            val searchResults = searchForItemByName(newItemTitle)
            var newItem: Item
            if (searchResults.isEmpty()){
                val type = if(existingItem.type=="COMMODITY"){"LOCATION"} else {"COMMODITY"}
                newItem = Item(type = type, legality = "NEUTRAL", title = newItemTitle, description = "Not Given", date = Date())
                val itemId = ObjectBoxSC.boxStore.boxFor(Item::class.java).put(newItem)
            }
            else newItem = searchResults.first()
            if(isBuy){
                if(newItem.buyList?.contains(existingItem.title.toString()) == false) {
                    newItem.buyList?.add(existingItem.title.toString())
                    val newPrice = price.toFloat()
                    newItem.itemBuyPrice?.add(String.format("%.2f", newPrice))
                    newItem.itemBuyStorage?.add(storage)
                }
                else{
                    val index = newItem.buyList?.indexOf(existingItem.title.toString())
                    if (index != null){
                        val newPrice = (newItem.itemBuyPrice?.get(index)?.toFloat()?.plus(price.toFloat()))?.div(2)
                        val newStorage = (newItem.itemBuyStorage?.get(index)?.toFloat()?.plus(storage.toFloat()))?.div(2)?.toInt()
                        newItem.itemBuyPrice?.set(index,String.format("%.2f", newPrice))
                        newItem.itemBuyStorage?.set(index,newStorage.toString())
                    }
                }
            }
            else{
                if(newItem.sellList?.contains(existingItem.title.toString()) == false) {
                    newItem.sellList?.add(existingItem.title.toString())
                    val newPrice = price.toFloat()
                    newItem.itemSellPrice?.add(String.format("%.2f", newPrice))
                    newItem.itemSellStorage?.add(storage)
                }
                else{
                    val index = newItem.sellList?.indexOf(existingItem.title.toString())
                    if (index != null){
                        val newPrice = (newItem.itemSellPrice?.get(index)?.toFloat()?.plus(price.toFloat()))?.div(2)
                        val newStorage = (newItem.itemSellStorage?.get(index)?.toFloat()?.plus(storage.toFloat()))?.div(2)?.toInt()
                        newItem.itemSellPrice?.set(index,String.format("%.2f", newPrice))
                        newItem.itemSellStorage?.set(index,newStorage.toString())
                    }
                }
            }
            ObjectBoxSC.boxStore.boxFor(Item::class.java).put(newItem)
        }

        fun mergeData(existingItem: Item){
            if(existingItem.buyList?.isNotEmpty() == true) {
                var nameList: MutableList<String> = mutableListOf()
                var nameListCount: MutableList<Int> = mutableListOf()
                var priceList: MutableList<Float> = mutableListOf()
                var storageList: MutableList<Int> = mutableListOf()
                for (index in existingItem.buyList!!.indices) {
                    var item = existingItem.buyList?.get(index)
                    val searchResults = item?.let { searchForItemByName(it) }
                    if (searchResults.isNullOrEmpty()){
                        continue
                    }
                    else {
                        item = item?.let { searchForItemByName(it).first().title }
                        if (item in nameList) {
                            val subIndex = nameList.indexOf(item)
                            nameListCount[subIndex] = nameListCount[subIndex] + 1
                            existingItem.itemBuyPrice?.get(index)?.let {
                                val priceToAdd = it.toFloatOrNull()
                                if (priceToAdd != null) {
                                    priceList[subIndex] = priceList[subIndex] + priceToAdd
                                }
                            }

                            existingItem.itemBuyStorage?.get(index)?.let {
                                val storageToAdd = it.toIntOrNull()
                                if (storageToAdd != null) {
                                    storageList[subIndex] = storageList[subIndex] + storageToAdd
                                }
                            }
                        } else {
                            if (item != null) {
                                nameList.add(item)
                            }
                            nameListCount.add(1)
                            existingItem.itemBuyPrice?.get(index)?.toFloat()
                                ?.let { priceList.add(it) }
                            existingItem.itemBuyStorage?.get(index)?.toInt()
                                ?.let { storageList.add(it) }
                        }
                    }
                }
                for(item in nameList){
                    val subIndex = nameList.indexOf(item)
                    val searchResults = searchForItemByName(item)
                    priceList[subIndex] = priceList[subIndex].div(nameListCount[subIndex])
                    storageList[subIndex] = storageList[subIndex].div(nameListCount[subIndex])
                    //sync
                    mergeData_NoMoreDepth(searchResults.first())
                    val targetIndex = searchResults.first().buyList?.indexOf(existingItem.title.toString())
                    if (targetIndex != null && targetIndex!=-1) {
                        val mergePrice = (searchResults.first().itemBuyPrice?.get(targetIndex)
                            ?.toFloat()?.plus(priceList[subIndex]))?.div(2)
                        val mergeStorage = (searchResults.first().itemBuyStorage?.get(targetIndex)
                            ?.toInt()?.plus(storageList[subIndex]))?.div(2)
                        searchResults.first().itemBuyPrice?.set(targetIndex,String.format("%.2f", mergePrice))
                        searchResults.first().itemBuyStorage?.set(targetIndex,mergeStorage.toString())
                        if (mergePrice != null) {
                            priceList[subIndex] = mergePrice
                        }
                        if (mergeStorage != null) {
                            storageList[subIndex] = mergeStorage
                        }
                    }
                    ObjectBoxSC.boxStore.boxFor(Item::class.java).put(searchResults.first())

                }
                existingItem.buyList = nameList
                existingItem.itemBuyPrice = priceList.map { String.format("%.2f", it) }.toMutableList()
                existingItem.itemBuyStorage = storageList.map { it.toString() }.toMutableList()
            }
            if(existingItem.sellList?.isNotEmpty() == true) {
                var nameList: MutableList<String> = mutableListOf()
                var nameListCount: MutableList<Int> = mutableListOf()
                var priceList: MutableList<Float> = mutableListOf()
                var storageList: MutableList<Int> = mutableListOf()
                for (index in existingItem.sellList!!.indices) {
                    var item = existingItem.sellList?.get(index)
                    val searchResults = item?.let { searchForItemByName(it) }
                    if (searchResults.isNullOrEmpty()){
                        continue
                    }
                    else {
                        if (item in nameList) {
                            val subIndex = nameList.indexOf(item)
                            nameListCount[subIndex] = nameListCount[subIndex] + 1
                            existingItem.itemSellPrice?.get(index)?.let {
                                val priceToAdd = it.toFloatOrNull()
                                if (priceToAdd != null) {
                                    priceList[subIndex] = priceList[subIndex] + priceToAdd
                                }
                            }

                            existingItem.itemSellStorage?.get(index)?.let {
                                val storageToAdd = it.toIntOrNull()
                                if (storageToAdd != null) {
                                    storageList[subIndex] = storageList[subIndex] + storageToAdd
                                }
                            }
                        } else {
                            if (item != null) {
                                nameList.add(item)
                            }
                            nameListCount.add(1)
                            existingItem.itemSellPrice?.get(index)?.toFloat()
                                ?.let { priceList.add(it) }
                            existingItem.itemSellStorage?.get(index)?.toInt()
                                ?.let { storageList.add(it) }
                        }
                    }
                }
                for(item in nameList){
                    val subIndex = nameList.indexOf(item)
                    val searchResults = searchForItemByName(item)
                    priceList[subIndex] = priceList[subIndex].div(nameListCount[subIndex])
                    storageList[subIndex] = storageList[subIndex].div(nameListCount[subIndex])
                    //sync
                    mergeData_NoMoreDepth(searchResults.first())
                    val targetIndex = searchResults.first().sellList?.indexOf(existingItem.title.toString())
                    if (targetIndex != null && targetIndex!=-1) {
                        val mergePrice = (searchResults.first().itemSellPrice?.get(targetIndex)
                            ?.toFloat()?.plus(priceList[subIndex]))?.div(2)
                        val mergeStorage = (searchResults.first().itemSellStorage?.get(targetIndex)
                            ?.toInt()?.plus(storageList[subIndex]))?.div(2)
                        searchResults.first().itemSellPrice?.set(targetIndex,String.format("%.2f", mergePrice))
                        searchResults.first().itemSellStorage?.set(targetIndex,mergeStorage.toString())
                        if (mergePrice != null) {
                            priceList[subIndex] = mergePrice
                        }
                        if (mergeStorage != null) {
                            storageList[subIndex] = mergeStorage
                        }
                    }
                    ObjectBoxSC.boxStore.boxFor(Item::class.java).put(searchResults.first())

                }
                existingItem.sellList = nameList
                existingItem.itemSellPrice = priceList.map { String.format("%.2f", it) }.toMutableList()
                existingItem.itemSellStorage = storageList.map { it.toString() }.toMutableList()
            }
            //ObjectBoxSC.boxStore.boxFor(Item::class.java).put(existingItem)
        }
        fun mergeData_NoMoreDepth(existingItem: Item){
            if(existingItem.buyList?.isNotEmpty() == true) {
                var nameList: MutableList<String> = mutableListOf()
                var nameListCount: MutableList<Int> = mutableListOf()
                var priceList: MutableList<Float> = mutableListOf()
                var storageList: MutableList<Int> = mutableListOf()
                for (index in existingItem.buyList!!.indices) {
                    var item = existingItem.buyList?.get(index)
                    val searchResults = item?.let { searchForItemByName(it) }
                    if (searchResults.isNullOrEmpty()){
                        continue
                    }
                    else {
                        if (item in nameList) {
                            val subIndex = nameList.indexOf(item)
                            nameListCount[subIndex] = nameListCount[subIndex] + 1
                            existingItem.itemBuyPrice?.get(index)?.let {
                                val priceToAdd = it.toFloatOrNull()
                                if (priceToAdd != null) {
                                    priceList[subIndex] = priceList[subIndex] + priceToAdd
                                }
                            }

                            existingItem.itemBuyStorage?.get(index)?.let {
                                val storageToAdd = it.toIntOrNull()
                                if (storageToAdd != null) {
                                    storageList[subIndex] = storageList[subIndex] + storageToAdd
                                }
                            }
                        } else {
                            if (item != null) {
                                nameList.add(item)
                            }
                            nameListCount.add(1)
                            existingItem.itemBuyPrice?.get(index)?.toFloat()
                                ?.let { priceList.add(it) }
                            existingItem.itemBuyStorage?.get(index)?.toInt()
                                ?.let { storageList.add(it) }
                        }
                    }
                }
                for(item in nameList){
                    val subIndex = nameList.indexOf(item)
                    priceList[subIndex] = priceList[subIndex].div(nameListCount[subIndex])
                    storageList[subIndex] = storageList[subIndex].div(nameListCount[subIndex])
                }
                existingItem.buyList = nameList
                existingItem.itemBuyPrice = priceList.map { String.format("%.2f", it) }.toMutableList()
                existingItem.itemBuyStorage = storageList.map { it.toString() }.toMutableList()
            }
            if(existingItem.sellList?.isNotEmpty() == true) {
                var nameList: MutableList<String> = mutableListOf()
                var nameListCount: MutableList<Int> = mutableListOf()
                var priceList: MutableList<Float> = mutableListOf()
                var storageList: MutableList<Int> = mutableListOf()
                for (index in existingItem.sellList!!.indices) {
                    var item = existingItem.sellList?.get(index)
                    val searchResults = item?.let { searchForItemByName(it) }
                    if (searchResults.isNullOrEmpty()){
                        continue
                    }
                    else {
                        if (item in nameList) {
                            val subIndex = nameList.indexOf(item)
                            nameListCount[subIndex] = nameListCount[subIndex].plus(1)
                            existingItem.itemSellPrice?.get(index)
                                ?.let {
                                    priceList[subIndex] = priceList[subIndex].plus(it.toFloat())
                                }
                            existingItem.itemSellStorage?.get(index)
                                ?.let {
                                    storageList[subIndex] = storageList[subIndex].plus(it.toInt())
                                }
                        } else {
                            if (item != null) {
                                nameList.add(item)
                            }
                            nameListCount.add(1)
                            existingItem.itemSellPrice?.get(index)?.toFloat()
                                ?.let { priceList.add(it) }
                            existingItem.itemSellStorage?.get(index)?.toInt()
                                ?.let { storageList.add(it) }
                        }
                    }
                }
                for(item in nameList){
                    val subIndex = nameList.indexOf(item)
                    priceList[subIndex] = priceList[subIndex].div(nameListCount[subIndex])
                    storageList[subIndex] = storageList[subIndex].div(nameListCount[subIndex])
                }
                existingItem.sellList = nameList
                existingItem.itemSellPrice = priceList.map { String.format("%.2f", it) }.toMutableList()
                existingItem.itemSellStorage = storageList.map { it.toString() }.toMutableList()
            }
            //ObjectBoxSC.boxStore.boxFor(Item::class.java).put(existingItem)
        }
    }
}