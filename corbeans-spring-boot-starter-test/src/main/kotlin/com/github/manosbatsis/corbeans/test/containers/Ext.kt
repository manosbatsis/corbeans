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
package com.github.manosbatsis.corbeans.test.containers

import org.apache.commons.io.FileUtils
import java.io.File

fun File.findNodesDir(): File?{
    if(!isGradleModule()) return null
    if(hasBuildNodes()) return getBuildNodesDir()

    val rootProjectDir = findRootGradleDir() ?: error("Root module could not be found")
    if(rootProjectDir.hasBuildNodes()) return rootProjectDir.getBuildNodesDir()
    println("findNodesDir, root: ${rootProjectDir.absolutePath}")
    val nodesModule = rootProjectDir.listFiles { file ->
        file.hasBuildNodes()
    }.singleOrNull()
    return nodesModule?.getBuildNodesDir()
}

fun File.findRootGradleDir(): File? =
        when{
            !isGradleModule() -> null
            else -> parentFile.findRootGradleDir() ?: this
        }


fun File.hasBuildNodes(): Boolean {

    println("hasBuildNodes, path: ${this.absolutePath} has nodes: ${getBuildNodesDir().exists()}")
    return isGradleModule() && getBuildNodesDir().exists()
}

fun File.getBuildNodesDir() = FileUtils.getFile(this, "build", "nodes")