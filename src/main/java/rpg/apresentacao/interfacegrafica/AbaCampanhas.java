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

    private final DefaultListModel<Campanha> modelo = new DefaultListModel<>();
    private final JList<Campanha> lista = new JList<>(modelo);
    private final JButton botaoCadastrar;

    AbaCampanhas(GerenciadorRpg gerenciador, ContextoTela contexto) {
        super(gerenciador, contexto);
        estilizarLista(lista, "Campanhas cadastradas");

        JTextField nome = campo("Ex.: A Maldição de Strahd");
        JTextField sistema = campo("Ex.: D&D 5e, Tormenta20");
        JPanel form = formulario();
        linha(form, "Nome da campanha", nome);
        linha(form, "Sistema de RPG", sistema);

        botaoCadastrar = botao("Cadastrar campanha", Variante.PRIMARIO, KeyEvent.VK_C,
                "Cadastra a campanha", e -> contexto.executar(() -> {
                    exigirTexto(nome, "Nome da campanha");
                    exigirTexto(sistema, "Sistema de RPG");
                    String n = nome.getText().trim();
                    String s = sistema.getText().trim();
                    contexto.emSegundoPlano(() -> gerenciador.cadastrarCampanha(new Campanha(n, s)), () -> {
                        nome.setText("");
                        sistema.setText("");
                        nome.requestFocusInWindow();
                    }, "Campanha cadastrada com sucesso.");
                }));
        botaoLinha(form, botaoCadastrar);
        fecharFormulario(form);

        JButton editar = botao("Editar selecionada", Variante.SECUNDARIO, KeyEvent.VK_E,
                "Edita a campanha selecionada", e -> editarSelecionada());
        JButton excluir = botao("Excluir selecionada", Variante.PERIGO, KeyEvent.VK_X,
                "Exclui a campanha selecionada", e -> excluirSelecionada());
        atalhosLista(lista, this::editarSelecionada, this::excluirSelecionada);
        lista.setToolTipText("Enter ou duplo clique: editar. Delete: excluir.");

        add(cartao("Nova campanha", form, null), BorderLayout.WEST);
        add(cartao("Campanhas cadastradas", listaComAcoes(lista, editar, excluir), null), BorderLayout.CENTER);
    }

    @Override
    JButton getBotaoPadrao() {
        return botaoCadastrar;
    }

    @Override
    void atualizar() {
        Campanha selecionada = lista.getSelectedValue();
        modelo.clear();
        for (Campanha campanha : gerenciador.getCampanhas()) modelo.addElement(campanha);
        if (selecionada != null) lista.setSelectedValue(selecionada, true);
    }

    private void editarSelecionada() {
        Campanha campanha = lista.getSelectedValue();
        if (campanha == null) {
            Dialogos.aviso(contexto.janela(), "Selecione uma campanha na lista.");
            lista.requestFocusInWindow();
            return;
        }

        JTextField nome = campo(campanha.getNome(), null);
        JTextField sistema = campo(campanha.getSistemaRpg(), null);
        JPanel form = formulario();
        linha(form, "Nome da campanha", nome);
        linha(form, "Sistema de RPG", sistema);
        if (!Dialogos.edicao(contexto.janela(), "Editar campanha", form, nome, () -> {
            exigirTexto(nome, "Nome da campanha");
            exigirTexto(sistema, "Sistema de RPG");
        })) return;

        String novoNome = nome.getText().trim();
        String novoSistema = sistema.getText().trim();
        contexto.emSegundoPlano(() -> gerenciador.atualizarCampanha(campanha, novoNome, novoSistema),
                null, "Campanha atualizada com sucesso.");
    }

    private void excluirSelecionada() {
        Campanha campanha = lista.getSelectedValue();
        if (campanha == null) {
            Dialogos.aviso(contexto.janela(), "Selecione uma campanha na lista.");
            lista.requestFocusInWindow();
            return;
        }
        if (!Dialogos.confirmarExclusao(contexto.janela(), "Excluir a campanha " + campanha.getNome() + "?")) return;

        contexto.emSegundoPlano(() -> gerenciador.excluirCampanha(campanha), null,
                "Campanha excluída com sucesso.");
    }
}
