package rpg.regra;

import rpg.modelo.Jogador;

public class EntradaLivre implements RegraParticipacao {

    @Override
    public boolean permiteEntrada(Jogador jogador) {
        return jogador != null;
    }
}