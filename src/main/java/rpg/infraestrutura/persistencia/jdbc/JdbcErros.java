package rpg.infraestrutura.persistencia.jdbc;

import rpg.dominio.excecao.PersistenciaException;

import java.sql.SQLException;

/**
 * Classe responsável por traduzir erros do banco de dados
 * em exceções de persistência da aplicação.
 */
final class JdbcErros {

    /**
     * Construtor privado para impedir a criação de objetos
     * dessa classe.
     */
    private JdbcErros() {
    }

    /**
     * Analisa o código de erro do banco de dados e retorna
     * uma exceção com uma mensagem adequada para a aplicação.
     *
     * @param operacao operação que estava sendo realizada
     * @param excecao exceção original gerada pelo banco de dados
     * @return exceção de persistência com a mensagem correspondente
     */
    static PersistenciaException traduzir(String operacao, SQLException excecao) {
        String estado = excecao.getSQLState();
        String mensagem = switch (estado == null ? "" : estado) {
            case "23505" -> "Já existe um registro com esses dados.";
            case "23503" -> "O registro não pode ser removido porque possui vínculos.";
            default -> estado != null && estado.startsWith("08")
                    ? "Não foi possível conectar ao banco de dados."
                    : "Falha ao " + operacao + ".";
        };
        return new PersistenciaException(mensagem, excecao);
    }
}