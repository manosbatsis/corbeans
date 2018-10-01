package com.github.manosbatsis.corbeans.cordapp.model

import net.corda.core.contracts.UniqueIdentifier

interface FlowInput {
}

interface LinearIdFlowInput: FlowInput {
    val linearId: UniqueIdentifier
}

interface ExternalIdFlowInput: FlowInput {
    val externalId: String
}