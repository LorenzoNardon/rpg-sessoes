package rpg.infraestrutura.persistencia.jdbc;

import rpg.aplicacao.excecao.PersistenciaException;

import java.sql.SQLException;

final class JdbcErros {
    private JdbcErros() {
    }

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
