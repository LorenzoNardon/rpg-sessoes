package rpg.infraestrutura.persistencia.jdbc;

import rpg.aplicacao.repositorio.JogadorRepositorio;
import rpg.dominio.modelo.Jogador;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementação JDBC do repositório de jogadores.
 * Responsável por realizar as operações de cadastro,
 * atualização, exclusão e consulta de jogadores no banco de dados.
 */
public final class JdbcJogadorRepositorio implements JogadorRepositorio {

    private final DataSource dataSource;

    /**
     * Cria o repositório utilizando a fonte de dados informada.
     *
     * @param dataSource fonte de dados utilizada para acessar o banco
     */
    public JdbcJogadorRepositorio(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Salva um novo jogador no banco de dados.
     *
     * @param jogador jogador que será cadastrado
     */
    @Override
    public void salvar(Jogador jogador) {
        String sql = "INSERT INTO jogador (id, nome, nome_personagem) VALUES (?, ?, ?)";
        try (var conexao = dataSource.getConnection(); var comando = conexao.prepareStatement(sql)) {
            comando.setObject(1, jogador.getId());
            comando.setString(2, jogador.getNome());
            comando.setString(3, jogador.getNomePersonagem());
            comando.executeUpdate();
        } catch (SQLException e) {
            throw JdbcErros.traduzir("cadastrar jogador", e);
        }
    }

    /**
     * Atualiza os dados de um jogador existente no banco de dados.
     *
     * @param jogador jogador com os dados atualizados
     */
    @Override
    public void atualizar(Jogador jogador) {
        String sql = "UPDATE jogador SET nome = ?, nome_personagem = ? WHERE id = ?";
        try (var conexao = dataSource.getConnection(); var comando = conexao.prepareStatement(sql)) {
            comando.setString(1, jogador.getNome());
            comando.setString(2, jogador.getNomePersonagem());
            comando.setObject(3, jogador.getId());
            if (comando.executeUpdate() == 0) throw new SQLException("Jogador não encontrado.");
        } catch (SQLException e) {
            throw JdbcErros.traduzir("atualizar jogador", e);
        }
    }

    /**
     * Exclui um jogador pelo seu identificador.
     *
     * @param id identificador do jogador que será excluído
     */
    @Override
    public void excluir(UUID id) {
        String sql = "DELETE FROM jogador WHERE id = ?";
        try (var conexao = dataSource.getConnection(); var comando = conexao.prepareStatement(sql)) {
            comando.setObject(1, id);
            comando.executeUpdate();
        } catch (SQLException e) {
            throw JdbcErros.traduzir("excluir jogador", e);
        }
    }

    /**
     * Busca um jogador pelo seu identificador.
     *
     * @param id identificador do jogador
     * @return um Optional contendo o jogador, caso seja encontrado,
     *         ou vazio caso não exista
     */
    @Override
    public Optional<Jogador> buscarPorId(UUID id) {
        String sql = "SELECT id, nome, nome_personagem FROM jogador WHERE id = ?";
        try (var conexao = dataSource.getConnection(); var comando = conexao.prepareStatement(sql)) {
            comando.setObject(1, id);
            try (var resultado = comando.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw JdbcErros.traduzir("consultar jogador", e);
        }
    }

    /**
     * Lista todos os jogadores cadastrados, ordenados pelo nome.
     *
     * @return lista contendo os jogadores encontrados
     */
    @Override
    public List<Jogador> listarTodos() {
        String sql = "SELECT id, nome, nome_personagem FROM jogador ORDER BY LOWER(nome)";
        List<Jogador> jogadores = new ArrayList<>();
        try (var conexao = dataSource.getConnection(); var comando = conexao.prepareStatement(sql);
             var resultado = comando.executeQuery()) {
            while (resultado.next()) jogadores.add(mapear(resultado));
            return jogadores;
        } catch (SQLException e) {
            throw JdbcErros.traduzir("listar jogadores", e);
        }
    }

    /**
     * Verifica se um jogador possui vínculos com sessões cadastradas,
     * seja como convidado ou como participante.
     *
     * @param id identificador do jogador
     * @return true se o jogador possuir vínculos, ou false caso contrário
     */
    @Override
    public boolean possuiVinculos(UUID id) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM sessao_convidado WHERE jogador_id = ?
                    UNION ALL
                    SELECT 1 FROM sessao_participante WHERE jogador_id = ?
                )
                """;
        try (var conexao = dataSource.getConnection(); var comando = conexao.prepareStatement(sql)) {
            comando.setObject(1, id);
            comando.setObject(2, id);
            try (var resultado = comando.executeQuery()) {
                resultado.next();
                return resultado.getBoolean(1);
            }
        } catch (SQLException e) {
            throw JdbcErros.traduzir("verificar vínculos do jogador", e);
        }
    }

    /**
     * Converte um registro do banco de dados em um objeto Jogador.
     *
     * @param resultado resultado da consulta SQL
     * @return jogador reconstruído com os dados do registro
     * @throws SQLException caso ocorra um erro ao acessar os dados do registro
     */
    private Jogador mapear(ResultSet resultado) throws SQLException {
        return Jogador.reconstituir(
                resultado.getObject("id", UUID.class),
                resultado.getString("nome"),
                resultado.getString("nome_personagem")
        );
    }
}