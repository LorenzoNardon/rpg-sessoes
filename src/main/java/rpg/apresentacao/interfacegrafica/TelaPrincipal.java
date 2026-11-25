package rpg.apresentacao.interfacegrafica;

import rpg.dominio.modelo.*;
import rpg.dominio.regra.*;
import rpg.servico.GerenciadorRpg;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class TelaPrincipal extends JFrame {
    private final GerenciadorRpg gerenciador;
    private final SeletorConvidados seletorConvidados;
    private final DefaultListModel<Jogador> modeloJogadores = new DefaultListModel<>();
    private final DefaultListModel<Campanha> modeloCampanhas = new DefaultListModel<>();
    private final DefaultListModel<Sessao> modeloSessoes = new DefaultListModel<>();
    private final DefaultListModel<String> modeloParticipantes = new DefaultListModel<>();
    private final JList<Jogador> listaJogadores = new JList<>(modeloJogadores);
    private final JList<Campanha> listaCampanhas = new JList<>(modeloCampanhas);
    private final JList<Sessao> listaSessoes = new JList<>(modeloSessoes);
    private final JList<String> listaParticipantes = new JList<>(modeloParticipantes);
    private final JComboBox<Campanha> comboCampanhas = new JComboBox<>();
    private final JComboBox<Sessao> comboSessoes = new JComboBox<>();
    private final JComboBox<Jogador> comboJogadores = new JComboBox<>();
    private final JLabel resumoSessao = new JLabel("Selecione uma sessão para ver os detalhes.");
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public TelaPrincipal(GerenciadorRpg gerenciador) {
        this(gerenciador, new DialogoSelecaoConvidados());
    }

    public TelaPrincipal(GerenciadorRpg gerenciador, SeletorConvidados seletorConvidados) {
        this.gerenciador = Objects.requireNonNull(gerenciador);
        this.seletorConvidados = Objects.requireNonNull(seletorConvidados);
        setTitle("Gerenciador de Sessões RPG");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(850, 600));
        setLocationRelativeTo(null);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Jogadores", criarAbaJogadores());
        abas.addTab("Campanhas", criarAbaCampanhas());
        abas.addTab("Sessões", criarAbaSessoes());
        add(abas);
        comboSessoes.addActionListener(e -> atualizarDetalhesSessao());
        atualizarListas();
    }

    private JPanel painelBase(String titulo) {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel texto = new JLabel(titulo);
        texto.setFont(texto.getFont().deriveFont(Font.BOLD, 20f));
        painel.add(texto, BorderLayout.NORTH);
        return painel;
    }

    private JPanel criarAbaJogadores() {
        JPanel painel = painelBase("Cadastro de jogadores");
        JPanel formulario = new JPanel(new GridLayout(0, 1, 6, 6));
        JTextField nome = new JTextField();
        JTextField personagem = new JTextField();
        formulario.add(new JLabel("Nome do jogador"));
        formulario.add(nome);
        formulario.add(new JLabel("Nome do personagem"));
        formulario.add(personagem);
        JButton cadastrar = new JButton("Cadastrar jogador");
        cadastrar.addActionListener(e -> executar(() -> {
            gerenciador.cadastrarJogador(new Jogador(nome.getText().trim(), personagem.getText().trim()));
            nome.setText("");
            personagem.setText("");
            atualizarListas();
            mensagem("Jogador cadastrado com sucesso.");
        }));
        formulario.add(cadastrar);
        painel.add(formulario, BorderLayout.WEST);
        painel.add(new JScrollPane(listaJogadores), BorderLayout.CENTER);

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editar = new JButton("Editar selecionado");
        editar.addActionListener(e -> editarJogadorSelecionado());
        JButton excluir = new JButton("Excluir selecionado");
        excluir.addActionListener(e -> excluirJogadorSelecionado());
        acoes.add(editar);
        acoes.add(excluir);
        painel.add(acoes, BorderLayout.SOUTH);
        return painel;
    }

    private JPanel criarAbaCampanhas() {
        JPanel painel = painelBase("Cadastro de campanhas");
        JPanel formulario = new JPanel(new GridLayout(0, 1, 6, 6));
        JTextField nome = new JTextField();
        JTextField sistema = new JTextField();
        formulario.add(new JLabel("Nome da campanha"));
        formulario.add(nome);
        formulario.add(new JLabel("Sistema de RPG"));
        formulario.add(sistema);
        JButton cadastrar = new JButton("Cadastrar campanha");
        cadastrar.addActionListener(e -> executar(() -> {
            gerenciador.cadastrarCampanha(new Campanha(nome.getText().trim(), sistema.getText().trim()));
            nome.setText("");
            sistema.setText("");
            atualizarListas();
            mensagem("Campanha cadastrada com sucesso.");
        }));
        formulario.add(cadastrar);
        painel.add(formulario, BorderLayout.WEST);
        painel.add(new JScrollPane(listaCampanhas), BorderLayout.CENTER);

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editar = new JButton("Editar selecionada");
        editar.addActionListener(e -> editarCampanhaSelecionada());
        JButton excluir = new JButton("Excluir selecionada");
        excluir.addActionListener(e -> excluirCampanhaSelecionada());
        acoes.add(editar);
        acoes.add(excluir);
        painel.add(acoes, BorderLayout.SOUTH);
        return painel;
    }

    private JPanel criarAbaSessoes() {
        JPanel painel = painelBase("Criação e gerenciamento de sessões");
        JPanel formulario = new JPanel(new GridLayout(0, 2, 6, 6));
        JTextField titulo = new JTextField();
        JTextField data = new JTextField();
        JTextField capacidade = new JTextField("5");
        JComboBox<String> tipo = new JComboBox<>(new String[]{"Ao vivo", "Por texto"});
        JTextField plataforma = new JTextField();
        JTextField detalhe = new JTextField();
        JComboBox<String> regra = new JComboBox<>(new String[]{"Entrada livre", "Por convite"});
        formulario.add(new JLabel("Campanha")); formulario.add(comboCampanhas);
        formulario.add(new JLabel("Título")); formulario.add(titulo);
        formulario.add(new JLabel("Data e hora (dd/MM/aaaa HH:mm)")); formulario.add(data);
        formulario.add(new JLabel("Capacidade máxima")); formulario.add(capacidade);
        formulario.add(new JLabel("Tipo")); formulario.add(tipo);
        formulario.add(new JLabel("Plataforma")); formulario.add(plataforma);
        formulario.add(new JLabel("Link da sala / canal")); formulario.add(detalhe);
        formulario.add(new JLabel("Participação")); formulario.add(regra);

        JButton criar = new JButton("Criar sessão");
        criar.addActionListener(e -> executar(() -> {
            Campanha campanha = (Campanha) comboCampanhas.getSelectedItem();
            if (campanha == null) throw new IllegalArgumentException("Cadastre e selecione uma campanha primeiro.");
            LocalDateTime dataHora;
            try {
                dataHora = LocalDateTime.parse(data.getText().trim(), formatoData);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Data inválida. Use dd/MM/aaaa HH:mm.");
            }
            int limite;
            try {
                limite = Integer.parseInt(capacidade.getText().trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("A capacidade deve ser um número inteiro.");
            }

            Optional<RegraParticipacao> regraEscolhida = criarRegraParticipacao(regra.getSelectedIndex());
            if (regraEscolhida.isEmpty()) return;

            Sessao sessao;
            if (tipo.getSelectedIndex() == 0) {
                sessao = new SessaoAoVivo(titulo.getText().trim(), dataHora, limite,
                        plataforma.getText().trim(), detalhe.getText().trim(), regraEscolhida.get());
            } else {
                sessao = new SessaoPorTexto(titulo.getText().trim(), dataHora, limite,
                        plataforma.getText().trim(), detalhe.getText().trim(), regraEscolhida.get());
            }
            gerenciador.adicionarSessao(campanha, sessao);
            titulo.setText(""); data.setText(""); plataforma.setText(""); detalhe.setText("");
            atualizarListas();
            mensagem("Sessão criada com sucesso.");
        }));
        formulario.add(criar);

        JPanel detalhes = new JPanel(new BorderLayout(5, 5));
        detalhes.add(resumoSessao, BorderLayout.NORTH);
        detalhes.add(new JScrollPane(listaParticipantes), BorderLayout.CENTER);
        JPanel lado = new JPanel(new BorderLayout(8, 8));
        lado.add(new JScrollPane(listaSessoes), BorderLayout.NORTH);
        lado.add(detalhes, BorderLayout.CENTER);

        JPanel acoes = new JPanel(new GridLayout(0, 2, 5, 5));
        acoes.add(new JLabel("Sessão selecionada")); acoes.add(comboSessoes);
        acoes.add(new JLabel("Jogador")); acoes.add(comboJogadores);
        JButton inscrever = new JButton("Inscrever jogador");
        inscrever.addActionListener(e -> executar(() -> {
            Sessao s = sessaoSelecionada();
            Jogador j = jogadorSelecionado();
            gerenciador.inscreverJogador(s, j);
            atualizarListas();
            mensagem("Jogador inscrito.");
        }));
        JButton iniciar = new JButton("Iniciar");
        iniciar.addActionListener(e -> alterarSessao(0));
        JButton presenca = new JButton("Registrar presença");
        presenca.addActionListener(e -> executar(() -> {
            Sessao s = sessaoSelecionada();
            Jogador j = jogadorSelecionado();
            if (!s.getJogadores().contains(j))
                throw new IllegalArgumentException("O jogador selecionado não está inscrito nesta sessão.");
            Object[] opcoes = {"Presente", "Ausente"};
            int escolha = JOptionPane.showOptionDialog(this, "Selecione a presença:", "Presença",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[0]);
            if (escolha < 0) return;
            gerenciador.registrarPresenca(s, j,
                    escolha == 0 ? StatusPresenca.PRESENTE : StatusPresenca.AUSENTE);
            atualizarListas();
            mensagem("Presença registrada.");
        }));
        JButton finalizar = new JButton("Finalizar"); finalizar.addActionListener(e -> alterarSessao(1));
        JButton cancelar = new JButton("Cancelar"); cancelar.addActionListener(e -> alterarSessao(2));
        JButton excluir = new JButton("Excluir sessão");
        excluir.addActionListener(e -> excluirSessaoSelecionada());
        acoes.add(inscrever); acoes.add(iniciar); acoes.add(presenca); acoes.add(finalizar);
        acoes.add(cancelar); acoes.add(excluir);
        lado.add(acoes, BorderLayout.SOUTH);

        JSplitPane divisao = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formulario, lado);
        divisao.setResizeWeight(0.48);
        painel.add(divisao, BorderLayout.CENTER);
        return painel;
    }

    private Optional<RegraParticipacao> criarRegraParticipacao(int indiceRegra) {
        if (indiceRegra == 0) {
            return Optional.of(new EntradaLivre());
        }

        if (gerenciador.getJogadores().isEmpty())
            throw new IllegalArgumentException("Cadastre jogadores antes de criar uma sessão por convite.");

        return seletorConvidados.selecionar(this, gerenciador.getJogadores())
                .map(EntradaPorConvite::new);
    }

    private Sessao sessaoSelecionada() {
        Sessao sessao = (Sessao) comboSessoes.getSelectedItem();
        if (sessao == null) throw new IllegalArgumentException("Selecione uma sessão.");
        return sessao;
    }

    private Jogador jogadorSelecionado() {
        Jogador jogador = (Jogador) comboJogadores.getSelectedItem();
        if (jogador == null) throw new IllegalArgumentException("Selecione um jogador.");
        return jogador;
    }

    private void alterarSessao(int acao) {
        executar(() -> {
            Sessao sessao = sessaoSelecionada();
            if (acao == 0) gerenciador.iniciarSessao(sessao);
            else if (acao == 1) gerenciador.finalizarSessao(sessao);
            else gerenciador.cancelarSessao(sessao);
            atualizarListas();
            mensagem("Operação realizada com sucesso.");
        });
    }

    private void editarJogadorSelecionado() {
        Jogador jogador = listaJogadores.getSelectedValue();
        if (jogador == null) {
            aviso("Selecione um jogador na lista.");
            return;
        }

        JTextField nome = new JTextField(jogador.getNome());
        JTextField personagem = new JTextField(jogador.getNomePersonagem());
        JPanel formulario = formularioEdicao(
                "Nome do jogador", nome,
                "Nome do personagem", personagem
        );
        if (JOptionPane.showConfirmDialog(this, formulario, "Editar jogador",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        executar(() -> {
            gerenciador.atualizarJogador(jogador, nome.getText().trim(), personagem.getText().trim());
            atualizarListas();
            mensagem("Jogador atualizado com sucesso.");
        });
    }

    private void excluirJogadorSelecionado() {
        Jogador jogador = listaJogadores.getSelectedValue();
        if (jogador == null) {
            aviso("Selecione um jogador na lista.");
            return;
        }
        if (!confirmarExclusao("Excluir o jogador " + jogador.getNome() + "?")) return;

        executar(() -> {
            gerenciador.excluirJogador(jogador);
            atualizarListas();
            mensagem("Jogador excluído com sucesso.");
        });
    }

    private void editarCampanhaSelecionada() {
        Campanha campanha = listaCampanhas.getSelectedValue();
        if (campanha == null) {
            aviso("Selecione uma campanha na lista.");
            return;
        }

        JTextField nome = new JTextField(campanha.getNome());
        JTextField sistema = new JTextField(campanha.getSistemaRpg());
        JPanel formulario = formularioEdicao(
                "Nome da campanha", nome,
                "Sistema de RPG", sistema
        );
        if (JOptionPane.showConfirmDialog(this, formulario, "Editar campanha",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        executar(() -> {
            gerenciador.atualizarCampanha(campanha, nome.getText().trim(), sistema.getText().trim());
            atualizarListas();
            mensagem("Campanha atualizada com sucesso.");
        });
    }

    private void excluirCampanhaSelecionada() {
        Campanha campanha = listaCampanhas.getSelectedValue();
        if (campanha == null) {
            aviso("Selecione uma campanha na lista.");
            return;
        }
        if (!confirmarExclusao("Excluir a campanha " + campanha.getNome() + "?")) return;

        executar(() -> {
            gerenciador.excluirCampanha(campanha);
            atualizarListas();
            mensagem("Campanha excluída com sucesso.");
        });
    }

    private void excluirSessaoSelecionada() {
        Sessao sessao;
        try {
            sessao = sessaoSelecionada();
        } catch (RuntimeException ex) {
            aviso(ex.getMessage());
            return;
        }
        if (!confirmarExclusao("Excluir a sessão " + sessao.getTitulo() + "?")) return;

        executar(() -> {
            gerenciador.excluirSessao(sessao);
            atualizarListas();
            mensagem("Sessão excluída com sucesso.");
        });
    }

    private JPanel formularioEdicao(String rotuloPrimeiro, JTextField primeiro,
                                    String rotuloSegundo, JTextField segundo) {
        JPanel formulario = new JPanel(new GridLayout(0, 1, 5, 5));
        formulario.add(new JLabel(rotuloPrimeiro));
        formulario.add(primeiro);
        formulario.add(new JLabel(rotuloSegundo));
        formulario.add(segundo);
        return formulario;
    }

    private boolean confirmarExclusao(String mensagem) {
        return JOptionPane.showConfirmDialog(this, mensagem, "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void atualizarDetalhesSessao() {
        modeloParticipantes.clear();
        Sessao sessao = (Sessao) comboSessoes.getSelectedItem();
        if (sessao == null) {
            resumoSessao.setText("Selecione uma sessão para ver os detalhes.");
            return;
        }
        resumoSessao.setText("<html>" + sessao.gerarResumo() + "<br>Status: " + sessao.getStatus()
                + " | Vagas: " + (sessao.getCapacidadeMaxima() - sessao.getJogadores().size())
                + " | Presentes: " + sessao.quantidadePresentes()
                + " | Ausentes: " + sessao.quantidadeAusentes() + "</html>");
        if (sessao.getJogadores().isEmpty()) {
            modeloParticipantes.addElement("Nenhum jogador inscrito.");
        } else {
            for (Map.Entry<Jogador, StatusPresenca> entrada : sessao.getPresencas().entrySet())
                modeloParticipantes.addElement(entrada.getKey().getNome() + " - " + entrada.getValue());
        }
    }

    private void atualizarListas() {
        modeloJogadores.clear();
        comboJogadores.removeAllItems();
        for (Jogador jogador : gerenciador.getJogadores()) {
            modeloJogadores.addElement(jogador);
            comboJogadores.addItem(jogador);
        }
        modeloCampanhas.clear();
        comboCampanhas.removeAllItems();
        modeloSessoes.clear();
        comboSessoes.removeAllItems();
        for (Campanha campanha : gerenciador.getCampanhas()) {
            modeloCampanhas.addElement(campanha);
            comboCampanhas.addItem(campanha);
            for (Sessao sessao : campanha.getSessoes()) {
                modeloSessoes.addElement(sessao);
                comboSessoes.addItem(sessao);
            }
        }
        atualizarDetalhesSessao();
    }

    private void executar(Runnable operacao) {
        try {
            operacao.run();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Atenção", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void mensagem(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Gerenciador de RPG", JOptionPane.INFORMATION_MESSAGE);
    }

    private void aviso(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Atenção", JOptionPane.WARNING_MESSAGE);
    }
}
