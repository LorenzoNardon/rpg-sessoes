package rpg.aplicacao.repositorio;

import rpg.dominio.modelo.Jogador;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JogadorRepositorio {
    void salvar(Jogador jogador);
    void atualizar(Jogador jogador);
    void excluir(UUID id);
    Optional<Jogador> buscarPorId(UUID id);
    List<Jogador> listarTodos();
    boolean possuiVinculos(UUID id);
}
