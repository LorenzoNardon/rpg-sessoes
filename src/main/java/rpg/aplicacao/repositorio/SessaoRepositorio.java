package rpg.aplicacao.repositorio;

import rpg.dominio.modelo.Sessao;
import rpg.dominio.modelo.StatusPresenca;
import rpg.dominio.modelo.StatusSessao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessaoRepositorio {
    void salvar(UUID campanhaId, Sessao sessao);
    void excluir(UUID id);
    Optional<Sessao> buscarPorId(UUID id);
    List<Sessao> listarPorCampanha(UUID campanhaId);
    void salvarParticipacao(UUID sessaoId, UUID jogadorId, StatusPresenca presenca);
    void atualizarPresenca(UUID sessaoId, UUID jogadorId, StatusPresenca presenca);
    void atualizarStatus(UUID sessaoId, StatusSessao status);
    boolean campanhaPossuiSessoes(UUID campanhaId);
}
