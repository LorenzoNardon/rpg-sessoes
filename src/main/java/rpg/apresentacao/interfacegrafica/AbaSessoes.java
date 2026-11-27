
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

    // Seletor responsável por escolher os jogadores que poderão participar por convite.
    private final SeletorConvidados seletorConvidados;

    // Modelos que armazenam as sessões e os participantes exibidos nas listas.
    private final DefaultListModel<Sessao> modeloSessoes = new DefaultListModel<>();
    private final DefaultListModel<String> modeloParticipantes = new DefaultListModel<>();

    // Listas visuais utilizadas para exibir sessões e seus respectivos participantes.
    private final JList<Sessao> listaSessoes = new JList<>(modeloSessoes);
    private final JList<String> listaParticipantes = new JList<>(modeloParticipantes);

    // Combos utilizados para selecionar campanhas e jogadores cadastrados.
    private final JComboBox<Campanha> comboCampanhas = new JComboBox<>();
    private final JComboBox<Jogador> comboJogadores = new JComboBox<>();

    // Área de texto responsável por apresentar o resumo da sessão selecionada.
    private final JTextArea resumoSessao = new JTextArea(3, 20);

    // Formatador utilizado para interpretar datas no padrão dd/MM/yyyy HH:mm.
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Botão responsável pela criação de novas sessões.
    private final JButton botaoCriar;

    // Indica quando a interface está sendo atualizada para evitar ações indevidas.
    private boolean atualizando;

    // Constrói a aba de sessões, inicializando os componentes,
    // formulários, listas, detalhes e ações disponíveis.
    AbaSessoes(GerenciadorRpg gerenciador, ContextoTela contexto, SeletorConvidados seletorConvidados) {
        super(gerenciador, contexto);
        this.seletorConvidados = Objects.requireNonNull(seletorConvidados);

        // Configura a aparência das listas e dos campos de seleção.
        estilizarLista(listaSessoes, "Sessões");
        estilizarLista(listaParticipantes, "Participantes da sessão");
        estilizarCombo(comboCampanhas);
        estilizarCombo(comboJogadores);
        configurarResumo();

        // ----- Formulário de nova sessão (coluna esquerda)

        // Cria os campos utilizados para informar os dados da nova sessão.
        JTextField titulo = campo("Ex.: A Cripta Esquecida - Parte 1");
        JTextField data = campo("dd/MM/aaaa HH:mm");
        JTextField capacidade = campo("5", "Número de vagas");
        JComboBox<String> tipo = new JComboBox<>(new String[]{"Ao vivo", "Por texto"});
        JTextField plataforma = campo("Ex.: Discord, Roll20");
        JTextField detalhe = campo("Link ou nome do canal");
        JComboBox<String> regra = new JComboBox<>(new String[]{"Entrada livre", "Por convite"});
        estilizarCombo(tipo);
        estilizarCombo(regra);

        // Monta o formulário e adiciona os campos com seus respectivos rótulos.
        JPanel form = formulario();
        linha(form, "Campanha", comboCampanhas);
        linha(form, "Título", titulo);
        linha(form, "Data e hora", data);
        linha(form, "Capacidade máxima", capacidade);
        linha(form, "Tipo", tipo);
        linha(form, "Plataforma", plataforma);
        linha(form, "Link da sala / canal", detalhe);
        linha(form, "Participação", regra);

        // Configura o botão de criação, validando os dados informados
        // e executando o cadastro da sessão em segundo plano.
        botaoCriar = botao("Criar sessão", Variante.PRIMARIO, KeyEvent.VK_C,
                "Cria a sessão com os dados informados", e -> contexto.executar(() -> {
                    Campanha campanha = (Campanha) comboCampanhas.getSelectedItem();

                    // Verifica se uma campanha foi selecionada antes de criar a sessão.
                    if (campanha == null)
                        throw new ErroValidacao("Cadastre e selecione uma campanha primeiro.", comboCampanhas);

                    exigirTexto(titulo, "Título");
                    LocalDateTime dataHora = lerDataHora(data);
                    int limite = lerCapacidade(capacidade);

                    // Cria a regra de participação escolhida pelo usuário.
                    Optional<RegraParticipacao> regraEscolhida =
                            criarRegraParticipacao(regra.getSelectedIndex());

                    if (regraEscolhida.isEmpty()) return;

                    String tituloSessao = titulo.getText().trim();
                    String plataformaSessao = plataforma.getText().trim();
                    String detalheSessao = detalhe.getText().trim();
                    RegraParticipacao regraSessao = regraEscolhida.get();

                    // Instancia o tipo de sessão correspondente à opção selecionada.
                    Sessao sessao = tipo.getSelectedIndex() == 0
                            ? new SessaoAoVivo(tituloSessao, dataHora, limite, plataformaSessao,
                            detalheSessao, regraSessao)
                            : new SessaoPorTexto(tituloSessao, dataHora, limite, plataformaSessao,
                            detalheSessao, regraSessao);

                    // Cadastra a sessão e limpa os campos após a conclusão.
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

        // Cria o cartão do formulário e define seu tamanho mínimo.
        JPanel cartaoForm = cartao("Nova sessão", form, null);
        cartaoForm.setMinimumSize(new Dimension(390, 300));

        // ----- Lista de sessões

        // Cria o botão responsável por excluir a sessão selecionada.
        JButton excluirSessao = botao("Excluir sessão", Variante.PERIGO, KeyEvent.VK_X,
                "Exclui a sessão selecionada", e -> excluirSessaoSelecionada());

        // Configura a área de rolagem e o cartão que contém a lista de sessões.
        JScrollPane rolagemSessoes = rolagem(listaSessoes);
        rolagemSessoes.setPreferredSize(new Dimension(300, 105));
        JPanel cartaoSessoes = cartao("Sessões", rolagemSessoes, excluirSessao);

        // Configura o atalho de exclusão e atualiza os detalhes ao selecionar uma sessão.
        atalhosLista(listaSessoes, null, this::excluirSessaoSelecionada);
        listaSessoes.setToolTipText("Selecione uma sessão. Delete: excluir.");
        listaSessoes.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !atualizando) atualizarDetalhes();
        });

        // ----- Detalhes da sessão selecionada

        // Organiza a área de resumo e a lista de participantes.
        JPanel conteudoDetalhes = new JPanel(new BorderLayout(0, 8));
        conteudoDetalhes.setOpaque(false);
        conteudoDetalhes.add(resumoSessao, BorderLayout.NORTH);
        conteudoDetalhes.add(rolagem(listaParticipantes), BorderLayout.CENTER);
        JPanel cartaoDetalhes = cartao("Detalhes e participantes", conteudoDetalhes, null);

        // ----- Ações

        // Cria o campo de seleção do jogador que participará das ações da sessão.
        JPanel selecao = formulario();
        linha(selecao, "Jogador", comboJogadores);

        // Inscreve o jogador selecionado na sessão escolhida.
        JButton inscrever = botao("Inscrever jogador", Variante.PRIMARIO, KeyEvent.VK_J,
                "Inscreve o jogador escolhido na sessão selecionada", e -> contexto.executar(() -> {
                    Sessao s = sessaoSelecionada();
                    Jogador j = jogadorSelecionado();
                    contexto.emSegundoPlano(() -> gerenciador.inscreverJogador(s, j), null, "Jogador inscrito.");
                }));

        // Registra a presença ou ausência de um jogador inscrito na sessão.
        JButton presenca = botao("Registrar presença", Variante.SECUNDARIO, KeyEvent.VK_P,
                "Registra presença ou ausência do jogador escolhido", e -> contexto.executar(() -> {
                    Sessao s = sessaoSelecionada();
                    Jogador j = jogadorSelecionado();

                    // Verifica se o jogador escolhido está inscrito na sessão.
                    if (!s.getJogadores().contains(j))
                        throw new ErroValidacao("O jogador selecionado não está inscrito nesta sessão.",
                                comboJogadores);

                    // Apresenta as opções de presença disponíveis.
                    Object[] opcoes = {"Presente", "Ausente"};
                    int escolha = JOptionPane.showOptionDialog(contexto.janela(), "Selecione a presença:",
                            "Presença", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                            opcoes, opcoes[0]);

                    if (escolha < 0) return;

                    // Converte a opção escolhida para o status correspondente.
                    StatusPresenca status = escolha == 0 ? StatusPresenca.PRESENTE : StatusPresenca.AUSENTE;

                    contexto.emSegundoPlano(() -> gerenciador.registrarPresenca(s, j, status), null,
                            "Presença registrada.");
                }));

        // Cria os botões para iniciar, finalizar e cancelar a sessão selecionada.
        JButton iniciar = botao("Iniciar", Variante.SECUNDARIO, KeyEvent.VK_I,
                "Inicia a sessão selecionada",
                e -> alterarSessao(gerenciador::iniciarSessao, "Sessão iniciada.", null));

        JButton finalizar = botao("Finalizar", Variante.SECUNDARIO, KeyEvent.VK_F,
                "Finaliza a sessão selecionada",
                e -> alterarSessao(gerenciador::finalizarSessao, "Sessão finalizada.", null));

        JButton cancelar = botao("Cancelar", Variante.PERIGO, KeyEvent.VK_A,
                "Cancela a sessão selecionada",
                e -> alterarSessao(gerenciador::cancelarSessao, "Sessão cancelada.", "Cancelar a sessão"));

        // Organiza os botões de ação em duas linhas.
        JPanel linhaCima = new JPanel(new GridLayout(1, 2, 8, 0));
        linhaCima.setOpaque(false);
        linhaCima.add(inscrever);
        linhaCima.add(presenca);

        JPanel linhaBaixo = new JPanel(new GridLayout(1, 3, 8, 0));
        linhaBaixo.setOpaque(false);
        linhaBaixo.add(iniciar);
        linhaBaixo.add(finalizar);
        linhaBaixo.add(cancelar);

        // Agrupa as linhas de botões e o seletor de jogadores no cartão de ações.
        JPanel botoes = new JPanel(new GridLayout(2, 1, 0, 8));
        botoes.setOpaque(false);
        botoes.add(linhaCima);
        botoes.add(linhaBaixo);

        JPanel conteudoAcoes = new JPanel(new BorderLayout(0, 6));
        conteudoAcoes.setOpaque(false);
        conteudoAcoes.add(selecao, BorderLayout.NORTH);
        conteudoAcoes.add(botoes, BorderLayout.CENTER);
        JPanel cartaoAcoes = cartao("Ações da sessão selecionada", conteudoAcoes, null);

        // Organiza os cartões de sessões, detalhes e ações na lateral direita.
        JPanel lado = new JPanel(new BorderLayout(0, 12));
        lado.setOpaque(false);
        lado.add(cartaoSessoes, BorderLayout.NORTH);
        lado.add(cartaoDetalhes, BorderLayout.CENTER);
        lado.add(cartaoAcoes, BorderLayout.SOUTH);
        lado.setMinimumSize(new Dimension(500, 300));

        // Divide a interface horizontalmente entre o formulário e os cartões laterais.
        JSplitPane divisao = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cartaoForm, lado);
        divisao.setResizeWeight(0.42);
        divisao.setContinuousLayout(true);
        divisao.setDividerSize(12);
        divisao.setBorder(null);
        add(divisao, BorderLayout.CENTER);
    }

    // Retorna o botão de criação para que a tela principal
    // possa utilizá-lo como botão padrão da aba.
    @Override
    JButton getBotaoPadrao() {
        return botaoCriar;
    }

    // Atualiza os combos e a lista de sessões com os dados mais recentes
    // obtidos pelo gerenciador, preservando as seleções quando possível.
    @Override
    void atualizar() {
        atualizando = true;

        try {
            // Armazena as seleções atuais antes de atualizar os componentes.
            Jogador jogadorNoCombo = (Jogador) comboJogadores.getSelectedItem();
            Campanha campanhaNoCombo = (Campanha) comboCampanhas.getSelectedItem();
            Sessao sessaoNaLista = listaSessoes.getSelectedValue();

            // Atualiza o combo de jogadores com os jogadores cadastrados.
            comboJogadores.removeAllItems();
            for (Jogador jogador : gerenciador.getJogadores())
                comboJogadores.addItem(jogador);

            // Atualiza o combo de campanhas e a lista com todas as sessões existentes.
            comboCampanhas.removeAllItems();
            modeloSessoes.clear();

            for (Campanha campanha : gerenciador.getCampanhas()) {
                comboCampanhas.addItem(campanha);

                for (Sessao sessao : campanha.getSessoes())
                    modeloSessoes.addElement(sessao);
            }

            // Mantém a seleção do usuário quando o item ainda existe.
            if (jogadorNoCombo != null) comboJogadores.setSelectedItem(jogadorNoCombo);
            if (campanhaNoCombo != null) comboCampanhas.setSelectedItem(campanhaNoCombo);
            if (sessaoNaLista != null) listaSessoes.setSelectedValue(sessaoNaLista, true);

            // Seleciona a primeira sessão quando nenhuma estiver selecionada.
            if (listaSessoes.getSelectedValue() == null && !modeloSessoes.isEmpty())
                listaSessoes.setSelectedIndex(0);

        } finally {
            // Libera a atualização para permitir novas interações do usuário.
            atualizando = false;
        }

        // Atualiza o resumo e os participantes da sessão selecionada.
        atualizarDetalhes();
    }

    // Configura a aparência e as propriedades da área de resumo da sessão.
    private void configurarResumo() {
        resumoSessao.setEditable(false);
        resumoSessao.setLineWrap(true);
        resumoSessao.setWrapStyleWord(true);
        resumoSessao.setBackground(Color.WHITE);
        resumoSessao.setForeground(Tema.COR_TEXTO);
        resumoSessao.setBorder(new EmptyBorder(4, 2, 4, 2));
        resumoSessao.getAccessibleContext().setAccessibleName("Resumo da sessão selecionada");
    }

    // Atualiza o resumo e a lista de participantes de acordo com a sessão selecionada.
    private void atualizarDetalhes() {
        modeloParticipantes.clear();

        Sessao sessao = listaSessoes.getSelectedValue();

        // Exibe uma mensagem caso nenhuma sessão esteja selecionada.
        if (sessao == null) {
            resumoSessao.setText("Selecione uma sessão na lista para ver os detalhes.");
            return;
        }

        // Apresenta o resumo, o status, as vagas disponíveis e os dados de presença.
        resumoSessao.setText(sessao.gerarResumo() + "\nStatus: " + sessao.getStatus()
                + " | Vagas: " + (sessao.getCapacidadeMaxima() - sessao.getJogadores().size())
                + " | Presentes: " + sessao.quantidadePresentes()
                + " | Ausentes: " + sessao.quantidadeAusentes());

        resumoSessao.setCaretPosition(0);

        // Exibe uma mensagem quando a sessão ainda não possui jogadores inscritos.
        if (sessao.getJogadores().isEmpty()) {
            modeloParticipantes.addElement("Nenhum jogador inscrito.");
        } else {
            // Adiciona à lista os nomes dos jogadores e seus respectivos status de presença.
            for (Map.Entry<Jogador, StatusPresenca> entrada : sessao.getPresencas().entrySet())
                modeloParticipantes.addElement(entrada.getKey().getNome() + " - " + entrada.getValue());
        }
    }

    // ---------------------------------------------------------------------
    // Leitura e validação de entradas
    // ---------------------------------------------------------------------

    // Converte o texto informado pelo usuário em uma data e hora.
    // Lança um erro de validação caso o conteúdo não corresponda ao formato esperado.
    private LocalDateTime lerDataHora(JTextField campo) {
        exigirTexto(campo, "Data e hora");

        try {
            return LocalDateTime.parse(campo.getText().trim(), formatoData);
        } catch (DateTimeParseException ex) {
            throw new ErroValidacao("Data inválida. Use dd/MM/aaaa HH:mm.", campo);
        }
    }

    // Converte a capacidade informada em um número inteiro.
    // Verifica se o valor é válido e maior que zero.
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

    // Cria a regra de participação de acordo com a opção escolhida.
    // Para entrada por convite, solicita ao usuário a seleção dos jogadores autorizados.
    private Optional<RegraParticipacao> criarRegraParticipacao(int indiceRegra) {
        if (indiceRegra == 0) {
            return Optional.of(new EntradaLivre());
        }

        // Impede a criação de uma sessão por convite quando não existem jogadores cadastrados.
        if (gerenciador.getJogadores().isEmpty())
            throw new IllegalArgumentException("Cadastre jogadores antes de criar uma sessão por convite.");

        return seletorConvidados.selecionar(contexto.janela(), gerenciador.getJogadores())
                .map(EntradaPorConvite::new);
    }

    // Retorna a sessão selecionada na lista.
    // Lança um erro de validação caso nenhuma sessão esteja selecionada.
    private Sessao sessaoSelecionada() {
        Sessao sessao = listaSessoes.getSelectedValue();

        if (sessao == null)
            throw new ErroValidacao("Selecione uma sessão na lista.", listaSessoes);

        return sessao;
    }

    // Retorna o jogador selecionado no combo.
    // Lança um erro de validação caso nenhum jogador esteja selecionado.
    private Jogador jogadorSelecionado() {
        Jogador jogador = (Jogador) comboJogadores.getSelectedItem();

        if (jogador == null)
            throw new ErroValidacao("Selecione um jogador.", comboJogadores);

        return jogador;
    }

    // ---------------------------------------------------------------------
    // Ações sobre a sessão selecionada
    // ---------------------------------------------------------------------

    // Executa uma ação sobre a sessão selecionada, podendo solicitar
    // confirmação antes de realizar a operação.
    private void alterarSessao(Consumer<Sessao> acao, String sucesso, String confirmacao) {
        contexto.executar(() -> {
            Sessao sessao = sessaoSelecionada();

            // Exibe um diálogo de confirmação quando a operação exigir confirmação.
            if (confirmacao != null && !Dialogos.confirmar(contexto.janela(), "Confirmar ação",
                    confirmacao + " \"" + sessao.getTitulo() + "\"?", confirmacao, "Voltar")) return;

            // Executa a operação em segundo plano e apresenta a mensagem de sucesso.
            contexto.emSegundoPlano(() -> acao.accept(sessao), null, sucesso);
        });
    }

    // Exclui a sessão selecionada após solicitar a confirmação do usuário.
    private void excluirSessaoSelecionada() {
        Sessao sessao = listaSessoes.getSelectedValue();

        // Impede a exclusão quando nenhuma sessão estiver selecionada.
        if (sessao == null) {
            Dialogos.aviso(contexto.janela(), "Selecione uma sessão na lista.");
            listaSessoes.requestFocusInWindow();
            return;
        }

        // Solicita confirmação antes de executar a exclusão.
        if (!Dialogos.confirmarExclusao(contexto.janela(),
                "Excluir a sessão " + sessao.getTitulo() + "?")) return;

        // Executa a exclusão em segundo plano e informa o resultado ao usuário.
        contexto.emSegundoPlano(() -> gerenciador.excluirSessao(sessao), null,
                "Sessão excluída com sucesso.");
    }
}
