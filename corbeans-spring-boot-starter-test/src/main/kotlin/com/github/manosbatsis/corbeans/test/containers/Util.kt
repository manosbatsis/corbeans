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

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.WaitStrategy
import org.testcontainers.containers.wait.strategy.WaitStrategyTarget
import org.testcontainers.images.builder.ImageFromDockerfile
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.time.Duration

class KImageNameContainer(
        dockerImageName: DockerImageName
) : GenericContainer<KImageNameContainer>(dockerImageName)

class KGenericContainer(
        dockerImage: ImageFromDockerfile
) : GenericContainer<KGenericContainer>(dockerImage)

fun File.isGradleModule() = exists() && isDirectory
        &&  (File(this, "build.gradle").exists()
        || File(this, "build.gradle.kts").exists())

class EmptyWaitStrategy: WaitStrategy {
    override fun waitUntilReady(waitStrategyTarget: WaitStrategyTarget?) {}
    override fun withStartupTimeout(startupTimeout: Duration?): WaitStrategy? {
        return null
    }
}