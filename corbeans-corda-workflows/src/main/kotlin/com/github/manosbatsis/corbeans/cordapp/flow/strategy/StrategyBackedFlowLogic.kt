package com.github.manosbatsis.corbeans.cordapp.flow.strategy

import com.github.manosbatsis.corbeans.cordapp.cordaservice.DecoratingIdentityService
import com.github.manosbatsis.corbeans.cordapp.helpers.ProgressTrackerUtil
import com.github.manosbatsis.corbeans.cordapp.model.TransactionItems
import net.corda.core.contracts.*
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker

abstract class StrategyBackedFlowLogic(): FlowLogic<SignedTransaction>() {

    override val progressTracker: ProgressTracker = ProgressTrackerUtil.createBasicTracker()

    /** The default transaction notary */
    abstract fun getNotary(): Party

    /** The notary appropriate for the given state and contract */
    abstract fun getNotary(state: ContractState, contract: ContractClassName): Party

    /** The contract identifier appropriate for the given contract state */
    abstract fun getContractClassName(state: ContractState): ContractClassName

    /** The [CommandData] appropriate for the given contract and state */
    abstract fun getCommandData(transactionState: TransactionState<*>): CommandData

    /** The notary appropriate for the given state and contract */
    fun getNotary(stateAndContract: StateAndContract): Party =
            getNotary(stateAndContract.state, stateAndContract.contract)

    /** The [CommandData] appropriate for the given contract and state */
    fun getCommandData(state: ContractState, contract: ContractClassName): CommandData =
            this.getCommandData(TransactionState(state, contract, this.getNotary()))

    /** The period during which the transaction must be notarised. May have a start and/or an end time */
    fun getTimeWindow(): TimeWindow? = null

    /**Get the items for the main transaction */
    abstract fun getTransactionItems(): TransactionItems

    /** Lazy access to the first notary found in the [NetworkMapCache] */
    val firstNotary by lazy {
        serviceHub.networkMapCache.notaryIdentities.firstOrNull()
                ?: throw FlowException("No available notary.")
    }

    /** Lazily-initialized type-aware vault service wrapper
    val typedLinearStateVaultService by lazy {
        if(LinearState::class.javaObjectType.isAssignableFrom(  stateType )) TypedLinearStateVaultHelper(serviceHub.vaultService, stateType as Class<LinearState>)
        else throw FlowException("Not a LinearState")
    }*/

    /** Lazily-initialized type-aware vault service wrapper */
    val identityService by lazy {
        serviceHub.cordaService(DecoratingIdentityService::class.java)
    }

    /** Get the sessions of counter-parties that need to sign a transaction  */
    fun toFlowSessions(vararg states: TransactionState<*>): Set<FlowSession> =
            toFlowSessions(*states.asIterable().mapNotNull { it.data }.toTypedArray())


    /** Get the sessions of counter-parties that need to sign a transaction  */
    fun toFlowSessions(vararg states: ContractState): Set<FlowSession> =
        states.toList().mapNotNull { identityService.resolveParticipants(it) }
                 .flatten().filter { it.name != this.ourIdentity.name }
                 .map { party: Party -> initiateFlow(party) }
                 .toSet()


}
