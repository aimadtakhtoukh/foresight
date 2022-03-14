package implementation.database.utils

import slick.jdbc.JdbcBackend.Database

object ConnectionConfig {
  def database: Database = Database.forConfig("db")
}
