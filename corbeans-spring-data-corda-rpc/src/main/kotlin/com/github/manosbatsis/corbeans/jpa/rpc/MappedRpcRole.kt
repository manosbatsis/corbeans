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
package com.github.manosbatsis.corbeans.jpa.rpc

import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.MappedSuperclass

@MappedSuperclass
open class MappedRpcRole<T : MappedRpcPermission>(
        @Id
        val id: String,

        @ManyToMany
        @JoinTable(
                name = "roles_permissions",
                joinColumns = arrayOf(JoinColumn(name = "role_name", referencedColumnName = "id")),
                inverseJoinColumns = arrayOf(JoinColumn(name = "permission", referencedColumnName = "id"))
        )
        val permissions: List<T> = emptyList()

)