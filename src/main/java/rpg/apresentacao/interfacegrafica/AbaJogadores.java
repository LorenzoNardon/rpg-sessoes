
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

class AbaJogadores extends AbaBase {

    // Modelo que armazena os jogadores exibidos na lista.
    private final DefaultListModel<Jogador> modelo = new DefaultListModel<>();

    // Lista visual utilizada para exibir e selecionar os jogadores cadastrados.
    private final JList<Jogador> lista = new JList<>(modelo);

    // Botão responsável pelo cadastro de novos jogadores.
    private final JButton botaoCadastrar;

    // Constrói a aba de jogadores, inicializando o formulário,
    // os botões de ação, os atalhos e a lista de jogadores.
    AbaJogadores(GerenciadorRpg gerenciador, ContextoTela contexto) {
        super(gerenciador, contexto);
        estilizarLista(lista, "Jogadores cadastrados");

        // Cria os campos utilizados para informar o nome do jogador e de seu personagem.
        JTextField nome = campo("Ex.: Marina Souza");
        JTextField personagem = campo("Ex.: Thalion, o Bardo");

        // Monta o formulário de cadastro e adiciona os campos com seus respectivos rótulos.
        JPanel form = formulario();
        linha(form, "Nome do jogador", nome);
        linha(form, "Nome do personagem", personagem);

        // Configura o botão de cadastro, validando os campos
        // e executando a operação em segundo plano.
        botaoCadastrar = botao("Cadastrar jogador", Variante.PRIMARIO, KeyEvent.VK_C,
                "Cadastra o jogador", e -> contexto.executar(() -> {
                    exigirTexto(nome, "Nome do jogador");
                    exigirTexto(personagem, "Nome do personagem");

                    String n = nome.getText().trim();
                    String p = personagem.getText().trim();

                    // Cadastra o jogador e limpa os campos após a conclusão.
                    contexto.emSegundoPlano(() -> gerenciador.cadastrarJogador(new Jogador(n, p)), () -> {
                        nome.setText("");
                        personagem.setText("");
                        nome.requestFocusInWindow();
                    }, "Jogador cadastrado com sucesso.");
                }));

        botaoLinha(form, botaoCadastrar);
        fecharFormulario(form);

        // Cria os botões responsáveis por editar e excluir o jogador selecionado.
        JButton editar = botao("Editar selecionado", Variante.SECUNDARIO, KeyEvent.VK_E,
                "Edita o jogador selecionado", e -> editarSelecionado());

        JButton excluir = botao("Excluir selecionado", Variante.PERIGO, KeyEvent.VK_X,
                "Exclui o jogador selecionado", e -> excluirSelecionado());

        // Configura os atalhos de teclado e as ações de duplo clique e Delete na lista.
        atalhosLista(lista, this::editarSelecionado, this::excluirSelecionado);
        lista.setToolTipText("Enter ou duplo clique: editar. Delete: excluir.");

        // Adiciona o formulário à esquerda e a lista de jogadores ao centro da aba.
        add(cartao("Novo jogador", form, null), BorderLayout.WEST);
        add(cartao("Jogadores cadastrados", listaComAcoes(lista, editar, excluir), null),
                BorderLayout.CENTER);
    }

    // Retorna o botão de cadastro para que a tela principal
    // possa utilizá-lo como botão padrão da aba.
    @Override
    JButton getBotaoPadrao() {
        return botaoCadastrar;
    }

    // Atualiza a lista de jogadores com os dados mais recentes
    // obtidos pelo gerenciador, preservando a seleção quando possível.
    @Override
    void atualizar() {
        Jogador selecionado = lista.getSelectedValue();

        modelo.clear();

        for (Jogador jogador : gerenciador.getJogadores())
            modelo.addElement(jogador);

        // Restaura a seleção anterior caso o jogador ainda esteja na lista.
        if (selecionado != null)
            lista.setSelectedValue(selecionado, true);
    }

    // Abre o formulário de edição do jogador selecionado,
    // valida os novos dados e solicita a atualização ao gerenciador.
    private void editarSelecionado() {
        Jogador jogador = lista.getSelectedValue();

        // Impede a edição quando nenhum jogador estiver selecionado.
        if (jogador == null) {
            Dialogos.aviso(contexto.janela(), "Selecione um jogador na lista.");
            lista.requestFocusInWindow();
            return;
        }

        // Preenche os campos de edição com os dados atuais do jogador.
        JTextField nome = campo(jogador.getNome(), null);
        JTextField personagem = campo(jogador.getNomePersonagem(), null);

        JPanel form = formulario();
        linha(form, "Nome do jogador", nome);
        linha(form, "Nome do personagem", personagem);

        // Exibe o diálogo de edição e valida os campos antes de confirmar.
        if (!Dialogos.edicao(contexto.janela(), "Editar jogador", form, nome, () -> {
            exigirTexto(nome, "Nome do jogador");
            exigirTexto(personagem, "Nome do personagem");
        })) return;

        String novoNome = nome.getText().trim();
        String novoPersonagem = personagem.getText().trim();

        // Executa a atualização em segundo plano para não bloquear a interface.
        contexto.emSegundoPlano(() -> gerenciador.atualizarJogador(jogador, novoNome, novoPersonagem),
                null, "Jogador atualizado com sucesso.");
    }

    // Exclui o jogador selecionado após solicitar a confirmação do usuário.
    private void excluirSelecionado() {
        Jogador jogador = lista.getSelectedValue();

        // Impede a exclusão quando nenhum jogador estiver selecionado.
        if (jogador == null) {
            Dialogos.aviso(contexto.janela(), "Selecione um jogador na lista.");
            lista.requestFocusInWindow();
            return;
        }

        // Solicita confirmação antes de executar a exclusão.
        if (!Dialogos.confirmarExclusao(contexto.janela(),
                "Excluir o jogador " + jogador.getNome() + "?")) return;

        // Executa a exclusão em segundo plano e informa o resultado ao usuário.
        contexto.emSegundoPlano(() -> gerenciador.excluirJogador(jogador), null,
                "Jogador excluído com sucesso.");
    }
}
