package rpg.infraestrutura.persistencia.jdbc;

import rpg.aplicacao.repositorio.CampanhaRepositorio;
import rpg.dominio.modelo.Campanha;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class JdbcCampanhaRepositorio implements CampanhaRepositorio {
    private final DataSource dataSource;

    public JdbcCampanhaRepositorio(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void salvar(Campanha campanha) {
        String sql = "INSERT INTO campanha (id, nome, sistema_rpg) VALUES (?, ?, ?)";
        try (var conexao = dataSource.getConnection(); var comando = conexao.prepareStatement(sql)) {
            comando.setObject(1, campanha.getId());
            comando.setString(2, campanha.getNome());
            comando.setString(3, campanha.getSistemaRpg());
            comando.executeUpdate();
        } catch (SQLException e) {
            throw JdbcErros.traduzir("cadastrar campanha", e);
        }
    }

    @Override
    public void atualizar(Campanha campanha) {
        String sql = "UPDATE campanha SET nome = ?, sistema_rpg = ? WHERE id = ?";
        try (var conexao = dataSource.getConnection(); var comando = conexao.prepareStatement(sql)) {
            comando.setString(1, campanha.getNome());
            comando.setString(2, campanha.getSistemaRpg());
            comando.setObject(3, campanha.getId());
            if (comando.executeUpdate() == 0) throw new SQLException("Campanha não encontrada.");
        } catch (SQLException e) {
            throw JdbcErros.traduzir("atualizar campanha", e);
        }
    }

    @Override
    public void excluir(UUID id) {
        String sql = "DELETE FROM campanha WHERE id = ?";
        try (var conexao = dataSource.getConnection(); var comando = conexao.prepareStatement(sql)) {
            comando.setObject(1, id);
            comando.executeUpdate();
        } catch (SQLException e) {
            throw JdbcErros.traduzir("excluir campanha", e);
        }
    }

    @Override
    public Optional<Campanha> buscarPorId(UUID id) {
        String sql = "SELECT id, nome, sistema_rpg FROM campanha WHERE id = ?";
        try (var conexao = dataSource.getConnection(); var comando = conexao.prepareStatement(sql)) {
            comando.setObject(1, id);
            try (var resultado = comando.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw JdbcErros.traduzir("consultar campanha", e);
        }
    }

    @Override
    public List<Campanha> listarTodas() {
        String sql = "SELECT id, nome, sistema_rpg FROM campanha ORDER BY LOWER(nome)";
        List<Campanha> campanhas = new ArrayList<>();
        try (var conexao = dataSource.getConnection(); var comando = conexao.prepareStatement(sql);
             var resultado = comando.executeQuery()) {
            while (resultado.next()) campanhas.add(mapear(resultado));
            return campanhas;
        } catch (SQLException e) {
            throw JdbcErros.traduzir("listar campanhas", e);
        }
    }

    private Campanha mapear(ResultSet resultado) throws SQLException {
        return Campanha.reconstituir(
                resultado.getObject("id", UUID.class),
                resultado.getString("nome"),
                resultado.getString("sistema_rpg")
        );
    }
}
