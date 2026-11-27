package rpg.aplicacao.repositorio;

import rpg.dominio.modelo.Campanha;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface responsável por definir as operações de persistência
 * e consulta das campanhas.
 */
public interface CampanhaRepositorio {

    /**
     * Salva uma nova campanha.
     *
     * @param campanha campanha que será salva
     */
    void salvar(Campanha campanha);

    /**
     * Atualiza os dados de uma campanha existente.
     *
     * @param campanha campanha com os dados atualizados
     */
    void atualizar(Campanha campanha);

    /**
     * Exclui uma campanha pelo seu identificador.
     *
     * @param id identificador da campanha que será excluída
     */
    void excluir(UUID id);

    /**
     * Busca uma campanha pelo seu identificador.
     *
     * @param id identificador da campanha
     * @return um Optional contendo a campanha, caso seja encontrada
     */
    Optional<Campanha> buscarPorId(UUID id);

    /**
     * Lista todas as campanhas cadastradas.
     *
     * @return lista com todas as campanhas
     */
    List<Campanha> listarTodas();
}