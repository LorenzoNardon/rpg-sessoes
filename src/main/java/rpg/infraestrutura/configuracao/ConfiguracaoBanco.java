package rpg.infraestrutura.configuracao;

/**
 * Record responsável por armazenar as configurações de conexão
 * com o banco de dados.
 *
 * @param url endereço de conexão com o banco de dados
 * @param usuario usuário utilizado na conexão
 * @param senha senha utilizada na conexão
 */
public record ConfiguracaoBanco(String url, String usuario, String senha) {

    /** Usuário padrão utilizado na conexão com o banco de dados. */
    private static final String USUARIO_PADRAO = "rpg_app";

    /** Senha padrão utilizada na conexão com o banco de dados. */
    private static final String SENHA_PADRAO = "rpg_app";

    /**
     * Carrega as configurações de conexão com o banco de dados.
     * Os valores são obtidos das variáveis de ambiente ou das propriedades
     * do sistema. Caso não sejam encontrados, utiliza valores padrão.
     *
     * @return objeto contendo as configurações de conexão
     */
    public static ConfiguracaoBanco carregar() {
        String urlPadrao = "jdbc:postgresql://%s:%s/%s".formatted(
                obter("RPG_DB_HOST", "rpg.db.host", "localhost"),
                obter("RPG_DB_PORT", "rpg.db.port", "5432"),
                obter("RPG_DB_NAME", "rpg.db.name", "rpg_sessoes")
        );

        return new ConfiguracaoBanco(
                obter("RPG_DB_URL", "rpg.db.url", urlPadrao),
                obter("RPG_DB_USER", "rpg.db.user", USUARIO_PADRAO),
                obter("RPG_DB_PASSWORD", "rpg.db.password", SENHA_PADRAO)
        );
    }

    /**
     * Obtém um valor de configuração, verificando primeiro a variável
     * de ambiente e depois a propriedade do sistema. Se nenhum valor
     * válido for encontrado, utiliza o valor padrão.
     *
     * @param variavelAmbiente nome da variável de ambiente
     * @param propriedade nome da propriedade do sistema
     * @param valorPadrao valor utilizado caso nenhum outro seja encontrado
     * @return valor encontrado ou valor padrão
     */
    private static String obter(String variavelAmbiente, String propriedade, String valorPadrao) {
        String valor = System.getenv(variavelAmbiente);

        if (valor == null || valor.isBlank()) {
            valor = System.getProperty(propriedade);
        }

        return valor == null || valor.isBlank() ? valorPadrao : valor;
    }
}