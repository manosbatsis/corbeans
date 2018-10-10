/**
 *     Corda-Spring: integration and other utilities for developers working with Spring-Boot and Corda.
 *     Copyright (C) 2018 Manos Batsis
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 */
package com.github.manosbatsis.corbeans.cordapp.flow.delegating

import co.paralleluniverse.fibers.Suspendable
import com.github.manosbatsis.corbeans.cordapp.extention.getFirstNotary
import com.github.manosbatsis.corbeans.cordapp.flow.base.BaseFlowLogic
import com.github.manosbatsis.corbeans.cordapp.flow.delegating.tx.TxStrategyDelegateConfig
import com.github.manosbatsis.corbeans.cordapp.flow.delegating.tx.TxStrategyDelegateFlow
import com.github.manosbatsis.corbeans.cordapp.model.TransactionItems
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.ContractClassName
import net.corda.core.contracts.TimeWindow
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.seconds

/**
 * A flow that delegates transaction execution to a delegate strategy implementation
 */
// todo: abstract class TxDelegatingFlow<out T> : BaseFlowLogic<TX>() {
abstract class TxDelegatingFlow<TX : TxStrategyDelegateFlow> : BaseFlowLogic<SignedTransaction>() {

    companion object {
        object INITIALISING : ProgressTracker.Step("Initializing.")
        object INPUT_VALIDATE : ProgressTracker.Step("Validating flow input.")
        object INPUT_CONVERT : ProgressTracker.Step("Converting flow input.")
        object TRANSACTION_CONFIG : ProgressTracker.Step("Creating transaction configuration.")
        object TRANSACTION_CREATE_DELEGATE : ProgressTracker.Step("Creating transaction delegate.")
        object TRANSACTION_EXEC : ProgressTracker.Step("Executing transaction.")

        fun tracker() = ProgressTracker(INITIALISING, INPUT_VALIDATE, INPUT_CONVERT, TRANSACTION_CONFIG, TRANSACTION_CREATE_DELEGATE, TRANSACTION_EXEC)

    }

    override val progressTracker: ProgressTracker = tracker()
    init{
        progressTracker.currentStep = INITIALISING
    }

    abstract val txDelegateType: Class<TX>
    /** The [CommandData] to be used in the transaction */
    abstract val commandData: CommandData
    /** The [ContractClassName] to be used in the transaction */
    abstract val contractClassName: ContractClassName

    /** The transaction items to be used by the [TxStrategyDelegateFlow] */
    abstract fun getTransactionItems(): TransactionItems

    /** Validate input data */
    open fun validateInput() {/* default is NO_OP */}

    /** The notary to be used in the transaction */
    open fun getNotary(): Party = serviceHub.getFirstNotary()

    /** The period during which the transaction must be notarised. May have a start and/or an end time */
    open fun getTimeWindow(): TimeWindow = TimeWindow.fromStartAndDuration(serviceHub.clock.instant(), 60.seconds)


    open fun createTxDelegate(config: TxStrategyDelegateConfig): TX {
        return txDelegateType.getDeclaredConstructor(TxStrategyDelegateConfig::class.java).newInstance(config)
    }

    @Suspendable
    override fun call(): SignedTransaction {

        progressTracker.currentStep = INPUT_VALIDATE
        validateInput()

        progressTracker.currentStep = INPUT_CONVERT
        val transactionItems = this.getTransactionItems()

        progressTracker.currentStep = TRANSACTION_CONFIG
        val config = TxStrategyDelegateConfig(
                transactionItems = transactionItems,
                commandData = commandData,
                contractClassName = contractClassName,
                notary = getNotary(),
                timeWindow = getTimeWindow()
        )

        progressTracker.currentStep = TRANSACTION_CREATE_DELEGATE
        val txDelegateFlow: TX = createTxDelegate(config)

        progressTracker.currentStep = TRANSACTION_EXEC
        return subFlow(txDelegateFlow)
    }

}
