package rpg.apresentacao.interfacegrafica;

import rpg.dominio.modelo.Jogador;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Painel que exibe os jogadores e permite selecionar quais serão convidados.
 */
final class PainelSelecaoConvidados extends JPanel {

    /**
     * Relaciona cada jogador à sua caixa de seleção.
     * {@link LinkedHashMap} mantém a ordem em que os jogadores foram adicionados.
     */
    private final Map<Jogador, JCheckBox> opcoesPorJogador = new LinkedHashMap<>();

    /**
     * Monta o painel com uma caixa de seleção para cada jogador.
     *
     * @param jogadores jogadores que poderão ser convidados
     */
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

    /**
     * Retorna somente os jogadores cuja caixa de seleção está marcada.
     *
     * @return lista de jogadores selecionados
     */
    List<Jogador> getJogadoresSelecionados() {
        return opcoesPorJogador.entrySet().stream()
                .filter(entrada -> entrada.getValue().isSelected())
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Marca ou desmarca a caixa de seleção de um jogador específico.
     * Jogadores que não estão no painel são ignorados.
     *
     * @param jogador     jogador cuja seleção será alterada
     * @param selecionado {@code true} para marcar, {@code false} para desmarcar
     */
    void definirSelecionado(Jogador jogador, boolean selecionado) {
        JCheckBox opcao = opcoesPorJogador.get(jogador);

        if (opcao != null) {
            opcao.setSelected(selecionado);
        }
    }
}
