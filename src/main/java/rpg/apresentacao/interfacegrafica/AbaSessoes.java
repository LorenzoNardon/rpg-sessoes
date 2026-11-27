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

/**
 * Aba de criação e gerenciamento de sessões: cadastro, inscrição de jogadores,
 * registro de presença, início, finalização, cancelamento e exclusão.
 */
class AbaSessoes extends AbaBase {

    /** Seletor responsável por escolher os jogadores que poderão participar por convite. */
    private final SeletorConvidados seletorConvidados;

    /** Modelo que armazena as sessões exibidas na lista. */
    private final DefaultListModel<Sessao> modeloSessoes = new DefaultListModel<>();

    /** Modelo que armazena os participantes da sessão selecionada. */
    private final DefaultListModel<String> modeloParticipantes = new DefaultListModel<>();

    /** Lista visual das sessões cadastradas. */
    private final JList<Sessao> listaSessoes = new JList<>(modeloSessoes);

    /** Lista visual dos participantes da sessão selecionada. */
    private final JList<String> listaParticipantes = new JList<>(modeloParticipantes);

    /** Combo usado para selecionar a campanha da nova sessão. */
    private final JComboBox<Campanha> comboCampanhas = new JComboBox<>();

    /** Combo usado para selecionar o jogador das ações da sessão. */
    private final JComboBox<Jogador> comboJogadores = new JComboBox<>();

    /** Área de texto que apresenta o resumo da sessão selecionada. */
    private final JTextArea resumoSessao = new JTextArea(3, 20);

    /** Formatador usado para interpretar datas no padrão dd/MM/yyyy HH:mm. */
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Botão responsável pela criação de novas sessões. */
    private final JButton botaoCriar;

    /** Indica que a interface está sendo atualizada, evitando reações indevidas aos eventos de seleção. */
    private boolean atualizando;

