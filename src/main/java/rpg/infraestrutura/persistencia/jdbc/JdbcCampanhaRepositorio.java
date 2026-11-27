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

/**
 * Implementação JDBC do repositório de campanhas.
 * Responsável por realizar as operações de cadastro,
 * atualização, exclusão e consulta de campanhas no banco de dados.
 */
public final class JdbcCampanhaRepositorio implements CampanhaRepositorio {

    private final DataSource dataSource;

    /**
     * Cria o repositório utilizando a fonte de dados informada.
     *
     * @param dataSource fonte de dados utilizada para acessar o banco
     */
    public JdbcCampanhaRepositorio(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Salva uma nova campanha no banco de dados.
     *
     * @param campanha campanha que será cadastrada
     */
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

    /**
     * Atualiza os dados de uma campanha existente no banco de dados.
     *
     * @param campanha campanha com os dados atualizados
     */
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

    /**
     * Exclui uma campanha pelo seu identificador.
     *
     * @param id identificador da campanha que será excluída
     */
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

    /**
     * Busca uma campanha pelo seu identificador.
     *
     * @param id identificador da campanha
     * @return um Optional contendo a campanha, caso seja encontrada,
     *         ou vazio caso não exista
     */
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

    /**
     * Lista todas as campanhas cadastradas, ordenadas pelo nome.
     *
     * @return lista contendo as campanhas encontradas
     */
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

    /**
     * Converte um registro do banco de dados em um objeto Campanha.
     *
     * @param resultado resultado da consulta SQL
     * @return campanha reconstruída com os dados do registro
     * @throws SQLException caso ocorra um erro ao acessar os dados do registro
     */
    private Campanha mapear(ResultSet resultado) throws SQLException {
        return Campanha.reconstituir(
                resultado.getObject("id", UUID.class),
                resultado.getString("nome"),
                resultado.getString("sistema_rpg")
        );
    }
}