
package rpg.apresentacao.interfacegrafica;

import rpg.dominio.modelo.Jogador;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

// Implementa a seleção de jogadores convidados por meio de uma janela de diálogo.
public final class DialogoSelecaoConvidados implements SeletorConvidados {

    // Responsável por controlar a interação com os diálogos.
    // Permite separar a lógica da seleção da implementação visual do Swing.
    private final InteracaoDialogo interacao;

    // Construtor padrão que utiliza a implementação de diálogos do Swing.
    public DialogoSelecaoConvidados() {
        this(new InteracaoSwing());
    }

    // Construtor que recebe uma implementação de interação.
    // Permite substituir a interação visual por outra implementação,
    // facilitando testes e reduzindo o acoplamento com o Swing.
    DialogoSelecaoConvidados(InteracaoDialogo interacao) {
        // Garante que uma implementação válida seja fornecida.
        this.interacao = Objects.requireNonNull(interacao);
    }

    // Implementa o método definido pela interface SeletorConvidados.
    // Exibe os jogadores disponíveis e permite selecionar os convidados.
    @Override
    public Optional<List<Jogador>> selecionar(Component componentePai, List<Jogador> jogadoresDisponiveis) {

        // Garante que a lista de jogadores recebida não seja nula.
        Objects.requireNonNull(jogadoresDisponiveis, "A lista de jogadores disponíveis é obrigatória.");

        // Cria o painel de seleção utilizando uma cópia da lista recebida.
        // Isso evita que alterações na lista original afetem diretamente o painel.
        PainelSelecaoConvidados painel = new PainelSelecaoConvidados(List.copyOf(jogadoresDisponiveis));

        // Mantém o diálogo aberto enquanto não houver uma seleção válida
        // ou até que o usuário cancele a operação.
        while (true) {

            // Exibe o diálogo e obtém a resposta do usuário.
            int resultado = interacao.confirmar(componentePai, painel);

            // Se o usuário cancelar ou fechar o diálogo,
            // retorna um Optional vazio para indicar que não houve seleção.
            if (resultado != JOptionPane.OK_OPTION) {
                return Optional.empty();
            }

            // Recupera a lista de jogadores selecionados no painel.
            List<Jogador> selecionados = painel.getJogadoresSelecionados();

            // Verifica se pelo menos um jogador foi selecionado.
            if (!selecionados.isEmpty()) {

                // Retorna os jogadores selecionados dentro de um Optional.
                // Uma cópia é utilizada para evitar alterações externas na lista.
                return Optional.of(List.copyOf(selecionados));
            }

            // Se nenhum jogador foi selecionado, exibe um aviso
            // e permite que o usuário tente novamente.
            interacao.avisarSelecaoObrigatoria(componentePai);
        }
    }

    // Interface que define as operações necessárias para controlar
    // os diálogos de confirmação e aviso da seleção de convidados.
    // Permite separar a lógica da seleção da forma como os diálogos são exibidos.
    interface InteracaoDialogo {

        // Exibe o painel de seleção e retorna a resposta do usuário.
        int confirmar(Component componentePai, PainelSelecaoConvidados painel);

        // Exibe um aviso informando que a seleção de convidados é obrigatória.
        void avisarSelecaoObrigatoria(Component componentePai);
    }

    // Implementação concreta da interface InteracaoDialogo utilizando Swing.
    // Centraliza a criação e exibição das janelas utilizadas na seleção.
    private static final class InteracaoSwing implements InteracaoDialogo {

        // Exibe o painel de seleção dentro de uma área com rolagem.
        @Override
        public int confirmar(Component componentePai, PainelSelecaoConvidados painel) {

            // Cria uma área de rolagem para permitir visualizar
            // todos os jogadores disponíveis.
            JScrollPane rolagem = new JScrollPane(painel);

            // Define o tamanho preferencial da janela de seleção.
            rolagem.setPreferredSize(new Dimension(400, 260));

            // Exibe o diálogo com as opções de confirmar ou cancelar.
            // Retorna o código correspondente à escolha do usuário.
            return JOptionPane.showConfirmDialog(
                    componentePai,
                    rolagem,
                    "Selecione os jogadores convidados",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
        }

        // Exibe uma mensagem de aviso quando nenhum jogador foi selecionado.
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
