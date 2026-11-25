package rpg.dominio.regra;

import rpg.dominio.modelo.Jogador;

public class EntradaLivre implements RegraParticipacao {

    @Override
    public boolean permiteEntrada(Jogador jogador) {
        return jogador != null;
    }
}