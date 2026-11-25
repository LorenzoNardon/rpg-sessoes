package rpg.infraestrutura.persistencia.jdbc;

import rpg.aplicacao.repositorio.SessaoRepositorio;
import rpg.dominio.modelo.Jogador;
import rpg.dominio.modelo.Sessao;
import rpg.dominio.modelo.SessaoAoVivo;
import rpg.dominio.modelo.SessaoPorTexto;
import rpg.dominio.modelo.StatusPresenca;
import rpg.dominio.modelo.StatusSessao;
import rpg.dominio.regra.EntradaLivre;
import rpg.dominio.regra.EntradaPorConvite;
import rpg.dominio.regra.RegraParticipacao;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class JdbcSessaoRepositorio implements SessaoRepositorio {
    private static final String CAMPOS_SESSAO = """
            id, titulo, data_hora, capacidade_maxima, tipo, plataforma,
            local_acesso, regra_participacao, status
            """;

    private final DataSource dataSource;

    public JdbcSessaoRepositorio(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void salvar(UUID campanhaId, Sessao sessao) {
        String sql = """
                INSERT INTO sessao (
                    id, campanha_id, titulo, data_hora, capacidade_maxima, tipo,
                    plataforma, local_acesso, regra_participacao, status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conexao = dataSource.getConnection()) {
            conexao.setAutoCommit(false);
            try {
                try (var comando = conexao.prepareStatement(sql)) {
                    preencherSessao(comando, campanhaId, sessao);
                    comando.executeUpdate();
                }
                salvarConvidados(conexao, sessao);
                conexao.commit();
            } catch (SQLException e) {
                conexao.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw JdbcErros.traduzir("cadastrar sessão", e);
        }
    }

    @Override
    public void excluir(UUID id) {
        String sql = "DELETE FROM sessao WHERE id = ?";
        try (var conexao = dataSource.getConnection(); var comando = conexao.prepareStatement(sql)) {
            comando.setObject(1, id);
            comando.executeUpdate();
        } catch (SQLException e) {
            throw JdbcErros.traduzir("excluir sessão", e);
        }
    }

    @Override
    public Optional<Sessao> buscarPorId(UUID id) {
        String sql = "SELECT " + CAMPOS_SESSAO + " FROM sessao WHERE id = ?";
        try (var conexao = dataSource.getConnection(); var comando = conexao.prepareStatement(sql)) {
            comando.setObject(1, id);
            try (var resultado = comando.executeQuery()) {
                return resultado.next() ? Optional.of(mapearSessao(conexao, resultado)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw JdbcErros.traduzir("consultar sessão", e);
        }
    }

    @Override
    public List<Sessao> listarPorCampanha(UUID campanhaId) {
        String sql = "SELECT " + CAMPOS_SESSAO + " FROM sessao WHERE campanha_id = ? ORDER BY data_hora";
        List<Sessao> sessoes = new ArrayList<>();
        try (var conexao = dataSource.getConnection(); var comando = conexao.prepareStatement(sql)) {
            comando.setObject(1, campanhaId);
            try (var resultado = comando.executeQuery()) {
                while (resultado.next()) sessoes.add(mapearSessao(conexao, resultado));
            }
            return sessoes;
        } catch (SQLException e) {
            throw JdbcErros.traduzir("listar sessões", e);
        }
    }

    @Override
    public void salvarParticipacao(UUID sessaoId, UUID jogadorId, StatusPresenca presenca) {
        String sql = "INSERT INTO sessao_participante (sessao_id, jogador_id, presenca) VALUES (?, ?, ?)";
        try (var conexao = dataSource.getConnection(); var comando = conexao.prepareStatement(sql)) {
            comando.setObject(1, sessaoId);
            comando.setObject(2, jogadorId);
            comando.setString(3, presenca.name());
            comando.executeUpdate();
        } catch (SQLException e) {
            throw JdbcErros.traduzir("inscrever jogador", e);
        }
    }

    @Override
    public void atualizarPresenca(UUID sessaoId, UUID jogadorId, StatusPresenca presenca) {
        String sql = "UPDATE sessao_participante SET presenca = ? WHERE sessao_id = ? AND jogador_id = ?";
        try (var conexao = dataSource.getConnection(); var comando = conexao.prepareStatement(sql)) {
            comando.setString(1, presenca.name());
            comando.setObject(2, sessaoId);
            comando.setObject(3, jogadorId);
            if (comando.executeUpdate() == 0) throw new SQLException("Participação não encontrada.");
        } catch (SQLException e) {
            throw JdbcErros.traduzir("registrar presença", e);
        }
    }

    @Override
    public void atualizarStatus(UUID sessaoId, StatusSessao status) {
        String sql = "UPDATE sessao SET status = ? WHERE id = ?";
        try (var conexao = dataSource.getConnection(); var comando = conexao.prepareStatement(sql)) {
            comando.setString(1, status.name());
            comando.setObject(2, sessaoId);
            if (comando.executeUpdate() == 0) throw new SQLException("Sessão não encontrada.");
        } catch (SQLException e) {
            throw JdbcErros.traduzir("atualizar sessão", e);
        }
    }

    @Override
    public boolean campanhaPossuiSessoes(UUID campanhaId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM sessao WHERE campanha_id = ?)";
        try (var conexao = dataSource.getConnection(); var comando = conexao.prepareStatement(sql)) {
            comando.setObject(1, campanhaId);
            try (var resultado = comando.executeQuery()) {
                resultado.next();
                return resultado.getBoolean(1);
            }
        } catch (SQLException e) {
            throw JdbcErros.traduzir("verificar sessões da campanha", e);
        }
    }

    private void preencherSessao(java.sql.PreparedStatement comando, UUID campanhaId, Sessao sessao)
            throws SQLException {
        boolean aoVivo = sessao instanceof SessaoAoVivo;
        String localAcesso = aoVivo
                ? ((SessaoAoVivo) sessao).getLinkSala()
                : ((SessaoPorTexto) sessao).getCanal();
        boolean porConvite = sessao.getRegraParticipacao() instanceof EntradaPorConvite;

        comando.setObject(1, sessao.getId());
        comando.setObject(2, campanhaId);
        comando.setString(3, sessao.getTitulo());
        comando.setObject(4, sessao.getDataHora());
        comando.setInt(5, sessao.getCapacidadeMaxima());
        comando.setString(6, aoVivo ? "AO_VIVO" : "POR_TEXTO");
        comando.setString(7, aoVivo
                ? ((SessaoAoVivo) sessao).getPlataforma()
                : ((SessaoPorTexto) sessao).getPlataforma());
        comando.setString(8, localAcesso);
        comando.setString(9, porConvite ? "CONVITE" : "LIVRE");
        comando.setString(10, sessao.getStatus().name());
    }

    private void salvarConvidados(Connection conexao, Sessao sessao) throws SQLException {
        if (!(sessao.getRegraParticipacao() instanceof EntradaPorConvite entrada)) return;

        String sql = "INSERT INTO sessao_convidado (sessao_id, jogador_id) VALUES (?, ?)";
        try (var comando = conexao.prepareStatement(sql)) {
            for (Jogador jogador : entrada.getConvidados()) {
                comando.setObject(1, sessao.getId());
                comando.setObject(2, jogador.getId());
                comando.addBatch();
            }
            comando.executeBatch();
        }
    }

    private Sessao mapearSessao(Connection conexao, ResultSet resultado) throws SQLException {
        UUID id = resultado.getObject("id", UUID.class);
        RegraParticipacao regra = "CONVITE".equals(resultado.getString("regra_participacao"))
                ? new EntradaPorConvite(carregarConvidados(conexao, id))
                : new EntradaLivre();
        Map<Jogador, StatusPresenca> presencas = carregarPresencas(conexao, id);
        StatusSessao status = StatusSessao.valueOf(resultado.getString("status"));

        if ("AO_VIVO".equals(resultado.getString("tipo"))) {
            return SessaoAoVivo.reconstituir(
                    id, resultado.getString("titulo"), resultado.getObject("data_hora", java.time.LocalDateTime.class),
                    resultado.getInt("capacidade_maxima"), resultado.getString("plataforma"),
                    resultado.getString("local_acesso"), regra, status, presencas
            );
        }
        return SessaoPorTexto.reconstituir(
                id, resultado.getString("titulo"), resultado.getObject("data_hora", java.time.LocalDateTime.class),
                resultado.getInt("capacidade_maxima"), resultado.getString("plataforma"),
                resultado.getString("local_acesso"), regra, status, presencas
        );
    }

    private List<Jogador> carregarConvidados(Connection conexao, UUID sessaoId) throws SQLException {
        String sql = """
                SELECT j.id, j.nome, j.nome_personagem
                FROM jogador j
                JOIN sessao_convidado sc ON sc.jogador_id = j.id
                WHERE sc.sessao_id = ?
                ORDER BY LOWER(j.nome)
                """;
        List<Jogador> convidados = new ArrayList<>();
        try (var comando = conexao.prepareStatement(sql)) {
            comando.setObject(1, sessaoId);
            try (var resultado = comando.executeQuery()) {
                while (resultado.next()) convidados.add(mapearJogador(resultado));
            }
        }
        return convidados;
    }

    private Map<Jogador, StatusPresenca> carregarPresencas(Connection conexao, UUID sessaoId)
            throws SQLException {
        String sql = """
                SELECT j.id, j.nome, j.nome_personagem, sp.presenca
                FROM jogador j
                JOIN sessao_participante sp ON sp.jogador_id = j.id
                WHERE sp.sessao_id = ?
                ORDER BY LOWER(j.nome)
                """;
        Map<Jogador, StatusPresenca> presencas = new LinkedHashMap<>();
        try (var comando = conexao.prepareStatement(sql)) {
            comando.setObject(1, sessaoId);
            try (var resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    presencas.put(mapearJogador(resultado), StatusPresenca.valueOf(resultado.getString("presenca")));
                }
            }
        }
        return presencas;
    }

    private Jogador mapearJogador(ResultSet resultado) throws SQLException {
        return Jogador.reconstituir(
                resultado.getObject("id", UUID.class),
                resultado.getString("nome"),
                resultado.getString("nome_personagem")
        );
    }
}
