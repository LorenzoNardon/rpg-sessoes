package rpg.apresentacao.interfacegrafica;

import rpg.dominio.modelo.Jogador;

import java.awt.Component;
import java.util.List;
import java.util.Optional;

/**
 * Define como o usuário escolhe os jogadores convidados de uma sessão.
 */
public interface SeletorConvidados {

    /**
     * Apresenta os jogadores disponíveis e retorna os escolhidos.
     *
     * @param componentePai        componente usado como referência para exibir a seleção
     * @param jogadoresDisponiveis jogadores que podem ser convidados
     * @return os jogadores selecionados, ou {@link Optional#empty()} se o usuário cancelar
     */
    Optional<List<Jogador>> selecionar(Component componentePai, List<Jogador> jogadoresDisponiveis);
}
