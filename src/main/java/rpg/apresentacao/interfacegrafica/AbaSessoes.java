package rpg.apresentacao.interfacegrafica;

import rpg.dominio.modelo.Campanha;
import rpg.dominio.modelo.Jogador;
import rpg.dominio.modelo.Sessao;
import rpg.dominio.modelo.SessaoAoVivo;
import rpg.dominio.modelo.SessaoPorTexto;
import rpg.dominio.modelo.StatusPresenca;
import rpg.dominio.regra.EntradaLivre;
import rpg.dominio.regra.EntradaPorConvite;
import rpg.dominio.regra.RegraParticipacao;
import rpg.servico.GerenciadorRpg;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static rpg.apresentacao.interfacegrafica.Componentes.*;

class AbaSessoes extends AbaBase {

    private final SeletorConvidados seletorConvidados;
    private final DefaultListModel<Sessao> modeloSessoes = new DefaultListModel<>();
    private final DefaultListModel<String> modeloParticipantes = new DefaultListModel<>();
    private final JList<Sessao> listaSessoes = new JList<>(modeloSessoes);
    private final JList<String> listaParticipantes = new JList<>(modeloParticipantes);
    private final JComboBox<Campanha> comboCampanhas = new JComboBox<>();
    private final JComboBox<Jogador> comboJogadores = new JComboBox<>();
    private final JTextArea resumoSessao = new JTextArea(3, 20);
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final JButton botaoCriar;
    private boolean atualizando;

