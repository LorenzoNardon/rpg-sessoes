package rpg.interfacegrafica;

import rpg.modelo.*;
import rpg.regra.*;
import rpg.servico.GerenciadorRpg;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TelaPrincipal extends JFrame {
    private final GerenciadorRpg gerenciador = new GerenciadorRpg();
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

    public TelaPrincipal() {
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

            RegraParticipacao regraEscolhida;
            if (regra.getSelectedIndex() == 0) {
                regraEscolhida = new EntradaLivre();
            } else {
                regraEscolhida = new EntradaPorConvite(selecionarConvidados());
            }

            Sessao sessao;
            if (tipo.getSelectedIndex() == 0) {
                sessao = new SessaoAoVivo(titulo.getText().trim(), dataHora, limite,
                        plataforma.getText().trim(), detalhe.getText().trim(), regraEscolhida);
            } else {
                sessao = new SessaoPorTexto(titulo.getText().trim(), dataHora, limite,
                        plataforma.getText().trim(), detalhe.getText().trim(), regraEscolhida);
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
            s.adicionarJogador(j);
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
            s.registrarPresenca(j, escolha == 0 ? StatusPresenca.PRESENTE : StatusPresenca.AUSENTE);
            atualizarListas();
            mensagem("Presença registrada.");
        }));
        JButton finalizar = new JButton("Finalizar"); finalizar.addActionListener(e -> alterarSessao(1));
        JButton cancelar = new JButton("Cancelar"); cancelar.addActionListener(e -> alterarSessao(2));
        acoes.add(inscrever); acoes.add(iniciar); acoes.add(presenca); acoes.add(finalizar); acoes.add(cancelar);
        lado.add(acoes, BorderLayout.SOUTH);

        JSplitPane divisao = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formulario, lado);
        divisao.setResizeWeight(0.48);
        painel.add(divisao, BorderLayout.CENTER);
        return painel;
    }

    private List<Jogador> selecionarConvidados() {
        if (gerenciador.getJogadores().isEmpty())
            throw new IllegalArgumentException("Cadastre jogadores antes de criar uma sessão por convite.");

        JList<Jogador> selecao = new JList<>(gerenciador.getJogadores().toArray(new Jogador[0]));
        selecao.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        int resultado = JOptionPane.showConfirmDialog(this, new JScrollPane(selecao),
                "Selecione os jogadores convidados", JOptionPane.OK_CANCEL_OPTION);
        if (resultado != JOptionPane.OK_OPTION)
            throw new IllegalArgumentException("Criação da sessão cancelada.");
        if (selecao.getSelectedValuesList().isEmpty())
            throw new IllegalArgumentException("Selecione pelo menos um jogador convidado.");
        return new ArrayList<>(selecao.getSelectedValuesList());
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
            if (acao == 0) sessao.iniciar();
            else if (acao == 1) sessao.finalizar();
            else sessao.cancelar();
            atualizarListas();
            mensagem("Operação realizada com sucesso.");
        });
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
}
