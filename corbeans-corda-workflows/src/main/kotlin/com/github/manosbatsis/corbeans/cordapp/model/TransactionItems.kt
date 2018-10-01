package com.github.manosbatsis.corbeans.cordapp.model

import net.corda.core.contracts.*
import net.corda.core.crypto.SecureHash

/**
 * Contains items for a single transaction. Typically used for providing
 * input to a [net.corda.core.transactions.TransactionBuilder]
 */
data class TransactionItems(
        /** [StateAndRef] objects used as the transaction input states */
        val inputStates: List<StateAndRef<*>> = emptyList(),
        /** [ContractState] objects used as the transaction output states */
        val outputStates: Iterable<ContractState> = emptyList(),
        /** [TransactionState] objects used as the transaction output states */
        val outputTransactionStates: Iterable<TransactionState<*>> = emptyList(),
        /** [StateAndContract] objects used as the transaction output states */
        val outputStateAndContracts: Iterable<StateAndContract> = emptyList(),
        /** [CommandData] objects used to build the transaction commands */
        val commandTypes: Iterable<CommandData> = emptyList(),
        /** [SecureHash] objects corresponding to transaction attachments that must have already been uploaded */
        val attachments: Iterable<SecureHash> = emptyList()
)