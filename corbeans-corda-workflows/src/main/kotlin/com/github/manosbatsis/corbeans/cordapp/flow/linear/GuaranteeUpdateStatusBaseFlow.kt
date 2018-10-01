package com.github.manosbatsis.corbeans.cordapp.flow.linear

import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.transactions.TransactionBuilder
import java.security.PublicKey

/**
 * Updates the status for the guarantee matching the given ID using the given command,
 * while optionally adding a comment
 */
abstract class GuaranteeUpdateStatusBaseFlow(open val linearId: UniqueIdentifier,
                                             val status: GuaranteeStatus,
                                             open val commentEntry: CommentEntry?,
                                             val advices: List<FileEntry>?,
                                             private val command: TypeOnlyCommandData) : GuaranteeUpdateBaseFlow(linearId) {

    override fun getCommand(): TypeOnlyCommandData = this.command

    /**
     * Override to add advice attachment if one exists
     */
    override fun buildTransactionBuilder(guaranteeToUpdate: StateAndRef<GuaranteeState>,
                                         newGuarantee: GuaranteeState,
                                         signerKeys: List<PublicKey>): TransactionBuilder {
        val transactionBuilder = super.buildTransactionBuilder(guaranteeToUpdate, newGuarantee, signerKeys)
        if(this.advices != null && this.advices.isNotEmpty()){
            for(advice in this.advices){
                val hash = advice.hash
                transactionBuilder.addAttachment(hash)
            }
        }
        return transactionBuilder
    }

    override fun buildNewState(guaranteeToUpdate: StateAndRef<GuaranteeState>): GuaranteeState {
        return guaranteeToUpdate.state.data.copy(
                guarantee = guaranteeToUpdate.state.data.guarantee.copy(
                        status = status
                ),
                comments = GuaranteeUtil.getMergedCommentEntries(guaranteeToUpdate, commentEntry)
        )
    }

}
