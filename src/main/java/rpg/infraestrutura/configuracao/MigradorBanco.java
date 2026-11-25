package rpg.infraestrutura.configuracao;

import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

public final class MigradorBanco {
    private MigradorBanco() {
    }

    public static void migrar(DataSource dataSource) {
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }
}
