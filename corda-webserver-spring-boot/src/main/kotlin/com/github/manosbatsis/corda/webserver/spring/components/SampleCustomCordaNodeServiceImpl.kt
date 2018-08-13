package com.github.manosbatsis.corda.webserver.spring.components

import com.github.manosbatsis.corda.spring.beans.CordaNodeService
import com.github.manosbatsis.corda.spring.beans.CordaNodeServiceImpl
import com.github.manosbatsis.corda.spring.beans.util.NodeRpcConnection
import org.springframework.beans.factory.InitializingBean


class SampleCustomCordaNodeServiceImpl(override val nodeRpcConnection: NodeRpcConnection) : CordaNodeServiceImpl(nodeRpcConnection) {

    /** dummy method */
    fun dummy(): Boolean = true

}
