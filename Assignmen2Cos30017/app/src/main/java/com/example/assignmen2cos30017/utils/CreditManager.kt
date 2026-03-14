package com.example.assignmen2cos30017.utils

object CreditManager {
    const val INITIAL_BALANCE = 500
    const val MAX_PER_RENTAL = 400
    var balance = INITIAL_BALANCE

    fun canAfford(amount: Int): Boolean = amount <= balance

    fun deduct(amount: Int) {
        balance -= amount
    }

    fun refund(amount: Int) {
        balance += amount
    }

    fun reset() {
        balance = INITIAL_BALANCE
    }
}
