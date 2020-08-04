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
package com.github.manosbatsis.corda.rpc.poolboy.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(SpringExtension::class)
@SpringBootTest()
class DataJpaIntegrationTest(@Autowired val repo: com.github.manosbatsis.corda.rpc.poolboy.beans.RpcUserRepository) {

    @Test
    fun `basic entity checks`() {
        val p = com.github.manosbatsis.corda.rpc.poolboy.entities.RpcUser("user1", "user1", emptyList())
        val hashCodeBefore = p.hashCode()
        val personSet = hashSetOf(p)
        repo.save(p)
        val hashCodeAfter = p.hashCode()
        assertEquals(1, repo.findAll().size)
        assertTrue(personSet.contains(p))
        assertEquals(hashCodeBefore, hashCodeAfter)
    }
}
