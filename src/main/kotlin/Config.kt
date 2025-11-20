package com.arekalov

import java.io.File
import java.util.Properties

object Config {
    private val properties = Properties()
    
    init {
        val localPropertiesFile = File("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { properties.load(it) }
        } else {
            throw IllegalStateException("local.properties file not found in project root")
        }
    }
    
    val todoistToken: String
        get() = properties.getProperty("todoist.token")
            ?: throw IllegalStateException("todoist.token not found in local.properties")
}

