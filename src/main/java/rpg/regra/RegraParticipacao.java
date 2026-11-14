package rpg.regra;

import rpg.modelo.Jogador;

public interface RegraParticipacao {

    boolean permiteEntrada(Jogador jogador);
}