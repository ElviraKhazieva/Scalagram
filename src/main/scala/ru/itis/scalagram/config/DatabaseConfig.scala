package ru.itis.scalagram.config

case class DatabaseConfig(url: String,
                          user: String,
                          password: String,
                          migrationsPath: String,
                          driverClass: String,
                          poolConnections: Int)
