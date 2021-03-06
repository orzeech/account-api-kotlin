package pl.orzechsoft.accountapi.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.r2dbc.core.DatabaseClient

@Configuration
@Profile("h2")
class H2Configuration(db: DatabaseClient) {
    init {
        val initDb = db.sql {
            """ CREATE TABLE IF NOT EXISTS account (
                    ACCOUNT_ID VARCHAR(60),
                    BALANCE_PLN DECIMAL
                );
            """
        }
        initDb.then().subscribe()
    }
}