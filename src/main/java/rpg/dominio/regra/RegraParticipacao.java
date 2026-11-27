package rpg.dominio.regra;

import rpg.dominio.modelo.Jogador;

/**
 * Define o contrato para as regras de participação em uma sessão.
 *
 * Cada implementação determina se um jogador pode ou não entrar
 * em uma sessão, de acordo com critérios específicos.
 */
public interface RegraParticipacao {

    /**
     * Verifica se um jogador tem permissão para participar da sessão.
     *
     * @param jogador jogador que deseja participar
     * @return true se a entrada for permitida, false caso contrário
     */
    boolean permiteEntrada(Jogador jogador);
}