    AbaSessoes(GerenciadorRpg gerenciador, ContextoTela contexto, SeletorConvidados seletorConvidados) {
        super(gerenciador, contexto);
        this.seletorConvidados = Objects.requireNonNull(seletorConvidados);

        estilizarLista(listaSessoes, "Sessões");
        estilizarLista(listaParticipantes, "Participantes da sessão");
        estilizarCombo(comboCampanhas);
        estilizarCombo(comboJogadores);
        configurarResumo();

        // ----- Formulário de nova sessão (coluna esquerda)
        JTextField titulo = campo("Ex.: A Cripta Esquecida - Parte 1");
        JTextField data = campo("dd/MM/aaaa HH:mm");
        JTextField capacidade = campo("5", "Número de vagas");
        JComboBox<String> tipo = new JComboBox<>(new String[]{"Ao vivo", "Por texto"});
        JTextField plataforma = campo("Ex.: Discord, Roll20");
        JTextField detalhe = campo("Link ou nome do canal");
        JComboBox<String> regra = new JComboBox<>(new String[]{"Entrada livre", "Por convite"});
        estilizarCombo(tipo);
        estilizarCombo(regra);

        JPanel form = formulario();
        linha(form, "Campanha", comboCampanhas);
        linha(form, "Título", titulo);
        linha(form, "Data e hora", data);
        linha(form, "Capacidade máxima", capacidade);
        linha(form, "Tipo", tipo);
        linha(form, "Plataforma", plataforma);
        linha(form, "Link da sala / canal", detalhe);
        linha(form, "Participação", regra);

        botaoCriar = botao("Criar sessão", Variante.PRIMARIO, KeyEvent.VK_C,
                "Cria a sessão com os dados informados", e -> contexto.executar(() -> {
                    Campanha campanha = (Campanha) comboCampanhas.getSelectedItem();
                    if (campanha == null)
                        throw new ErroValidacao("Cadastre e selecione uma campanha primeiro.", comboCampanhas);
                    exigirTexto(titulo, "Título");
                    LocalDateTime dataHora = lerDataHora(data);
                    int limite = lerCapacidade(capacidade);

                    Optional<RegraParticipacao> regraEscolhida = criarRegraParticipacao(regra.getSelectedIndex());
                    if (regraEscolhida.isEmpty()) return;

                    String tituloSessao = titulo.getText().trim();
                    String plataformaSessao = plataforma.getText().trim();
                    String detalheSessao = detalhe.getText().trim();
                    RegraParticipacao regraSessao = regraEscolhida.get();
                    Sessao sessao = tipo.getSelectedIndex() == 0
                            ? new SessaoAoVivo(tituloSessao, dataHora, limite, plataformaSessao,
                                    detalheSessao, regraSessao)
                            : new SessaoPorTexto(tituloSessao, dataHora, limite, plataformaSessao,
                                    detalheSessao, regraSessao);
                    contexto.emSegundoPlano(() -> gerenciador.adicionarSessao(campanha, sessao), () -> {
                        titulo.setText("");
                        data.setText("");
                        plataforma.setText("");
                        detalhe.setText("");
                        titulo.requestFocusInWindow();
                    }, "Sessão criada com sucesso.");
                }));
        botaoLinha(form, botaoCriar);
        fecharFormulario(form);
        JPanel cartaoForm = cartao("Nova sessão", form, null);
        cartaoForm.setMinimumSize(new Dimension(390, 300));

        // ----- Lista de sessões
        JButton excluirSessao = botao("Excluir sessão", Variante.PERIGO, KeyEvent.VK_X,
                "Exclui a sessão selecionada", e -> excluirSessaoSelecionada());
        JScrollPane rolagemSessoes = rolagem(listaSessoes);
        rolagemSessoes.setPreferredSize(new Dimension(300, 105));
        JPanel cartaoSessoes = cartao("Sessões", rolagemSessoes, excluirSessao);
        atalhosLista(listaSessoes, null, this::excluirSessaoSelecionada);
        listaSessoes.setToolTipText("Selecione uma sessão. Delete: excluir.");
        listaSessoes.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !atualizando) atualizarDetalhes();
        });

        // ----- Detalhes da sessão selecionada
        JPanel conteudoDetalhes = new JPanel(new BorderLayout(0, 8));
        conteudoDetalhes.setOpaque(false);
        conteudoDetalhes.add(resumoSessao, BorderLayout.NORTH);
        conteudoDetalhes.add(rolagem(listaParticipantes), BorderLayout.CENTER);
        JPanel cartaoDetalhes = cartao("Detalhes e participantes", conteudoDetalhes, null);

        // ----- Ações
        JPanel selecao = formulario();
        linha(selecao, "Jogador", comboJogadores);

        JButton inscrever = botao("Inscrever jogador", Variante.PRIMARIO, KeyEvent.VK_J,
                "Inscreve o jogador escolhido na sessão selecionada", e -> contexto.executar(() -> {
                    Sessao s = sessaoSelecionada();
                    Jogador j = jogadorSelecionado();
                    contexto.emSegundoPlano(() -> gerenciador.inscreverJogador(s, j), null, "Jogador inscrito.");
                }));
        JButton presenca = botao("Registrar presença", Variante.SECUNDARIO, KeyEvent.VK_P,
                "Registra presença ou ausência do jogador escolhido", e -> contexto.executar(() -> {
                    Sessao s = sessaoSelecionada();
                    Jogador j = jogadorSelecionado();
                    if (!s.getJogadores().contains(j))
                        throw new ErroValidacao("O jogador selecionado não está inscrito nesta sessão.",
                                comboJogadores);
                    Object[] opcoes = {"Presente", "Ausente"};
                    int escolha = JOptionPane.showOptionDialog(contexto.janela(), "Selecione a presença:",
                            "Presença", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                            opcoes, opcoes[0]);
                    if (escolha < 0) return;
                    StatusPresenca status = escolha == 0 ? StatusPresenca.PRESENTE : StatusPresenca.AUSENTE;
                    contexto.emSegundoPlano(() -> gerenciador.registrarPresenca(s, j, status), null,
                            "Presença registrada.");
                }));
        JButton iniciar = botao("Iniciar", Variante.SECUNDARIO, KeyEvent.VK_I,
                "Inicia a sessão selecionada",
                e -> alterarSessao(gerenciador::iniciarSessao, "Sessão iniciada.", null));
        JButton finalizar = botao("Finalizar", Variante.SECUNDARIO, KeyEvent.VK_F,
                "Finaliza a sessão selecionada",
                e -> alterarSessao(gerenciador::finalizarSessao, "Sessão finalizada.", null));
        JButton cancelar = botao("Cancelar", Variante.PERIGO, KeyEvent.VK_A,
                "Cancela a sessão selecionada",
                e -> alterarSessao(gerenciador::cancelarSessao, "Sessão cancelada.", "Cancelar a sessão"));

        JPanel linhaCima = new JPanel(new GridLayout(1, 2, 8, 0));
        linhaCima.setOpaque(false);
        linhaCima.add(inscrever);
        linhaCima.add(presenca);
        JPanel linhaBaixo = new JPanel(new GridLayout(1, 3, 8, 0));
        linhaBaixo.setOpaque(false);
        linhaBaixo.add(iniciar);
        linhaBaixo.add(finalizar);
        linhaBaixo.add(cancelar);
        JPanel botoes = new JPanel(new GridLayout(2, 1, 0, 8));
        botoes.setOpaque(false);
        botoes.add(linhaCima);
        botoes.add(linhaBaixo);
        JPanel conteudoAcoes = new JPanel(new BorderLayout(0, 6));
        conteudoAcoes.setOpaque(false);
        conteudoAcoes.add(selecao, BorderLayout.NORTH);
        conteudoAcoes.add(botoes, BorderLayout.CENTER);
        JPanel cartaoAcoes = cartao("Ações da sessão selecionada", conteudoAcoes, null);

        JPanel lado = new JPanel(new BorderLayout(0, 12));
        lado.setOpaque(false);
        lado.add(cartaoSessoes, BorderLayout.NORTH);
        lado.add(cartaoDetalhes, BorderLayout.CENTER);
        lado.add(cartaoAcoes, BorderLayout.SOUTH);
        lado.setMinimumSize(new Dimension(500, 300));

        JSplitPane divisao = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cartaoForm, lado);
        divisao.setResizeWeight(0.42);
        divisao.setContinuousLayout(true);
        divisao.setDividerSize(12);
        divisao.setBorder(null);
        add(divisao, BorderLayout.CENTER);
    }

    @Override
    JButton getBotaoPadrao() {
        return botaoCriar;
    }

    @Override
    void atualizar() {
        atualizando = true;
        try {
            Jogador jogadorNoCombo = (Jogador) comboJogadores.getSelectedItem();
            Campanha campanhaNoCombo = (Campanha) comboCampanhas.getSelectedItem();
            Sessao sessaoNaLista = listaSessoes.getSelectedValue();

            comboJogadores.removeAllItems();
            for (Jogador jogador : gerenciador.getJogadores()) comboJogadores.addItem(jogador);
            comboCampanhas.removeAllItems();
            modeloSessoes.clear();
            for (Campanha campanha : gerenciador.getCampanhas()) {
                comboCampanhas.addItem(campanha);
                for (Sessao sessao : campanha.getSessoes()) modeloSessoes.addElement(sessao);
            }

            // Mantém a seleção do usuário quando o item ainda existe
            if (jogadorNoCombo != null) comboJogadores.setSelectedItem(jogadorNoCombo);
            if (campanhaNoCombo != null) comboCampanhas.setSelectedItem(campanhaNoCombo);
            if (sessaoNaLista != null) listaSessoes.setSelectedValue(sessaoNaLista, true);
            if (listaSessoes.getSelectedValue() == null && !modeloSessoes.isEmpty())
                listaSessoes.setSelectedIndex(0);
        } finally {
            atualizando = false;
        }
        atualizarDetalhes();
    }

    private void configurarResumo() {
        resumoSessao.setEditable(false);
        resumoSessao.setLineWrap(true);
        resumoSessao.setWrapStyleWord(true);
        resumoSessao.setBackground(Color.WHITE);
        resumoSessao.setForeground(Tema.COR_TEXTO);
        resumoSessao.setBorder(new EmptyBorder(4, 2, 4, 2));
        resumoSessao.getAccessibleContext().setAccessibleName("Resumo da sessão selecionada");
    }

    private void atualizarDetalhes() {
        modeloParticipantes.clear();
        Sessao sessao = listaSessoes.getSelectedValue();
        if (sessao == null) {
            resumoSessao.setText("Selecione uma sessão na lista para ver os detalhes.");
            return;
        }
        resumoSessao.setText(sessao.gerarResumo() + "\nStatus: " + sessao.getStatus()
                + " | Vagas: " + (sessao.getCapacidadeMaxima() - sessao.getJogadores().size())
                + " | Presentes: " + sessao.quantidadePresentes()
                + " | Ausentes: " + sessao.quantidadeAusentes());
        resumoSessao.setCaretPosition(0);
        if (sessao.getJogadores().isEmpty()) {
            modeloParticipantes.addElement("Nenhum jogador inscrito.");
        } else {
            for (Map.Entry<Jogador, StatusPresenca> entrada : sessao.getPresencas().entrySet())
                modeloParticipantes.addElement(entrada.getKey().getNome() + " - " + entrada.getValue());
        }
    }

    // ---------------------------------------------------------------------
    // Leitura e validação de entradas
    // ---------------------------------------------------------------------
    private LocalDateTime lerDataHora(JTextField campo) {
        exigirTexto(campo, "Data e hora");
        try {
            return LocalDateTime.parse(campo.getText().trim(), formatoData);
        } catch (DateTimeParseException ex) {
            throw new ErroValidacao("Data inválida. Use dd/MM/aaaa HH:mm.", campo);
        }
    }

    private int lerCapacidade(JTextField campo) {
        int valor;
        try {
            valor = Integer.parseInt(campo.getText().trim());
        } catch (NumberFormatException ex) {
            throw new ErroValidacao("A capacidade deve ser um número inteiro.", campo);
        }
        if (valor <= 0) throw new ErroValidacao("A capacidade deve ser maior que zero.", campo);
        return valor;
    }

    private Optional<RegraParticipacao> criarRegraParticipacao(int indiceRegra) {
        if (indiceRegra == 0) {
            return Optional.of(new EntradaLivre());
        }

        if (gerenciador.getJogadores().isEmpty())
            throw new IllegalArgumentException("Cadastre jogadores antes de criar uma sessão por convite.");

        return seletorConvidados.selecionar(contexto.janela(), gerenciador.getJogadores())
                .map(EntradaPorConvite::new);
    }

    private Sessao sessaoSelecionada() {
        Sessao sessao = listaSessoes.getSelectedValue();
        if (sessao == null) throw new ErroValidacao("Selecione uma sessão na lista.", listaSessoes);
        return sessao;
    }

    private Jogador jogadorSelecionado() {
        Jogador jogador = (Jogador) comboJogadores.getSelectedItem();
        if (jogador == null) throw new ErroValidacao("Selecione um jogador.", comboJogadores);
        return jogador;
    }

    // ---------------------------------------------------------------------
    // Ações sobre a sessão selecionada
    // ---------------------------------------------------------------------
    private void alterarSessao(Consumer<Sessao> acao, String sucesso, String confirmacao) {
        contexto.executar(() -> {
            Sessao sessao = sessaoSelecionada();
            if (confirmacao != null && !Dialogos.confirmar(contexto.janela(), "Confirmar ação",
                    confirmacao + " \"" + sessao.getTitulo() + "\"?", confirmacao, "Voltar")) return;
            contexto.emSegundoPlano(() -> acao.accept(sessao), null, sucesso);
        });
    }

    private void excluirSessaoSelecionada() {
        Sessao sessao = listaSessoes.getSelectedValue();
        if (sessao == null) {
            Dialogos.aviso(contexto.janela(), "Selecione uma sessão na lista.");
            listaSessoes.requestFocusInWindow();
            return;
        }
        if (!Dialogos.confirmarExclusao(contexto.janela(), "Excluir a sessão " + sessao.getTitulo() + "?")) return;

        contexto.emSegundoPlano(() -> gerenciador.excluirSessao(sessao), null,
                "Sessão excluída com sucesso.");
    }
}
