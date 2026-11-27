package rpg.dominio.regra;

import rpg.dominio.modelo.Jogador;

import java.util.List;

/**
 * Implementa uma regra de participação que permite a entrada
 * somente de jogadores previamente convidados.
 *
 * A lista de convidados é imutável após a criação do objeto.
 */
public class EntradaPorConvite implements RegraParticipacao {

    /** Lista de jogadores autorizados a participar da sessão. */
    private final List<Jogador> convidados;

    /**
     * Cria uma regra de participação baseada em convites.
     *
     * @param convidados lista de jogadores convidados
     * @throws IllegalArgumentException se a lista for nula
     *                                  ou contiver jogadores nulos
     */
    public EntradaPorConvite(List<Jogador> convidados) {

        if (convidados == null) {
            throw new IllegalArgumentException(
                    "A lista de convidados não pode ser nula."
            );
        }

        for (Jogador jogador : convidados) {
            if (jogador == null) {
                throw new IllegalArgumentException(
                        "A lista não pode conter jogadores nulos."
                );
            }
        }

        this.convidados = List.copyOf(convidados);
    }

    /**
     * Verifica se o jogador está autorizado a participar da sessão.
     *
     * @param jogador jogador que deseja participar
     * @return true se o jogador estiver na lista de convidados,
     *         false caso contrário
     */
    @Override
    public boolean permiteEntrada(Jogador jogador) {
        return jogador != null && convidados.contains(jogador);
    }

    /**
     * Retorna a lista imutável de jogadores convidados.
     *
     * @return lista de jogadores convidados
     */
    public List<Jogador> getConvidados() {
        return convidados;
    }
}