package rpg.infraestrutura.configuracao;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public final class FabricaDataSource {
    private FabricaDataSource() {
    }

    public static DataSource criar(ConfiguracaoBanco configuracao) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL(configuracao.url());
        dataSource.setUser(configuracao.usuario());
        dataSource.setPassword(configuracao.senha());
        return dataSource;
    }
}
