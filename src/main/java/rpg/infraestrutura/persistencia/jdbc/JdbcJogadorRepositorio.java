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

public final class JdbcJogadorRepositorio implements JogadorRepositorio {
    private final DataSource dataSource;

    public JdbcJogadorRepositorio(DataSource dataSource) {
        this.dataSource = dataSource;
    }

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

    private Jogador mapear(ResultSet resultado) throws SQLException {
        return Jogador.reconstituir(
                resultado.getObject("id", UUID.class),
                resultado.getString("nome"),
                resultado.getString("nome_personagem")
        );
    }
}
