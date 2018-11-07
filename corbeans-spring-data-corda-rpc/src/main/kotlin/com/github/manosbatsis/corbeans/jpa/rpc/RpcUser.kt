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
package com.github.manosbatsis.corbeans.jpa.rpc

import javax.persistence.*

@Entity
@Table(name = "users")
class RpcUser(
    @Id
    val username: String,
    @Column(nullable = false)
    val password: String,
    @JoinTable(
            name = "user_roles",
            joinColumns = arrayOf(JoinColumn(name = "username", referencedColumnName = "username")),
            inverseJoinColumns = arrayOf(JoinColumn(name = "role_name", referencedColumnName = "role_name"))
    )
    val role: List<RpcRole>
)