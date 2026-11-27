
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

// Painel responsável por exibir os jogadores e permitir selecionar quais serão convidados.
final class PainelSelecaoConvidados extends JPanel {

    // Relaciona cada jogador à sua caixa de seleção.
    // LinkedHashMap mantém a ordem em que os jogadores foram adicionados.
    private final Map<Jogador, JCheckBox> opcoesPorJogador = new LinkedHashMap<>();

    // Recebe a lista de jogadores que poderão ser convidados e monta o painel.
    PainelSelecaoConvidados(List<Jogador> jogadores) {
        // Define o layout principal com espaçamento vertical entre os componentes.
        super(new BorderLayout(0, 8));

        // Adiciona uma instrução na parte superior do painel.
        add(new JLabel("Marque os jogadores que serão convidados:"), BorderLayout.NORTH);

        // Cria um painel para organizar as caixas de seleção em uma única coluna.
        JPanel opcoes = new JPanel(new GridLayout(0, 1, 4, 4));

        // Adiciona uma pequena margem interna ao painel das opções.
        opcoes.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // Percorre todos os jogadores recebidos.
        for (Jogador jogador : jogadores) {

            // Cria uma caixa de seleção exibindo o nome do jogador.
            JCheckBox opcao = new JCheckBox(jogador.toString());

            // Guarda a relação entre o jogador e sua caixa de seleção.
            opcoesPorJogador.put(jogador, opcao);

            // Adiciona a caixa de seleção ao painel de opções.
            opcoes.add(opcao);
        }

        // Adiciona o painel de opções ao centro do painel principal.
        add(opcoes, BorderLayout.CENTER);
    }

    // Retorna uma lista contendo somente os jogadores que foram selecionados.
    List<Jogador> getJogadoresSelecionados() {

        return opcoesPorJogador.entrySet().stream()

                // Mantém apenas as entradas cuja caixa de seleção está marcada.
                .filter(entrada -> entrada.getValue().isSelected())

                // Extrai o objeto Jogador de cada entrada selecionada.
                .map(Map.Entry::getKey)

                // Converte o resultado do Stream em uma lista.
                .toList();
    }

    // Marca ou desmarca a caixa de seleção de um jogador específico.
    void definirSelecionado(Jogador jogador, boolean selecionado) {

        // Busca a caixa de seleção correspondente ao jogador informado.
        JCheckBox opcao = opcoesPorJogador.get(jogador);

        // Verifica se o jogador existe no mapa antes de alterar sua seleção.
        if (opcao != null) {

            // Define se a caixa ficará marcada ou desmarcada.
            opcao.setSelected(selecionado);
        }
    }
}
