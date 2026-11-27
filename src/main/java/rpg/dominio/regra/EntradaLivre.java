package rpg.dominio.regra;

import rpg.dominio.modelo.Jogador;

/**
 * Implementa uma regra de participação que permite a entrada livre
 * de qualquer jogador válido na sessão.
 *
 * Não exige convite ou autorização prévia.
 */
public class EntradaLivre implements RegraParticipacao {

    /**
     * Verifica se o jogador pode participar da sessão.
     *
     * @param jogador jogador que deseja participar
     * @return true se o jogador não for nulo, false caso contrário
     */
    @Override
    public boolean permiteEntrada(Jogador jogador) {
        return jogador != null;
    }
}