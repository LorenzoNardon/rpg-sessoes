package rpg.interfacegrafica;

import rpg.modelo.Jogador;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class DialogoSelecaoConvidados implements SeletorConvidados {
    private final InteracaoDialogo interacao;

    public DialogoSelecaoConvidados() {
        this(new InteracaoSwing());
    }

    DialogoSelecaoConvidados(InteracaoDialogo interacao) {
        this.interacao = Objects.requireNonNull(interacao);
    }

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

    interface InteracaoDialogo {
        int confirmar(Component componentePai, PainelSelecaoConvidados painel);

        void avisarSelecaoObrigatoria(Component componentePai);
    }

    private static final class InteracaoSwing implements InteracaoDialogo {
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
