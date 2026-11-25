package rpg.aplicacao.repositorio;

import rpg.dominio.modelo.Campanha;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampanhaRepositorio {
    void salvar(Campanha campanha);
    void atualizar(Campanha campanha);
    void excluir(UUID id);
    Optional<Campanha> buscarPorId(UUID id);
    List<Campanha> listarTodas();
}
