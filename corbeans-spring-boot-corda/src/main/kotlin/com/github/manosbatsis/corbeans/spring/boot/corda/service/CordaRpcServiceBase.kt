/*
 *     Corbeans: Corda integration for Spring Boot
 *     Copyright (C) 2018 Manos Batsis
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 3 of the License, or (at your option) any later version.
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
package com.github.manosbatsis.corbeans.spring.boot.corda.service

import com.github.manosbatsis.corda.rpc.poolboy.KeyFreePoolBoy
import com.github.manosbatsis.vaultaire.dto.attachment.AttachmentFile
import com.github.manosbatsis.vaultaire.dto.attachment.AttachmentReceipt
import com.github.manosbatsis.vaultaire.service.SimpleServiceDefaults
import com.github.manosbatsis.vaultaire.service.node.BasicNodeService
import com.github.manosbatsis.vaultaire.service.node.NodeServiceRpcPoolBoyDelegate
import net.corda.core.identity.Party
import net.corda.core.node.services.vault.QueryCriteria
import org.slf4j.LoggerFactory


/**
 *  Abstract base implementation for RPC-based node services
 */
abstract class CordaRpcServiceBase(
        override val delegate: NodeServiceRpcPoolBoyDelegate
) : BasicNodeService(delegate), CordaNodeService {

    companion object {
        private val logger = LoggerFactory.getLogger(CordaRpcServiceBase::class.java)
    }
    /** [KeyFreePoolBoy]-based constructor */
    constructor(
            poolBoy: KeyFreePoolBoy, defaults: SimpleServiceDefaults = SimpleServiceDefaults()
    ) : this(NodeServiceRpcPoolBoyDelegate(poolBoy, defaults))

    override val myIdentity: Party by lazy { nodeIdentity }

    protected val myIdCriteria: QueryCriteria.LinearStateQueryCriteria by lazy {
        QueryCriteria.LinearStateQueryCriteria(participants = listOf(nodeIdentity))

    }

    override fun refreshNetworkMapCache() = delegate.poolBoy.withConnection {
        it.proxy.refreshNetworkMapCache()
    }

    /** Persist the given file(s) as a single attachment in the vault  */
    override fun saveAttachment(attachmentFile: AttachmentFile): AttachmentReceipt {
        return this.saveAttachment(listOf(attachmentFile))
    }

    /** Persist the given file(s) as a single attachment in the vault  */
    override fun saveAttachment(attachmentFiles: List<AttachmentFile>): AttachmentReceipt {
        return this.saveAttachment(toAttachment(attachmentFiles))
    }

    override fun skipInfo(): Boolean = delegate.poolBoy.withConnection { it.skipInfo() }


}
