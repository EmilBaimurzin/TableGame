package com.game.tablegame.ui.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.game.tablegame.core.library.l
import com.game.tablegame.core.library.random
import com.game.tablegame.domain.game.GameItem
import com.game.tablegame.domain.game.GameRepository
import com.game.tablegame.domain.game.ItemValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {
    private val repository = GameRepository()
    var enemyGates = 0f to 0f
    var playerGates = 0f to 0f
    private val _playerPosition = MutableLiveData(0f to 0f)
    val playerPosition: LiveData<Pair<Float, Float>> = _playerPosition

    private val _enemyPosition = MutableLiveData(0f to 0f)
    val enemyPosition: LiveData<Pair<Float, Float>> = _enemyPosition

    private val _playerInventory = MutableLiveData<GameItem?>(null)
    val playerInventory: LiveData<GameItem?> = _playerInventory

    private val _enemyInventory = MutableLiveData<GameItem?>(null)
    val enemyInventory: LiveData<GameItem?> = _enemyInventory

    private val _playerScore = MutableLiveData(0)
    val playerScore: LiveData<Int> = _playerScore

    private val _enemyScore = MutableLiveData(0)
    val enemyScore: LiveData<Int> = _enemyScore

    private val _timer = MutableLiveData(60)
    val timer: LiveData<Int> = _timer

    private val _items = MutableLiveData(repository.generateList())
    val items: LiveData<List<GameItem>> = _items

    private var gameScope = CoroutineScope(Dispatchers.Default)

    fun setItemsPosition(minX: Float, maxX: Float, minY: Float, maxY: Float) {
        viewModelScope.launch(Dispatchers.Default) {
            val newList = mutableListOf<GameItem>()
            _items.value!!.forEach { item ->
                newList.add(
                    GameItem(
                        x = (minX.toInt() random maxX.toInt()).toFloat(),
                        y = (minY.toInt() random maxY.toInt()).toFloat(),
                        value = item.value
                    )
                )
            }
            _items.postValue(newList)
        }
    }

    init {
        startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            repeat(60) {
                _timer.postValue(_timer.value!! - 1)
                delay(1000)
            }
        }
    }

    fun addToPlayerInventory(item: GameItem) = _playerInventory.postValue(item)
    fun removeFromPlayerInventory() = _playerInventory.postValue(null)

    fun addToEnemyInventory(item: GameItem) = _enemyInventory.postValue(item)
    fun removeFromEnemyInventory() = _enemyInventory.postValue(null)

    fun playerMoveUp(limit: Float) {
        if (_playerPosition.value!!.second - 1f > limit) {
            _playerPosition.postValue(_playerPosition.value!!.first to _playerPosition.value!!.second - 1f)
        }
    }

    fun playerMoveDown(limit: Float) {
        if (_playerPosition.value!!.second + 1f < limit) {
            _playerPosition.postValue(_playerPosition.value!!.first to _playerPosition.value!!.second + 1f)
        }
    }

    fun playerMoveLeft(limit: Float) {
        if (_playerPosition.value!!.first - 1f > limit) {
            _playerPosition.postValue(_playerPosition.value!!.first - 1 to _playerPosition.value!!.second)
        }
    }

    fun removeItem(x: Float, y: Float) {
        val newList = _items.value!!.toMutableList()
        val item = newList.find { it.x == x && it.y == y }
        newList.removeAll { it == item }
        _items.postValue(newList)
    }

    fun playerMoveRight(limit: Float) {
        if (_playerPosition.value!!.first + 1f < limit) {
            _playerPosition.postValue(_playerPosition.value!!.first + 1 to _playerPosition.value!!.second)
        }
    }

    fun increasePlayerScore(value: Int) {
        _playerScore.postValue(_playerScore.value!! + value)
    }

    fun increaseEnemyScore(value: Int) {
        _enemyScore.postValue(_enemyScore.value!! + value)
    }

    fun moveEnemyToRandomItem() {
        gameScope.launch {
            if (items.value!!.isNotEmpty()) {
                val randomItem = _items.value?.random()
                randomItem?.let { item ->
                    if (_items.value!!.contains(item)) {
                        moveEnemy(x = item.x, y = item.y) {
                            if (_enemyInventory.value?.value == ItemValue.BOMB) {
                                moveToPlayersGate {
                                    l("first")
                                    moveEnemyToRandomItem()
                                }
                            } else {
                                moveToEnemyGate {
                                    l("second")
                                    moveEnemyToRandomItem()
                                }
                            }
                        }
                    } else {
                        moveEnemyToRandomItem()
                    }
                }
            }
        }
    }

    private fun getRandomItem(): GameItem {
        return _items.value!!.random()
    }

    private fun moveToEnemyGate(callback: () -> Unit) {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            while (true) {
                if (gameScope.isActive) {
                    if (_enemyInventory.value == null) {
                        callback.invoke()
                        scope.cancel()
                    }

                    if (_enemyPosition.value!!.first > enemyGates.first) {
                        _enemyPosition.postValue(_enemyPosition.value!!.first - 1 to _enemyPosition.value!!.second)
                    } else {
                        _enemyPosition.postValue(_enemyPosition.value!!.first + 1 to _enemyPosition.value!!.second)
                    }

                    if (_enemyPosition.value!!.second > enemyGates.second) {
                        _enemyPosition.postValue(_enemyPosition.value!!.first to _enemyPosition.value!!.second - 1)
                    } else {
                        _enemyPosition.postValue(_enemyPosition.value!!.first to _enemyPosition.value!!.second + 1)
                    }
                    delay(6)
                } else {
                    scope.cancel()
                }
            }
        }
    }

    private fun moveToPlayersGate(callback: () -> Unit) {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            while (true) {
                if (gameScope.isActive) {
                    if (_enemyInventory.value == null) {
                        callback.invoke()
                        scope.cancel()
                    }

                    if (_enemyPosition.value!!.first > playerGates.first) {
                        _enemyPosition.postValue(_enemyPosition.value!!.first - 1 to _enemyPosition.value!!.second)
                    } else {
                        _enemyPosition.postValue(_enemyPosition.value!!.first + 1 to _enemyPosition.value!!.second)
                    }

                    if (_enemyPosition.value!!.second > playerGates.second) {
                        _enemyPosition.postValue(_enemyPosition.value!!.first to _enemyPosition.value!!.second - 1)
                    } else {
                        _enemyPosition.postValue(_enemyPosition.value!!.first to _enemyPosition.value!!.second + 1)
                    }
                    delay(6)
                } else {
                    scope.cancel()
                }
            }
        }
    }

    private fun moveEnemy(x: Float, y: Float, callback: () -> Unit) {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            while (true) {
                if (gameScope.isActive) {
                    if (_enemyInventory.value != null) {
                        callback.invoke()
                        scope.cancel()
                    }

                    if (_enemyPosition.value!!.first == x && _enemyPosition.value!!.second == y) {
                        callback.invoke()
                        scope.cancel()
                    }

                    if (_enemyPosition.value!!.first > x) {
                        _enemyPosition.postValue(_enemyPosition.value!!.first - 1 to _enemyPosition.value!!.second)
                    }
                    if (_enemyPosition.value!!.first < x) {
                        _enemyPosition.postValue(_enemyPosition.value!!.first + 1 to _enemyPosition.value!!.second)
                    }

                    if (_enemyPosition.value!!.second > y) {
                        _enemyPosition.postValue(_enemyPosition.value!!.first to _enemyPosition.value!!.second - 1)
                    }
                    if (_enemyPosition.value!!.second < y) {
                        _enemyPosition.postValue(_enemyPosition.value!!.first to _enemyPosition.value!!.second + 1)
                    }
                    delay(6)
                } else {
                    scope.cancel()
                }
            }
        }
    }

    fun endGame() {
        gameScope.cancel()
        _timer.postValue(-1)
    }

    fun pause() {
        gameScope.cancel()
    }

    fun resume() {
        gameScope = CoroutineScope(Dispatchers.Default)
    }


    fun getPlayerPosition(): Pair<Float, Float> = _playerPosition.value!!
    fun setPlayerPosition(x: Float, y: Float) = _playerPosition.postValue(x to y)

    fun getEnemyPosition(): Pair<Float, Float> = _enemyPosition.value!!
    fun setEnemyPosition(x: Float, y: Float) = _enemyPosition.postValue(x to y)

    override fun onCleared() {
        super.onCleared()
        gameScope.cancel()
    }
}