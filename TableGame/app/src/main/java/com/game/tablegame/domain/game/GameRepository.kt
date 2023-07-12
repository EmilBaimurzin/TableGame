package com.game.tablegame.domain.game

class GameRepository {
    fun generateList(): List<GameItem> {
        val items = mutableListOf<GameItem>()
        val randomIndexes = mutableListOf<Int>()
        repeat(18) {
            randomIndexes.add(it)
            items.add(GameItem(value = getRandomItem()))
        }
        repeat(2) {
            val randomIndex = randomIndexes.random()
            items[randomIndex].value = ItemValue.BOMB
            randomIndexes.remove(randomIndex)
        }
        repeat(2) {
            val randomIndex = randomIndexes.random()
            items[randomIndex].value = ItemValue.COIN
            randomIndexes.remove(randomIndex)
        }
        return items
    }

    private fun getRandomItem(): ItemValue {
        return listOf(
            ItemValue.ITEM1,
            ItemValue.ITEM2,
            ItemValue.ITEM3,
            ItemValue.ITEM4,
        ).random()
    }
}