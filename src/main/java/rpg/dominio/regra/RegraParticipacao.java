package rpg.dominio.regra;

import rpg.dominio.modelo.Jogador;

public interface RegraParticipacao {

    boolean permiteEntrada(Jogador jogador);
}