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

/**
 * Implementação JDBC do repositório de sessões.
 * Responsável por realizar as operações de cadastro, exclusão,
 * consulta e atualização das sessões no banco de dados,
 * incluindo o gerenciamento de convidados e participantes.
 */
public final class JdbcSessaoRepositorio implements SessaoRepositorio {

    /** Campos da tabela sessão utilizados nas consultas SQL. */
    private static final String CAMPOS_SESSAO = """
            id, titulo, data_hora, capacidade_maxima, tipo, plataforma,
            local_acesso, regra_participacao, status
            """;

    private final DataSource dataSource;

    /**
     * Cria o repositório utilizando a fonte de dados informada.
     *
     * @param dataSource fonte de dados utilizada para acessar o banco
     */
    public JdbcSessaoRepositorio(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Salva uma nova sessão vinculada a uma campanha.
     * Também registra os convidados, caso a participação seja por convite.
     *
     * @param campanhaId identificador da campanha
     * @param sessao sessão que será cadastrada
     */
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

    /**
     * Exclui uma sessão pelo seu identificador.
     *
     * @param id identificador da sessão que será excluída
     */
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

    /**
     * Busca uma sessão pelo seu identificador.
     *
     * @param id identificador da sessão
     * @return um Optional contendo a sessão, caso seja encontrada,
     *         ou vazio caso não exista
     */
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

    /**
     * Lista todas as sessões vinculadas a uma campanha,
     * ordenadas pela data e hora.
     *
     * @param campanhaId identificador da campanha
     * @return lista de sessões encontradas
     */
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

    /**
     * Registra a participação de um jogador em uma sessão.
     *
     * @param sessaoId identificador da sessão
     * @param jogadorId identificador do jogador
     * @param presenca status de presença do jogador
     */
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

    /**
     * Atualiza o status de presença de um jogador em uma sessão.
     *
     * @param sessaoId identificador da sessão
     * @param jogadorId identificador do jogador
     * @param presenca novo status de presença
     */
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

    /**
     * Atualiza o status de uma sessão.
     *
     * @param sessaoId identificador da sessão
     * @param status novo status da sessão
     */
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

    /**
     * Verifica se uma campanha possui sessões cadastradas.
     *
     * @param campanhaId identificador da campanha
     * @return true se a campanha possuir sessões, ou false caso contrário
     */
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

    /**
     * Preenche os parâmetros do comando SQL com os dados da sessão.
     * Identifica o tipo de sessão e a regra de participação para
     * armazenar as informações correspondentes.
     *
     * @param comando comando SQL que receberá os valores
     * @param campanhaId identificador da campanha vinculada
     * @param sessao sessão cujos dados serão utilizados
     * @throws SQLException caso ocorra um erro ao preencher os parâmetros
     */
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

    /**
     * Salva os jogadores convidados para uma sessão.
     * Caso a regra de participação não seja por convite,
     * nenhuma operação é realizada.
     *
     * @param conexao conexão utilizada com o banco de dados
     * @param sessao sessão que contém a lista de convidados
     * @throws SQLException caso ocorra um erro ao salvar os convidados
     */
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

    /**
     * Converte um registro do banco de dados em um objeto Sessao.
     * Também carrega a regra de participação e os dados de presença
     * dos jogadores associados à sessão.
     *
     * @param conexao conexão utilizada para consultar os dados relacionados
     * @param resultado resultado da consulta SQL
     * @return sessão reconstruída com os dados encontrados
     * @throws SQLException caso ocorra um erro ao acessar os dados
     */
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

    /**
     * Carrega a lista de jogadores convidados para uma sessão.
     *
     * @param conexao conexão utilizada com o banco de dados
     * @param sessaoId identificador da sessão
     * @return lista de jogadores convidados, ordenada pelo nome
     * @throws SQLException caso ocorra um erro ao consultar os convidados
     */
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

    /**
     * Carrega os jogadores participantes de uma sessão e seus
     * respectivos status de presença.
     *
     * @param conexao conexão utilizada com o banco de dados
     * @param sessaoId identificador da sessão
     * @return mapa contendo os jogadores e seus status de presença,
     *         mantendo a ordem de consulta
     * @throws SQLException caso ocorra um erro ao consultar as presenças
     */
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

    /**
     * Converte um registro do banco de dados em um objeto Jogador.
     *
     * @param resultado resultado da consulta SQL
     * @return jogador reconstruído com os dados do registro
     * @throws SQLException caso ocorra um erro ao acessar os dados do registro
     */
    private Jogador mapearJogador(ResultSet resultado) throws SQLException {
        return Jogador.reconstituir(
                resultado.getObject("id", UUID.class),
                resultado.getString("nome"),
                resultado.getString("nome_personagem")
        );
    }
}