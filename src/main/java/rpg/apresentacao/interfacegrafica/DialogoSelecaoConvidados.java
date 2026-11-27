package rpg.apresentacao.interfacegrafica;

import rpg.dominio.modelo.Jogador;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementa a seleção de jogadores convidados por meio de uma janela de diálogo.
 */
public final class DialogoSelecaoConvidados implements SeletorConvidados {

    /**
     * Controla a interação com os diálogos, separando a lógica da seleção
     * da implementação visual do Swing.
     */
    private final InteracaoDialogo interacao;

    /**
     * Cria o seletor utilizando a implementação de diálogos do Swing.
     */
    public DialogoSelecaoConvidados() {
        this(new InteracaoSwing());
    }

    /**
     * Cria o seletor com uma implementação de interação personalizada,
     * facilitando testes e reduzindo o acoplamento com o Swing.
     *
     * @param interacao implementação responsável por exibir os diálogos
     */
    DialogoSelecaoConvidados(InteracaoDialogo interacao) {
        this.interacao = Objects.requireNonNull(interacao);
    }

    /**
     * Exibe os jogadores disponíveis e permite selecionar os convidados.
     * O diálogo é reaberto enquanto nenhum jogador for marcado.
     *
     * @param componentePai        componente pai usado para posicionar o diálogo
     * @param jogadoresDisponiveis jogadores que podem ser convidados
     * @return os jogadores selecionados, ou {@link Optional#empty()} se o usuário cancelar
     */
    @Override
    public Optional<List<Jogador>> selecionar(Component componentePai, List<Jogador> jogadoresDisponiveis) {

        Objects.requireNonNull(jogadoresDisponiveis, "A lista de jogadores disponíveis é obrigatória.");

        PainelSelecaoConvidados painel = new PainelSelecaoConvidados(List.copyOf(jogadoresDisponiveis));

        while (true) {

            int resultado = interacao.confirmar(componentePai, painel);

            if (resultado != JOptionPane.OK_OPTION) {
                return Optional.empty();
            }

            List<Jogador> selecionados = painel.getJogadoresSelecionados();

            if (!selecionados.isEmpty()) {
                return Optional.of(List.copyOf(selecionados));
            }

            interacao.avisarSelecaoObrigatoria(componentePai);
        }
    }

    /**
     * Define as operações necessárias para controlar os diálogos de confirmação
     * e aviso da seleção de convidados.
     */
    interface InteracaoDialogo {

        /**
         * Exibe o painel de seleção e retorna a resposta do usuário.
         *
         * @param componentePai componente pai usado para posicionar o diálogo
         * @param painel        painel com as opções de convidados
         * @return código da opção escolhida, conforme {@link JOptionPane}
         */
        int confirmar(Component componentePai, PainelSelecaoConvidados painel);

        /**
         * Exibe um aviso informando que a seleção de convidados é obrigatória.
         *
         * @param componentePai componente pai usado para posicionar o diálogo
         */
        void avisarSelecaoObrigatoria(Component componentePai);
    }

    /**
     * Implementação de {@link InteracaoDialogo} baseada em Swing.
     */
    private static final class InteracaoSwing implements InteracaoDialogo {

        /**
         * Exibe o painel de seleção dentro de uma área com rolagem.
         *
         * @param componentePai componente pai usado para posicionar o diálogo
         * @param painel        painel com as opções de convidados
         * @return código da opção escolhida, conforme {@link JOptionPane}
         */
        @Override
        public int confirmar(Component componentePai, PainelSelecaoConvidados painel) {

            JScrollPane rolagem = new JScrollPane(painel);
            rolagem.setPreferredSize(new Dimension(400, 260));

            return JOptionPane.showConfirmDialog(
                    componentePai,
                    rolagem,
                    "Selecione os jogadores convidados",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
        }

        /**
         * Exibe um aviso quando nenhum jogador foi selecionado.
         *
         * @param componentePai componente pai usado para posicionar o diálogo
         */
        @Override
        public void avisarSelecaoObrigatoria(Component componentePai) {
            JOptionPane.showMessageDialog(
                    componentePai,
                    "Selecione pelo menos um jogador convidado.",
                    "Atenção",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }
}
