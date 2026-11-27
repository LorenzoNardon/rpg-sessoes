package rpg.aplicacao.repositorio;

import rpg.dominio.modelo.Jogador;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface responsável por definir as operações de persistência
 * e consulta dos jogadores.
 */
public interface JogadorRepositorio {

    /**
     * Salva um novo jogador.
     *
     * @param jogador jogador que será salvo
     */
    void salvar(Jogador jogador);

    /**
     * Atualiza os dados de um jogador existente.
     *
     * @param jogador jogador com os dados atualizados
     */
    void atualizar(Jogador jogador);

    /**
     * Exclui um jogador pelo seu identificador.
     *
     * @param id identificador do jogador que será excluído
     */
    void excluir(UUID id);

    /**
     * Busca um jogador pelo seu identificador.
     *
     * @param id identificador do jogador
     * @return um Optional contendo o jogador, caso seja encontrado
     */
    Optional<Jogador> buscarPorId(UUID id);

    /**
     * Lista todos os jogadores cadastrados.
     *
     * @return lista com todos os jogadores
     */
    List<Jogador> listarTodos();

    /**
     * Verifica se um jogador possui vínculos com outros registros.
     *
     * @param id identificador do jogador
     * @return true se o jogador possuir vínculos, ou false caso contrário
     */
    boolean possuiVinculos(UUID id);
}