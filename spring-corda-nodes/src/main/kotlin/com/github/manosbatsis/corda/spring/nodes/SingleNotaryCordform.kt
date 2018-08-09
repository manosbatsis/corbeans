package com.github.manosbatsis.corda.webserver.spring.cordform

//import net.corda.core.node.services.ServiceInfo
//import net.corda.demorun.runNodes
//import net.corda.nodeapi.User
//import net.corda.demorun.util.*

//fun main(args: Array<String>) = SingleNotaryCordform.runNodes()
object SingleNotaryCordform
//val notaryDemoUser = User("demou", "demop", setOf(all()))
/*
object SingleNotaryCordform : CordformDefinition("build" / "notary-demo-nodes", DUMMY_NOTARY.name) {
    init {
        node {
            name(ALICE.name)
            p2pPort(10002)
            rpcPort(10003)
            rpcUsers(notaryDemoUser)
        }
        node {
            name(BOB.name)
            p2pPort(10005)
            rpcPort(10006)
        }
        node {
            name(DUMMY_NOTARY.name)
            p2pPort(10009)
            rpcPort(10010)
            advertisedServices(ServiceInfo(ValidatingNotaryService.type))
        }
    }

    override fun setup(context: CordformContext) {}
}*/