    /**
     * Constrói a aba, inicializando componentes, formulário, listas, detalhes e ações.
     *
     * @param gerenciador       serviço utilizado pela aba
     * @param contexto          contexto da janela principal
     * @param seletorConvidados seletor usado em sessões por convite
     */
    AbaSessoes(GerenciadorRpg gerenciador, ContextoTela contexto, SeletorConvidados seletorConvidados) {
        super(gerenciador, contexto);
        this.seletorConvidados = Objects.requireNonNull(seletorConvidados);

        estilizarLista(listaSessoes, "Sessões");
        estilizarLista(listaParticipantes, "Participantes da sessão");
        estilizarCombo(comboCampanhas);
        estilizarCombo(comboJogadores);
        configurarResumo();

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

                    Optional<RegraParticipacao> regraEscolhida =
                            criarRegraParticipacao(regra.getSelectedIndex());

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

        JPanel conteudoDetalhes = new JPanel(new BorderLayout(0, 8));
        conteudoDetalhes.setOpaque(false);
        conteudoDetalhes.add(resumoSessao, BorderLayout.NORTH);
        conteudoDetalhes.add(rolagem(listaParticipantes), BorderLayout.CENTER);
        JPanel cartaoDetalhes = cartao("Detalhes e participantes", conteudoDetalhes, null);

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

    /**
     * Retorna o botão de criação, usado pela tela principal como botão padrão da aba.
     *
     * @return botão de criação de sessão
     */
    @Override
    JButton getBotaoPadrao() {
        return botaoCriar;
    }

    /**
     * Atualiza os combos e a lista de sessões com os dados mais recentes do gerenciador,
     * preservando as seleções quando possível.
     */
    @Override
    void atualizar() {
        atualizando = true;

        try {
            Jogador jogadorNoCombo = (Jogador) comboJogadores.getSelectedItem();
            Campanha campanhaNoCombo = (Campanha) comboCampanhas.getSelectedItem();
            Sessao sessaoNaLista = listaSessoes.getSelectedValue();

            comboJogadores.removeAllItems();
            for (Jogador jogador : gerenciador.getJogadores())
                comboJogadores.addItem(jogador);

            comboCampanhas.removeAllItems();
            modeloSessoes.clear();

            for (Campanha campanha : gerenciador.getCampanhas()) {
                comboCampanhas.addItem(campanha);

                for (Sessao sessao : campanha.getSessoes())
                    modeloSessoes.addElement(sessao);
            }

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

    /**
     * Configura a aparência e as propriedades da área de resumo da sessão.
     */
    private void configurarResumo() {
        resumoSessao.setEditable(false);
        resumoSessao.setLineWrap(true);
        resumoSessao.setWrapStyleWord(true);
        resumoSessao.setBackground(Color.WHITE);
        resumoSessao.setForeground(Tema.COR_TEXTO);
        resumoSessao.setBorder(new EmptyBorder(4, 2, 4, 2));
        resumoSessao.getAccessibleContext().setAccessibleName("Resumo da sessão selecionada");
    }

    /**
     * Atualiza o resumo (status, vagas, presentes e ausentes) e a lista de participantes
     * de acordo com a sessão selecionada.
     */
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

    /**
     * Converte o texto informado em data e hora no formato dd/MM/aaaa HH:mm.
     *
     * @param campo campo com o texto da data
     * @return data e hora interpretadas
     * @throws ErroValidacao se o campo estiver vazio ou fora do formato esperado
     */
    private LocalDateTime lerDataHora(JTextField campo) {
        exigirTexto(campo, "Data e hora");

        try {
            return LocalDateTime.parse(campo.getText().trim(), formatoData);
        } catch (DateTimeParseException ex) {
            throw new ErroValidacao("Data inválida. Use dd/MM/aaaa HH:mm.", campo);
        }
    }

    /**
     * Converte a capacidade informada em um inteiro maior que zero.
     *
     * @param campo campo com o valor da capacidade
     * @return capacidade máxima da sessão
     * @throws ErroValidacao se o valor não for um inteiro ou não for positivo
     */
    private int lerCapacidade(JTextField campo) {
        int valor;

        try {
            valor = Integer.parseInt(campo.getText().trim());
        } catch (NumberFormatException ex) {
            throw new ErroValidacao("A capacidade deve ser um número inteiro.", campo);
        }

        if (valor <= 0)
            throw new ErroValidacao("A capacidade deve ser maior que zero.", campo);

        return valor;
    }

    /**
     * Cria a regra de participação de acordo com a opção escolhida.
     * Para entrada por convite, solicita ao usuário a seleção dos jogadores autorizados.
     *
     * @param indiceRegra índice da opção escolhida (0 = entrada livre, outro = por convite)
     * @return a regra criada, ou {@link Optional#empty()} se o usuário cancelar a seleção
     * @throws IllegalArgumentException se não houver jogadores cadastrados para o convite
     */
    private Optional<RegraParticipacao> criarRegraParticipacao(int indiceRegra) {
        if (indiceRegra == 0) {
            return Optional.of(new EntradaLivre());
        }

        if (gerenciador.getJogadores().isEmpty())
            throw new IllegalArgumentException("Cadastre jogadores antes de criar uma sessão por convite.");

        return seletorConvidados.selecionar(contexto.janela(), gerenciador.getJogadores())
                .map(EntradaPorConvite::new);
    }

    /**
     * Retorna a sessão selecionada na lista.
     *
     * @return sessão selecionada
     * @throws ErroValidacao se nenhuma sessão estiver selecionada
     */
    private Sessao sessaoSelecionada() {
        Sessao sessao = listaSessoes.getSelectedValue();

        if (sessao == null)
            throw new ErroValidacao("Selecione uma sessão na lista.", listaSessoes);

        return sessao;
    }

    /**
     * Retorna o jogador selecionado no combo.
     *
     * @return jogador selecionado
     * @throws ErroValidacao se nenhum jogador estiver selecionado
     */
    private Jogador jogadorSelecionado() {
        Jogador jogador = (Jogador) comboJogadores.getSelectedItem();

        if (jogador == null)
            throw new ErroValidacao("Selecione um jogador.", comboJogadores);

        return jogador;
    }

    /**
     * Executa uma ação sobre a sessão selecionada, podendo pedir confirmação antes.
     *
     * @param acao        operação aplicada à sessão
     * @param sucesso     mensagem exibida ao concluir
     * @param confirmacao texto da confirmação (também usado no botão de confirmar),
     *                    ou {@code null} para executar sem confirmar
     */
    private void alterarSessao(Consumer<Sessao> acao, String sucesso, String confirmacao) {
        contexto.executar(() -> {
            Sessao sessao = sessaoSelecionada();

            if (confirmacao != null && !Dialogos.confirmar(contexto.janela(), "Confirmar ação",
                    confirmacao + " \"" + sessao.getTitulo() + "\"?", confirmacao, "Voltar")) return;

            contexto.emSegundoPlano(() -> acao.accept(sessao), null, sucesso);
        });
    }

    /**
     * Exclui a sessão selecionada após a confirmação do usuário.
     */
    private void excluirSessaoSelecionada() {
        Sessao sessao = listaSessoes.getSelectedValue();

        if (sessao == null) {
            Dialogos.aviso(contexto.janela(), "Selecione uma sessão na lista.");
            listaSessoes.requestFocusInWindow();
            return;
        }

        if (!Dialogos.confirmarExclusao(contexto.janela(),
                "Excluir a sessão " + sessao.getTitulo() + "?")) return;

        contexto.emSegundoPlano(() -> gerenciador.excluirSessao(sessao), null,
                "Sessão excluída com sucesso.");
    }
}
