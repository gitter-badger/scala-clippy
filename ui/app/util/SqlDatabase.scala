package util

import java.net.URI

import com.typesafe.config.ConfigValueFactory._
import com.typesafe.config.{ConfigFactory, Config}
import com.typesafe.scalalogging.StrictLogging
import org.flywaydb.core.Flyway
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend._

case class SqlDatabase(db: slick.jdbc.JdbcBackend#Database, driver: JdbcProfile, connectionString: JdbcConnectionString) {
  def updateSchema() {
    val flyway = new Flyway()
    flyway.setDataSource(connectionString.url, connectionString.username, connectionString.password)
    flyway.migrate()
  }

  def close() {
    db.close()
  }
}

case class JdbcConnectionString(url: String, username: String = "", password: String = "")

object SqlDatabase extends StrictLogging {

  def create(config: DatabaseConfig): SqlDatabase = {
    val envDatabaseUrl = System.getenv("DATABASE_URL")

    if (config.dbPostgresServerName.length > 0)
      createPostgresFromConfig(config)
    else if (envDatabaseUrl != null)
      createPostgresFromEnv(envDatabaseUrl)
    else
      createEmbedded(config)
  }

  def createEmbedded(connectionString: String): SqlDatabase = {
    val db = Database.forURL(connectionString)
    SqlDatabase(db, slick.driver.H2Driver, JdbcConnectionString(connectionString))
  }

  private def createPostgresFromEnv(envDatabaseUrl: String) = {
    import DatabaseConfig._
    /*
      The DATABASE_URL is set by Heroku (if deploying there) and must be converted to a proper object
      of type Config (for Slick). Expected format:
      postgres://<username>:<password>@<host>:<port>/<dbname>
    */
    val dbUri = new URI(envDatabaseUrl)
    val username = dbUri.getUserInfo.split(":")(0)
    val password = dbUri.getUserInfo.split(":")(1)
    val intermediaryConfig = new DatabaseConfig {
      override def rootConfig: Config = ConfigFactory.empty()
        .withValue(PostgresDSClass, fromAnyRef("org.postgresql.ds.PGSimpleDataSource"))
        .withValue(PostgresServerNameKey, fromAnyRef(dbUri.getHost))
        .withValue(PostgresPortKey, fromAnyRef(dbUri.getPort))
        .withValue(PostgresDbNameKey, fromAnyRef(dbUri.getPath.tail))
        .withValue(PostgresUsernameKey, fromAnyRef(username))
        .withValue(PostgresPasswordKey, fromAnyRef(password))
        .withFallback(ConfigFactory.load())
    }
    createPostgresFromConfig(intermediaryConfig)
  }

  private def postgresUrl(host: String, port: String, dbName: String) =
    s"jdbc:postgresql://$host:$port/$dbName"

  private def postgresConnectionString(config: DatabaseConfig) = {
    val host = config.dbPostgresServerName
    val port = config.dbPostgresPort
    val dbName = config.dbPostgresDbName
    val username = config.dbPostgresUsername
    val password = config.dbPostgresPassword
    JdbcConnectionString(postgresUrl(host, port, dbName), username, password)
  }

  private def createPostgresFromConfig(config: DatabaseConfig) = {
    val db = Database.forConfig("db.postgres", config.rootConfig)
    SqlDatabase(db, slick.driver.PostgresDriver, postgresConnectionString(config))
  }

  private def createEmbedded(config: DatabaseConfig): SqlDatabase = {
    val db = Database.forConfig("db.h2")
    SqlDatabase(db, slick.driver.H2Driver, JdbcConnectionString(embeddedConnectionStringFromConfig(config)))
  }

  private def embeddedConnectionStringFromConfig(config: DatabaseConfig): String = {
    val url = config.dbH2Url
    val fullPath = url.split(":")(3)
    logger.info(s"Using an embedded database, with data files located at: $fullPath")
    url
  }
}