package com.github.manosbatsis.corbeans.spring.boot.corda.model.info

import net.corda.core.identity.Party
import net.corda.core.utilities.NetworkHostAndPort

data class NodeInfo(
        val platformVersion: Int,
        val peerNames: List<String>,
        val peers: List<String>,

        val identity: Party,
        val identities: List<Party>,
        val notaries: List<Party>,

        val flows: List<String>,
        val addresses: List<NetworkHostAndPort>
)

data class NetworkInfo(
        val nodes: Map<String, NodeInfo>
)