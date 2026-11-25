package rpg.infraestrutura.configuracao;

public record ConfiguracaoBanco(String url, String usuario, String senha) {
    private static final String USUARIO_PADRAO = "rpg_app";
    private static final String SENHA_PADRAO = "rpg_app";

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

    private static String obter(String variavelAmbiente, String propriedade, String valorPadrao) {
        String valor = System.getenv(variavelAmbiente);
        if (valor == null || valor.isBlank()) {
            valor = System.getProperty(propriedade);
        }
        return valor == null || valor.isBlank() ? valorPadrao : valor;
    }
}
