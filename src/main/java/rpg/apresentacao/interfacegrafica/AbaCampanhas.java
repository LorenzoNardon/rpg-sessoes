
package rpg.apresentacao.interfacegrafica;

import rpg.dominio.modelo.Campanha;
import rpg.servico.GerenciadorRpg;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;

import static rpg.apresentacao.interfacegrafica.Componentes.*;

class AbaCampanhas extends AbaBase {

    // Modelo que armazena as campanhas exibidas na lista.
    private final DefaultListModel<Campanha> modelo = new DefaultListModel<>();

    // Lista visual utilizada para exibir e selecionar as campanhas cadastradas.
    private final JList<Campanha> lista = new JList<>(modelo);

    // Botão responsável pelo cadastro de novas campanhas.
    private final JButton botaoCadastrar;

    // Constrói a aba de campanhas, inicializando o formulário,
    // os botões de ação, os atalhos e a lista de campanhas.
    AbaCampanhas(GerenciadorRpg gerenciador, ContextoTela contexto) {
        super(gerenciador, contexto);
        estilizarLista(lista, "Campanhas cadastradas");

        // Cria os campos utilizados para informar o nome e o sistema da campanha.
        JTextField nome = campo("Ex.: A Maldição de Strahd");
        JTextField sistema = campo("Ex.: D&D 5e, Tormenta20");

        // Monta o formulário de cadastro e adiciona os campos com seus respectivos rótulos.
        JPanel form = formulario();
        linha(form, "Nome da campanha", nome);
        linha(form, "Sistema de RPG", sistema);

        // Configura o botão de cadastro, validando os campos
        // e executando a operação em segundo plano.
        botaoCadastrar = botao("Cadastrar campanha", Variante.PRIMARIO, KeyEvent.VK_C,
                "Cadastra a campanha", e -> contexto.executar(() -> {
                    exigirTexto(nome, "Nome da campanha");
                    exigirTexto(sistema, "Sistema de RPG");

                    String n = nome.getText().trim();
                    String s = sistema.getText().trim();

                    // Cadastra a campanha e limpa os campos após a conclusão.
                    contexto.emSegundoPlano(() -> gerenciador.cadastrarCampanha(new Campanha(n, s)), () -> {
                        nome.setText("");
                        sistema.setText("");
                        nome.requestFocusInWindow();
                    }, "Campanha cadastrada com sucesso.");
                }));

        botaoLinha(form, botaoCadastrar);
        fecharFormulario(form);

        // Cria os botões responsáveis por editar e excluir a campanha selecionada.
        JButton editar = botao("Editar selecionada", Variante.SECUNDARIO, KeyEvent.VK_E,
                "Edita a campanha selecionada", e -> editarSelecionada());

        JButton excluir = botao("Excluir selecionada", Variante.PERIGO, KeyEvent.VK_X,
                "Exclui a campanha selecionada", e -> excluirSelecionada());

        // Configura os atalhos de teclado e as ações de duplo clique e Delete na lista.
        atalhosLista(lista, this::editarSelecionada, this::excluirSelecionada);
        lista.setToolTipText("Enter ou duplo clique: editar. Delete: excluir.");

        // Adiciona o formulário à esquerda e a lista de campanhas ao centro da aba.
        add(cartao("Nova campanha", form, null), BorderLayout.WEST);
        add(cartao("Campanhas cadastradas", listaComAcoes(lista, editar, excluir), null),
                BorderLayout.CENTER);
    }

    // Retorna o botão de cadastro para que a tela principal
    // possa utilizá-lo como botão padrão da aba.
    @Override
    JButton getBotaoPadrao() {
        return botaoCadastrar;
    }

    // Atualiza a lista de campanhas com os dados mais recentes
    // obtidos pelo gerenciador, preservando a seleção quando possível.
    @Override
    void atualizar() {
        Campanha selecionada = lista.getSelectedValue();

        modelo.clear();

        for (Campanha campanha : gerenciador.getCampanhas())
            modelo.addElement(campanha);

        // Restaura a seleção anterior caso a campanha ainda esteja na lista.
        if (selecionada != null)
            lista.setSelectedValue(selecionada, true);
    }

    // Abre o formulário de edição da campanha selecionada,
    // valida os novos dados e solicita a atualização ao gerenciador.
    private void editarSelecionada() {
        Campanha campanha = lista.getSelectedValue();

        // Impede a edição quando nenhuma campanha estiver selecionada.
        if (campanha == null) {
            Dialogos.aviso(contexto.janela(), "Selecione uma campanha na lista.");
            lista.requestFocusInWindow();
            return;
        }

        // Preenche os campos de edição com os dados atuais da campanha.
        JTextField nome = campo(campanha.getNome(), null);
        JTextField sistema = campo(campanha.getSistemaRpg(), null);

        JPanel form = formulario();
        linha(form, "Nome da campanha", nome);
        linha(form, "Sistema de RPG", sistema);

        // Exibe o diálogo de edição e valida os campos antes de confirmar.
        if (!Dialogos.edicao(contexto.janela(), "Editar campanha", form, nome, () -> {
            exigirTexto(nome, "Nome da campanha");
            exigirTexto(sistema, "Sistema de RPG");
        })) return;

        String novoNome = nome.getText().trim();
        String novoSistema = sistema.getText().trim();

        // Executa a atualização em segundo plano para não bloquear a interface.
        contexto.emSegundoPlano(() -> gerenciador.atualizarCampanha(campanha, novoNome, novoSistema),
                null, "Campanha atualizada com sucesso.");
    }

    // Exclui a campanha selecionada após solicitar a confirmação do usuário.
    private void excluirSelecionada() {
        Campanha campanha = lista.getSelectedValue();

        // Impede a exclusão quando nenhuma campanha estiver selecionada.
        if (campanha == null) {
            Dialogos.aviso(contexto.janela(), "Selecione uma campanha na lista.");
            lista.requestFocusInWindow();
            return;
        }

        // Solicita confirmação antes de executar a exclusão.
        if (!Dialogos.confirmarExclusao(contexto.janela(),
                "Excluir a campanha " + campanha.getNome() + "?")) return;

        // Executa a exclusão em segundo plano e informa o resultado ao usuário.
        contexto.emSegundoPlano(() -> gerenciador.excluirCampanha(campanha), null,
                "Campanha excluída com sucesso.");
    }
}
