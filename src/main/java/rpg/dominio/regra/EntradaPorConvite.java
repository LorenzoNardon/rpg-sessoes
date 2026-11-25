package rpg.dominio.regra;

import rpg.dominio.modelo.Jogador;

import java.util.List;

public class EntradaPorConvite implements RegraParticipacao {

    private final List<Jogador> convidados;

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

    @Override
    public boolean permiteEntrada(Jogador jogador) {
        return jogador != null && convidados.contains(jogador);
    }
}