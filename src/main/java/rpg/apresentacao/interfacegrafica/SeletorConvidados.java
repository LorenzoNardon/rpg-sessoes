package rpg.apresentacao.interfacegrafica;

import rpg.dominio.modelo.Jogador;

import java.awt.Component;
import java.util.List;
import java.util.Optional;

public interface SeletorConvidados {
    Optional<List<Jogador>> selecionar(Component componentePai, List<Jogador> jogadoresDisponiveis);
}
