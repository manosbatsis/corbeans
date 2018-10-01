package com.github.manosbatsis.corbeans.cordapp.flow.strategy

import net.corda.core.transactions.SignedTransaction

interface FlowCallStrategyDelegate{

    /** Provide a delegate for the givel client */
    fun transact(flow: StrategyBackedFlowLogic): SignedTransaction
}