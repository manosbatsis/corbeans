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

import com.github.manosbatsis.vaultaire.dto.attachment.AttachmentFile
import com.github.manosbatsis.vaultaire.dto.attachment.AttachmentReceipt
import com.github.manosbatsis.vaultaire.service.node.NodeService
import com.github.manosbatsis.vaultaire.service.node.NodeServiceRpcPoolBoyDelegate
import net.corda.core.identity.Party

/**
 *  RPC-based,  node-specific service
 */
interface CordaRpcService : NodeService {

    val delegate: NodeServiceRpcPoolBoyDelegate

    /** Get the node identity */
    @Deprecated(
            "Deprecated in favour of [nodeIdentity",
            replaceWith = ReplaceWith("nodeIdentity"))
    val myIdentity: Party

    /** Refreshes the node's NetworkMap cache */
    fun refreshNetworkMapCache(): Unit

    /** Persist the given file as an attachment in the vault  */
    fun saveAttachment(attachmentFile: AttachmentFile): AttachmentReceipt

    /** Persist the given files as a single attachment in the vault  */
    fun saveAttachment(attachmentFiles: List<AttachmentFile>): AttachmentReceipt

    /** Whether this service should be skipped from actuator */
    fun skipInfo(): Boolean
}
