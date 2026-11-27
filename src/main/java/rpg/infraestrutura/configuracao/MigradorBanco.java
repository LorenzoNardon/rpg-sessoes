package rpg.infraestrutura.configuracao;

import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

/**
 * Classe responsável por executar as migrações do banco de dados
 * utilizando o Flyway.
 */
public final class MigradorBanco {

    /**
     * Construtor privado para impedir a criação de objetos
     * dessa classe.
     */
    private MigradorBanco() {
    }

    /**
     * Executa as migrações pendentes do banco de dados,
     * utilizando os arquivos localizados em db/migration.
     *
     * @param dataSource fonte de dados utilizada para acessar o banco
     */
    public static void migrar(DataSource dataSource) {
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }
}