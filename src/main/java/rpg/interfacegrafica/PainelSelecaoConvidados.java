package rpg.interfacegrafica;

import rpg.modelo.Jogador;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class PainelSelecaoConvidados extends JPanel {
    private final Map<Jogador, JCheckBox> opcoesPorJogador = new LinkedHashMap<>();

    PainelSelecaoConvidados(List<Jogador> jogadores) {
        super(new BorderLayout(0, 8));
        add(new JLabel("Marque os jogadores que serão convidados:"), BorderLayout.NORTH);

        JPanel opcoes = new JPanel(new GridLayout(0, 1, 4, 4));
        opcoes.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        for (Jogador jogador : jogadores) {
            JCheckBox opcao = new JCheckBox(jogador.toString());
            opcoesPorJogador.put(jogador, opcao);
            opcoes.add(opcao);
        }
        add(opcoes, BorderLayout.CENTER);
    }

    List<Jogador> getJogadoresSelecionados() {
        return opcoesPorJogador.entrySet().stream()
                .filter(entrada -> entrada.getValue().isSelected())
                .map(Map.Entry::getKey)
                .toList();
    }

    void definirSelecionado(Jogador jogador, boolean selecionado) {
        JCheckBox opcao = opcoesPorJogador.get(jogador);
        if (opcao != null) {
            opcao.setSelected(selecionado);
        }
    }
}
