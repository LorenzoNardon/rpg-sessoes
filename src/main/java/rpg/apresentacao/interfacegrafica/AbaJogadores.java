package rpg.apresentacao.interfacegrafica;

import rpg.dominio.modelo.Jogador;
import rpg.servico.GerenciadorRpg;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;

import static rpg.apresentacao.interfacegrafica.Componentes.*;

/**
 * Aba de cadastro, edição e exclusão de jogadores.
 */
class AbaJogadores extends AbaBase {

    /** Modelo que armazena os jogadores exibidos na lista. */
    private final DefaultListModel<Jogador> modelo = new DefaultListModel<>();

    /** Lista visual usada para exibir e selecionar os jogadores cadastrados. */
    private final JList<Jogador> lista = new JList<>(modelo);

    /** Botão responsável pelo cadastro de novos jogadores. */
    private final JButton botaoCadastrar;

    /**
     * Constrói a aba, inicializando o formulário, os botões de ação,
     * os atalhos e a lista de jogadores.
     *
     * @param gerenciador serviço utilizado pela aba
     * @param contexto    contexto da janela principal
     */
    AbaJogadores(GerenciadorRpg gerenciador, ContextoTela contexto) {
        super(gerenciador, contexto);
        estilizarLista(lista, "Jogadores cadastrados");

        JTextField nome = campo("Ex.: Marina Souza");
        JTextField personagem = campo("Ex.: Thalion, o Bardo");

        JPanel form = formulario();
        linha(form, "Nome do jogador", nome);
        linha(form, "Nome do personagem", personagem);

        botaoCadastrar = botao("Cadastrar jogador", Variante.PRIMARIO, KeyEvent.VK_C,
                "Cadastra o jogador", e -> contexto.executar(() -> {
                    exigirTexto(nome, "Nome do jogador");
                    exigirTexto(personagem, "Nome do personagem");

                    String n = nome.getText().trim();
                    String p = personagem.getText().trim();

                    contexto.emSegundoPlano(() -> gerenciador.cadastrarJogador(new Jogador(n, p)), () -> {
                        nome.setText("");
                        personagem.setText("");
                        nome.requestFocusInWindow();
                    }, "Jogador cadastrado com sucesso.");
                }));

        botaoLinha(form, botaoCadastrar);
        fecharFormulario(form);

        JButton editar = botao("Editar selecionado", Variante.SECUNDARIO, KeyEvent.VK_E,
                "Edita o jogador selecionado", e -> editarSelecionado());

        JButton excluir = botao("Excluir selecionado", Variante.PERIGO, KeyEvent.VK_X,
                "Exclui o jogador selecionado", e -> excluirSelecionado());

        atalhosLista(lista, this::editarSelecionado, this::excluirSelecionado);
        lista.setToolTipText("Enter ou duplo clique: editar. Delete: excluir.");

        add(cartao("Novo jogador", form, null), BorderLayout.WEST);
        add(cartao("Jogadores cadastrados", listaComAcoes(lista, editar, excluir), null),
                BorderLayout.CENTER);
    }

    /**
     * Retorna o botão de cadastro, usado pela tela principal como botão padrão da aba.
     *
     * @return botão de cadastro
     */
    @Override
    JButton getBotaoPadrao() {
        return botaoCadastrar;
    }

    /**
     * Atualiza a lista com os jogadores mais recentes do gerenciador,
     * preservando a seleção quando possível.
     */
    @Override
    void atualizar() {
        Jogador selecionado = lista.getSelectedValue();

        modelo.clear();

        for (Jogador jogador : gerenciador.getJogadores())
            modelo.addElement(jogador);

        if (selecionado != null)
            lista.setSelectedValue(selecionado, true);
    }

    /**
     * Abre o diálogo de edição do jogador selecionado, valida os novos dados
     * e solicita a atualização ao gerenciador em segundo plano.
     */
    private void editarSelecionado() {
        Jogador jogador = lista.getSelectedValue();

        if (jogador == null) {
            Dialogos.aviso(contexto.janela(), "Selecione um jogador na lista.");
            lista.requestFocusInWindow();
            return;
        }

        JTextField nome = campo(jogador.getNome(), null);
        JTextField personagem = campo(jogador.getNomePersonagem(), null);

        JPanel form = formulario();
        linha(form, "Nome do jogador", nome);
        linha(form, "Nome do personagem", personagem);

        if (!Dialogos.edicao(contexto.janela(), "Editar jogador", form, nome, () -> {
            exigirTexto(nome, "Nome do jogador");
            exigirTexto(personagem, "Nome do personagem");
        })) return;

        String novoNome = nome.getText().trim();
        String novoPersonagem = personagem.getText().trim();

        contexto.emSegundoPlano(() -> gerenciador.atualizarJogador(jogador, novoNome, novoPersonagem),
                null, "Jogador atualizado com sucesso.");
    }

    /**
     * Exclui o jogador selecionado após a confirmação do usuário.
     */
    private void excluirSelecionado() {
        Jogador jogador = lista.getSelectedValue();

        if (jogador == null) {
            Dialogos.aviso(contexto.janela(), "Selecione um jogador na lista.");
            lista.requestFocusInWindow();
            return;
        }

        if (!Dialogos.confirmarExclusao(contexto.janela(),
                "Excluir o jogador " + jogador.getNome() + "?")) return;

        contexto.emSegundoPlano(() -> gerenciador.excluirJogador(jogador), null,
                "Jogador excluído com sucesso.");
    }
}
