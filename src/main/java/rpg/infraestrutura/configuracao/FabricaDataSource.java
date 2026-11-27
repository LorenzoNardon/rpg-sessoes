package rpg.infraestrutura.configuracao;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

/**
 * Classe responsável por criar e configurar a fonte de dados
 * utilizada para estabelecer conexões com o banco de dados.
 */
public final class FabricaDataSource {

    /**
     * Construtor privado para impedir a criação de objetos
     * dessa classe.
     */
    private FabricaDataSource() {
    }

    /**
     * Cria uma fonte de dados utilizando as configurações
     * de conexão informadas.
     *
     * @param configuracao configurações de conexão com o banco de dados
     * @return fonte de dados configurada para acessar o banco
     */
    public static DataSource criar(ConfiguracaoBanco configuracao) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL(configuracao.url());
        dataSource.setUser(configuracao.usuario());
        dataSource.setPassword(configuracao.senha());
        return dataSource;
    }
}