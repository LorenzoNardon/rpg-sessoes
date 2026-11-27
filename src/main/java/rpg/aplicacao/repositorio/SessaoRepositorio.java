package rpg.aplicacao.repositorio;

import rpg.dominio.modelo.Sessao;
import rpg.dominio.modelo.StatusPresenca;
import rpg.dominio.modelo.StatusSessao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface responsável por definir as operações de persistência
 * e consulta das sessões de uma campanha.
 */
public interface SessaoRepositorio {

    /**
     * Salva uma nova sessão vinculada a uma campanha.
     *
     * @param campanhaId identificador da campanha
     * @param sessao sessão que será salva
     */
    void salvar(UUID campanhaId, Sessao sessao);

    /**
     * Exclui uma sessão pelo seu identificador.
     *
     * @param id identificador da sessão que será excluída
     */
    void excluir(UUID id);

    /**
     * Busca uma sessão pelo seu identificador.
     *
     * @param id identificador da sessão
     * @return um Optional contendo a sessão, caso seja encontrada
     */
    Optional<Sessao> buscarPorId(UUID id);

    /**
     * Lista todas as sessões vinculadas a uma campanha.
     *
     * @param campanhaId identificador da campanha
     * @return lista de sessões da campanha
     */
    List<Sessao> listarPorCampanha(UUID campanhaId);

    /**
     * Salva a participação de um jogador em uma sessão.
     *
     * @param sessaoId identificador da sessão
     * @param jogadorId identificador do jogador
     * @param presenca status de presença do jogador
     */
    void salvarParticipacao(UUID sessaoId, UUID jogadorId, StatusPresenca presenca);

    /**
     * Atualiza o status de presença de um jogador em uma sessão.
     *
     * @param sessaoId identificador da sessão
     * @param jogadorId identificador do jogador
     * @param presenca novo status de presença
     */
    void atualizarPresenca(UUID sessaoId, UUID jogadorId, StatusPresenca presenca);

    /**
     * Atualiza o status de uma sessão.
     *
     * @param sessaoId identificador da sessão
     * @param status novo status da sessão
     */
    void atualizarStatus(UUID sessaoId, StatusSessao status);

    /**
     * Verifica se uma campanha possui sessões cadastradas.
     *
     * @param campanhaId identificador da campanha
     * @return true se a campanha possuir sessões, ou false caso contrário
     */
    boolean campanhaPossuiSessoes(UUID campanhaId);
}