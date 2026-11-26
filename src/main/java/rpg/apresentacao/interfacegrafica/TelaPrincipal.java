package rpg.apresentacao.interfacegrafica;

import rpg.dominio.modelo.*;
import rpg.dominio.regra.*;
import rpg.servico.GerenciadorRpg;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class TelaPrincipal extends JFrame {

    // ---------------------------------------------------------------------
    // Identidade visual: paleta única usada em todas as telas
    // ---------------------------------------------------------------------
    private static final Color COR_FUNDO = new Color(0xF4F2EC);
    private static final Color COR_BORDA = new Color(0xCFCABB);
    private static final Color COR_TEXTO = new Color(0x1F1D2B);
    private static final Color COR_TEXTO_SUAVE = new Color(0x5C5870);
    private static final Color COR_PRIMARIA = new Color(0x3B3486);
    private static final Color COR_PRIMARIA_HOVER = new Color(0x4C44A6);
    private static final Color COR_PRIMARIA_ESCURA = new Color(0x2B2565);
    private static final Color COR_PERIGO = new Color(0xB3261E);
    private static final Color COR_PERIGO_HOVER = new Color(0x8F1D17);
    private static final Color COR_SUCESSO = new Color(0x1B7F3B);
    private static final Color COR_SELECAO = new Color(0xDAD6F5);
    private static final Color COR_FOCO = new Color(0xD98E04);

    private static final String PROP_ERRO = "campoComErro";

    static {
        configurarAparencia();
    }

    private enum Variante {
        PRIMARIO(COR_PRIMARIA, COR_PRIMARIA_HOVER, Color.WHITE, COR_PRIMARIA_ESCURA),
        SECUNDARIO(Color.WHITE, COR_SELECAO, COR_PRIMARIA_ESCURA, COR_PRIMARIA),
        PERIGO(COR_PERIGO, COR_PERIGO_HOVER, Color.WHITE, COR_PERIGO_HOVER);

        final Color fundo;
        final Color fundoHover;
        final Color texto;
        final Color contorno;

        Variante(Color fundo, Color fundoHover, Color texto, Color contorno) {
            this.fundo = fundo;
            this.fundoHover = fundoHover;
            this.texto = texto;
            this.contorno = contorno;
        }
    }

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
    private final JComboBox<Jogador> comboJogadores = new JComboBox<>();
    private final JTextArea resumoSessao = new JTextArea(3, 20);
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JTabbedPane abas = new JTabbedPane();
    private final JButton[] botoesPadrao = new JButton[3];
    private final JLabel textoStatus = new JLabel("Pronto.");
    private final JProgressBar barraProgresso = new JProgressBar();
    private final JPanel painelOcupado = new JPanel();
    private final Timer temporizadorStatus = new Timer(7000, e -> definirStatusPadrao());
    private boolean ocupado;
    private boolean atualizando;

    public TelaPrincipal(GerenciadorRpg gerenciador) {
        this(gerenciador, new DialogoSelecaoConvidados());
    }

    public TelaPrincipal(GerenciadorRpg gerenciador, SeletorConvidados seletorConvidados) {
        this.gerenciador = Objects.requireNonNull(gerenciador);
        this.seletorConvidados = Objects.requireNonNull(seletorConvidados);
        setTitle("Gerenciador de Sessões RPG");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 740);
        setMinimumSize(new Dimension(960, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(COR_FUNDO);
        setIconImages(java.util.List.of(imagemD20(32), imagemD20(64)));

        estilizarLista(listaJogadores, "Jogadores cadastrados");
        estilizarLista(listaCampanhas, "Campanhas cadastradas");
        estilizarLista(listaSessoes, "Sessões");
        estilizarLista(listaParticipantes, "Participantes da sessão");
        estilizarCombo(comboCampanhas);
        estilizarCombo(comboJogadores);
        configurarResumo();
        configurarBloqueioOcupado();
        temporizadorStatus.setRepeats(false);

        abas.setFont(abas.getFont().deriveFont(Font.BOLD));
        abas.addTab("Jogadores", criarAbaJogadores());
        abas.addTab("Campanhas", criarAbaCampanhas());
        abas.addTab("Sessões", criarAbaSessoes());
        abas.addChangeListener(e -> getRootPane().setDefaultButton(botoesPadrao[abas.getSelectedIndex()]));

        add(criarCabecalho(), BorderLayout.NORTH);
        add(abas, BorderLayout.CENTER);
        add(criarBarraStatus(), BorderLayout.SOUTH);
        getRootPane().setDefaultButton(botoesPadrao[0]);

        listaSessoes.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !atualizando) atualizarDetalhesSessao();
        });
        atualizarListas();
    }

    // ---------------------------------------------------------------------
    // Aparência global
    // ---------------------------------------------------------------------
    private static void configurarAparencia() {
        System.setProperty("swing.boldMetal", "false");
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignorada) {
            // mantém o visual padrão
        }
        Font base = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        for (Object chave : Collections.list(UIManager.getDefaults().keys())) {
            if (UIManager.get(chave) instanceof FontUIResource) {
                UIManager.put(chave, new FontUIResource(base));
            }
        }
        UIManager.put("Panel.background", COR_FUNDO);
        UIManager.put("OptionPane.background", COR_FUNDO);
        UIManager.put("OptionPane.messageForeground", COR_TEXTO);
        UIManager.put("Label.foreground", COR_TEXTO);
        UIManager.put("List.selectionBackground", COR_SELECAO);
        UIManager.put("List.selectionForeground", COR_TEXTO);
        UIManager.put("TabbedPane.selected", COR_FUNDO);
        UIManager.put("TabbedPane.focus", COR_FOCO);
        UIManager.put("TabbedPane.tabInsets", new Insets(8, 18, 8, 18));
        UIManager.put("Button.focus", COR_FOCO);
        UIManager.put("OptionPane.okButtonText", "OK");
        UIManager.put("OptionPane.cancelButtonText", "Cancelar");
        UIManager.put("OptionPane.yesButtonText", "Sim");
        UIManager.put("OptionPane.noButtonText", "Não");
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(COR_PRIMARIA_ESCURA);
        cabecalho.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, COR_FOCO),
                new EmptyBorder(14, 20, 14, 20)));
        JLabel titulo = new JLabel("Gerenciador de Sessões RPG");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        titulo.setForeground(Color.WHITE);
        JLabel subtitulo = new JLabel("Jogadores, campanhas e sessões em um só lugar");
        subtitulo.setForeground(new Color(0xD8D4F2));
        JPanel textos = new JPanel(new GridLayout(0, 1, 0, 2));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(subtitulo);

        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        subtitulo.setHorizontalAlignment(SwingConstants.CENTER);

        // Dois d20 do mesmo tamanho, um em cada lado, com o título no meio
        JLabel dadoEsquerdo = new JLabel(iconeD20Dourado(52, "20"));
        JLabel dadoDireito = new JLabel(iconeD20Dourado(52, "20"));
        dadoEsquerdo.getAccessibleContext().setAccessibleName("Dado de 20 lados");
        dadoDireito.getAccessibleContext().setAccessibleName("Dado de 20 lados");
        cabecalho.add(dadoEsquerdo, BorderLayout.WEST);
        cabecalho.add(textos, BorderLayout.CENTER);
        cabecalho.add(dadoDireito, BorderLayout.EAST);
        return cabecalho;
    }

    private static IconeD20 iconeD20Dourado(int tamanho, String numero) {
        return new IconeD20(tamanho, numero, COR_FOCO, new Color(0xF0B33C),
                COR_PRIMARIA_ESCURA, COR_PRIMARIA_ESCURA);
    }

    private static Image imagemD20(int tamanho) {
        BufferedImage imagem = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagem.createGraphics();
        iconeD20Dourado(tamanho, "20").paintIcon(null, g, 0, 0);
        g.dispose();
        return imagem;
    }

    /** Dado de 20 lados (icosaedro visto de frente) desenhado em Java2D, em qualquer tamanho. */
    private static class IconeD20 implements Icon {
        private final int tamanho;
        private final String numero;
        private final Color corpo;
        private final Color faceCentral;
        private final Color linhas;
        private final Color corNumero;

        IconeD20(int tamanho, String numero, Color corpo, Color faceCentral, Color linhas, Color corNumero) {
            this.tamanho = tamanho;
            this.numero = numero;
            this.corpo = corpo;
            this.faceCentral = faceCentral;
            this.linhas = linhas;
            this.corNumero = corNumero;
        }

        private static Path2D poligono(double cx, double cy, double raio, double... angulos) {
            Path2D.Double caminho = new Path2D.Double();
            for (int i = 0; i < angulos.length; i++) {
                double a = Math.toRadians(angulos[i]);
                double px = cx + raio * Math.cos(a);
                double py = cy + raio * Math.sin(a);
                if (i == 0) caminho.moveTo(px, py);
                else caminho.lineTo(px, py);
            }
            caminho.closePath();
            return caminho;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            double cx = x + tamanho / 2.0;
            double cy = y + tamanho / 2.0;
            double raio = tamanho / 2.0 - 1;
            double raioTriangulo = raio * 0.55;

            Path2D contorno = poligono(cx, cy, raio, -90, -30, 30, 90, 150, 210);
            Path2D triangulo = poligono(cx, cy, raioTriangulo, -90, 30, 150);
            g2.setColor(corpo);
            g2.fill(contorno);
            g2.setColor(faceCentral);
            g2.fill(triangulo);

            g2.setColor(linhas);
            g2.setStroke(new BasicStroke(Math.max(1f, tamanho / 28f),
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(contorno);
            g2.draw(triangulo);
            // Cada vértice do triângulo se liga aos três vértices mais próximos do contorno
            for (double angulo : new double[]{-90, 30, 150}) {
                double ax = cx + raioTriangulo * Math.cos(Math.toRadians(angulo));
                double ay = cy + raioTriangulo * Math.sin(Math.toRadians(angulo));
                for (double desvio : new double[]{-60, 0, 60}) {
                    double bx = cx + raio * Math.cos(Math.toRadians(angulo + desvio));
                    double by = cy + raio * Math.sin(Math.toRadians(angulo + desvio));
                    g2.draw(new java.awt.geom.Line2D.Double(ax, ay, bx, by));
                }
            }

            if (tamanho >= 28 && numero != null) {
                g2.setColor(corNumero);
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.round(tamanho * 0.22f)));
                FontMetrics fm = g2.getFontMetrics();
                double tx = cx - fm.stringWidth(numero) / 2.0;
                double ty = cy + (fm.getAscent() - fm.getDescent()) / 2.0 + tamanho * 0.05;
                g2.drawString(numero, (float) tx, (float) ty);
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return tamanho;
        }

        @Override
        public int getIconHeight() {
            return tamanho;
        }
    }

    private JPanel criarBarraStatus() {
        JPanel barra = new JPanel(new BorderLayout(12, 0));
        barra.setBackground(Color.WHITE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COR_BORDA),
                new EmptyBorder(7, 16, 7, 16)));
        textoStatus.setIconTextGap(8);
        textoStatus.getAccessibleContext().setAccessibleName("Mensagem de status");
        barraProgresso.setIndeterminate(true);
        barraProgresso.setVisible(false);
        barraProgresso.setPreferredSize(new Dimension(140, 14));
        barraProgresso.setForeground(COR_PRIMARIA);
        barra.add(textoStatus, BorderLayout.CENTER);
        barra.add(barraProgresso, BorderLayout.EAST);
        definirStatusPadrao();
        return barra;
    }

    // ---------------------------------------------------------------------
    // Feedback e operações em segundo plano
    // ---------------------------------------------------------------------
    private void definirStatusPadrao() {
        textoStatus.setText("Pronto.");
        textoStatus.setForeground(COR_TEXTO_SUAVE);
        textoStatus.setIcon(new IconePonto(COR_TEXTO_SUAVE));
    }

    private void informar(String texto, boolean erro) {
        Color cor = erro ? COR_PERIGO : COR_SUCESSO;
        textoStatus.setText(texto);
        textoStatus.setForeground(cor);
        textoStatus.setIcon(new IconePonto(cor));
        temporizadorStatus.restart();
    }

    private void configurarBloqueioOcupado() {
        painelOcupado.setOpaque(false);
        painelOcupado.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        painelOcupado.addMouseListener(new MouseAdapter() { });
        painelOcupado.addMouseMotionListener(new MouseMotionAdapter() { });
        setGlassPane(painelOcupado);
    }

    private void definirOcupado(boolean valor) {
        ocupado = valor;
        painelOcupado.setVisible(valor);
        barraProgresso.setVisible(valor);
        if (valor) {
            temporizadorStatus.stop();
            textoStatus.setText("Processando...");
            textoStatus.setForeground(COR_TEXTO_SUAVE);
            textoStatus.setIcon(new IconePonto(COR_PRIMARIA));
        }
    }

    /** Executa na thread da interface: validações e diálogos. Erros viram aviso ao usuário. */
    private void executar(Runnable operacao) {
        try {
            operacao.run();
        } catch (RuntimeException ex) {
            tratarErro(ex);
        }
    }

    /** Executa a tarefa fora da thread da interface, sem congelar a janela. */
    private void emSegundoPlano(Runnable tarefa, Runnable aoSucesso, String mensagemSucesso) {
        if (ocupado) return;
        definirOcupado(true);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                tarefa.run();
                return null;
            }

            @Override
            protected void done() {
                definirOcupado(false);
                Throwable falha = null;
                try {
                    get();
                } catch (ExecutionException ex) {
                    falha = ex.getCause() != null ? ex.getCause() : ex;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    falha = ex;
                }
                if (falha == null && aoSucesso != null) aoSucesso.run();
                atualizarListas();
                if (falha == null) informar(mensagemSucesso, false);
                else tratarErro(falha);
            }
        }.execute();
    }

    private void tratarErro(Throwable ex) {
        String texto = ex.getMessage() == null || ex.getMessage().isBlank()
                ? "Ocorreu um erro inesperado." : ex.getMessage();
        JComponent foco = null;
        if (ex instanceof ErroValidacao) {
            foco = ((ErroValidacao) ex).campo;
            marcarErro(foco);
        }
        informar("Atenção: " + texto, true);
        aviso(texto);
        if (foco != null) foco.requestFocusInWindow();
    }

    private static class ErroValidacao extends IllegalArgumentException {
        private final transient JComponent campo;

        ErroValidacao(String mensagem, JComponent campo) {
            super(mensagem);
            this.campo = campo;
        }
    }

    private void exigirTexto(JTextField campo, String rotulo) {
        if (campo.getText().trim().isEmpty())
            throw new ErroValidacao("Preencha o campo \"" + rotulo + "\".", campo);
    }

    private static class IconePonto implements Icon {
        private final Color cor;

        IconePonto(Color cor) {
            this.cor = cor;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(cor);
            g2.fillOval(x, y, 10, 10);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 10;
        }

        @Override
        public int getIconHeight() {
            return 10;
        }
    }

    // ---------------------------------------------------------------------
    // Fábricas de componentes padronizados
    // ---------------------------------------------------------------------
    private static Border borda(Color cor, int espessura, int vertical, int horizontal) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(cor, espessura),
                new EmptyBorder(vertical - espessura, horizontal - espessura,
                        vertical - espessura, horizontal - espessura));
    }

    private static class CampoTexto extends JTextField {
        private final String dica;

        CampoTexto(String texto, String dica) {
            super(texto);
            this.dica = dica;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (dica != null && getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(0x8A869B));
                g2.setFont(getFont().deriveFont(Font.ITALIC));
                FontMetrics fm = g2.getFontMetrics();
                Insets in = getInsets();
                int y = in.top + (getHeight() - in.top - in.bottom - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(dica, in.left, y);
                g2.dispose();
            }
        }
    }

    private JTextField campo(String dica) {
        return campo("", dica);
    }

    private JTextField campo(String texto, String dica) {
        CampoTexto campo = new CampoTexto(texto, dica);
        campo.setColumns(18);
        campo.setBackground(Color.WHITE);
        campo.setForeground(COR_TEXTO);
        campo.setCaretColor(COR_PRIMARIA);
        campo.setSelectionColor(COR_SELECAO);
        campo.setSelectedTextColor(COR_TEXTO);
        atualizarBordaCampo(campo);
        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                atualizarBordaCampo(campo);
            }

            @Override
            public void focusLost(FocusEvent e) {
                atualizarBordaCampo(campo);
            }
        });
        DocumentListener limpaErro = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                limparErro(campo);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                limparErro(campo);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                limparErro(campo);
            }
        };
        campo.getDocument().addDocumentListener(limpaErro);
        return campo;
    }

    private void atualizarBordaCampo(JComponent campo) {
        if (Boolean.TRUE.equals(campo.getClientProperty(PROP_ERRO))) {
            campo.setBorder(borda(COR_PERIGO, 2, 8, 10));
        } else if (campo.isFocusOwner()) {
            campo.setBorder(borda(COR_FOCO, 2, 8, 10));
        } else {
            campo.setBorder(borda(COR_BORDA, 1, 8, 10));
        }
        campo.repaint();
    }

    private void marcarErro(JComponent campo) {
        if (campo instanceof JTextComponent) {
            campo.putClientProperty(PROP_ERRO, Boolean.TRUE);
            atualizarBordaCampo(campo);
        }
    }

    private void limparErro(JComponent campo) {
        if (Boolean.TRUE.equals(campo.getClientProperty(PROP_ERRO))) {
            campo.putClientProperty(PROP_ERRO, Boolean.FALSE);
            atualizarBordaCampo(campo);
        }
    }

    private JButton botao(String texto, Variante variante, int mnemonico, String dica, ActionListener acao) {
        JButton botao = new JButton(texto);
        botao.setOpaque(true);
        botao.setContentAreaFilled(true);
        botao.setFocusPainted(false);
        botao.setBackground(variante.fundo);
        botao.setForeground(variante.texto);
        botao.setBorder(borda(variante.contorno, 1, 10, 16));
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.setMnemonic(mnemonico);
        botao.setToolTipText(dica + " (Alt+" + (char) mnemonico + ")");
        botao.addActionListener(acao);
        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (botao.isEnabled()) botao.setBackground(variante.fundoHover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(variante.fundo);
            }
        });
        botao.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                botao.setBorder(borda(COR_FOCO, 3, 10, 16));
            }

            @Override
            public void focusLost(FocusEvent e) {
                botao.setBorder(borda(variante.contorno, 1, 10, 16));
            }
        });
        return botao;
    }

    private static class RenderizadorLista extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                      boolean selecionado, boolean comFoco) {
            JLabel rotulo = (JLabel) super.getListCellRendererComponent(lista, valor, indice, selecionado, comFoco);
            Border contorno = comFoco && indice >= 0
                    ? BorderFactory.createLineBorder(COR_FOCO, 1)
                    : new EmptyBorder(1, 1, 1, 1);
            rotulo.setBorder(BorderFactory.createCompoundBorder(contorno, new EmptyBorder(5, 9, 5, 9)));
            rotulo.setFont(lista.getFont());
            if (selecionado) {
                rotulo.setBackground(COR_SELECAO);
                rotulo.setForeground(COR_TEXTO);
            } else {
                rotulo.setBackground(Color.WHITE);
                rotulo.setForeground(COR_TEXTO);
                if (valor instanceof String) {
                    String texto = (String) valor;
                    if (texto.endsWith(StatusPresenca.PRESENTE.toString())) {
                        rotulo.setForeground(COR_SUCESSO);
                    } else if (texto.endsWith(StatusPresenca.AUSENTE.toString())) {
                        rotulo.setForeground(COR_PERIGO);
                    } else if (texto.startsWith("Nenhum")) {
                        rotulo.setForeground(COR_TEXTO_SUAVE);
                        rotulo.setFont(lista.getFont().deriveFont(Font.ITALIC));
                    }
                }
            }
            return rotulo;
        }
    }

    private <T> void estilizarLista(JList<T> lista, String nomeAcessivel) {
        lista.setCellRenderer(new RenderizadorLista());
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setBackground(Color.WHITE);
        lista.getAccessibleContext().setAccessibleName(nomeAcessivel);
    }

    private <T> void estilizarCombo(JComboBox<T> combo) {
        combo.setRenderer(new RenderizadorLista());
        combo.setBackground(Color.WHITE);
        combo.setForeground(COR_TEXTO);
    }

    private void configurarResumo() {
        resumoSessao.setEditable(false);
        resumoSessao.setLineWrap(true);
        resumoSessao.setWrapStyleWord(true);
        resumoSessao.setBackground(Color.WHITE);
        resumoSessao.setForeground(COR_TEXTO);
        resumoSessao.setBorder(new EmptyBorder(4, 2, 4, 2));
        resumoSessao.getAccessibleContext().setAccessibleName("Resumo da sessão selecionada");
    }

    private JScrollPane rolagem(JComponent conteudo) {
        JScrollPane rolagem = new JScrollPane(conteudo);
        rolagem.setBorder(BorderFactory.createLineBorder(COR_BORDA));
        rolagem.getViewport().setBackground(Color.WHITE);
        if (conteudo instanceof JList) {
            conteudo.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    rolagem.setBorder(BorderFactory.createLineBorder(COR_FOCO, 2));
                }

                @Override
                public void focusLost(FocusEvent e) {
                    rolagem.setBorder(BorderFactory.createLineBorder(COR_BORDA));
                }
            });
        }
        return rolagem;
    }

    private JPanel cartao(String titulo, JComponent conteudo, JComponent acaoCabecalho) {
        JPanel cartao = new JPanel(new BorderLayout(0, 10));
        cartao.setBackground(Color.WHITE);
        cartao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA), new EmptyBorder(12, 14, 14, 14)));
        JLabel rotulo = new JLabel(titulo);
        rotulo.setFont(rotulo.getFont().deriveFont(Font.BOLD, 16f));
        rotulo.setForeground(COR_PRIMARIA_ESCURA);
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setOpaque(false);
        cabecalho.add(rotulo, BorderLayout.WEST);
        if (acaoCabecalho != null) cabecalho.add(acaoCabecalho, BorderLayout.EAST);
        cartao.add(cabecalho, BorderLayout.NORTH);
        cartao.add(conteudo, BorderLayout.CENTER);
        return cartao;
    }

    private JPanel painelBase() {
        JPanel painel = new JPanel(new BorderLayout(14, 14));
        painel.setBackground(COR_FUNDO);
        painel.setBorder(new EmptyBorder(16, 16, 16, 16));
        return painel;
    }

    private JPanel listaComAcoes(JList<?> lista, JButton... botoes) {
        JPanel conteudo = new JPanel(new BorderLayout(0, 10));
        conteudo.setOpaque(false);
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acoes.setOpaque(false);
        for (JButton b : botoes) acoes.add(b);
        conteudo.add(rolagem(lista), BorderLayout.CENTER);
        conteudo.add(acoes, BorderLayout.SOUTH);
        return conteudo;
    }

    // Formulários em duas colunas (rótulo + campo), iguais em todas as telas
    private JPanel formulario() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        return form;
    }

    private int proximaLinha(JPanel form) {
        Integer atual = (Integer) form.getClientProperty("linha");
        int linha = atual == null ? 0 : atual;
        form.putClientProperty("linha", linha + 1);
        return linha;
    }

    private void linha(JPanel form, String rotulo, JComponent campo) {
        int y = proximaLinha(form);
        JLabel texto = new JLabel(rotulo);
        texto.setForeground(COR_TEXTO_SUAVE);
        texto.setLabelFor(campo);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = y;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 0, 5, 12);
        form.add(texto, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 0, 5, 0);
        form.add(campo, c);
    }

    private void botaoLinha(JPanel form, JButton botao) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = proximaLinha(form);
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(14, 0, 0, 0);
        form.add(botao, c);
    }

    private void fecharFormulario(JPanel form) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = proximaLinha(form);
        c.gridwidth = 2;
        c.weighty = 1;
        JPanel espaco = new JPanel();
        espaco.setOpaque(false);
        form.add(espaco, c);
    }

    private void atalhosLista(JList<?> lista, Runnable editar, Runnable excluir) {
        InputMap entradas = lista.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap acoes = lista.getActionMap();
        if (editar != null) {
            entradas.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "editarItem");
            acoes.put("editarItem", new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    editar.run();
                }
            });
            lista.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && lista.getSelectedIndex() >= 0) editar.run();
                }
            });
        }
        if (excluir != null) {
            entradas.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "excluirItem");
            acoes.put("excluirItem", new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    excluir.run();
                }
            });
        }
    }

    // ---------------------------------------------------------------------
    // Abas
    // ---------------------------------------------------------------------
    private JPanel criarAbaJogadores() {
        JPanel painel = painelBase();
        JTextField nome = campo("Ex.: Marina Souza");
        JTextField personagem = campo("Ex.: Thalion, o Bardo");
        JPanel form = formulario();
        linha(form, "Nome do jogador", nome);
        linha(form, "Nome do personagem", personagem);

        JButton cadastrar = botao("Cadastrar jogador", Variante.PRIMARIO, KeyEvent.VK_C,
                "Cadastra o jogador", e -> executar(() -> {
                    exigirTexto(nome, "Nome do jogador");
                    exigirTexto(personagem, "Nome do personagem");
                    String n = nome.getText().trim();
                    String p = personagem.getText().trim();
                    emSegundoPlano(() -> gerenciador.cadastrarJogador(new Jogador(n, p)), () -> {
                        nome.setText("");
                        personagem.setText("");
                        nome.requestFocusInWindow();
                    }, "Jogador cadastrado com sucesso.");
                }));
        botoesPadrao[0] = cadastrar;
        botaoLinha(form, cadastrar);
        fecharFormulario(form);

        JButton editar = botao("Editar selecionado", Variante.SECUNDARIO, KeyEvent.VK_E,
                "Edita o jogador selecionado", e -> editarJogadorSelecionado());
        JButton excluir = botao("Excluir selecionado", Variante.PERIGO, KeyEvent.VK_X,
                "Exclui o jogador selecionado", e -> excluirJogadorSelecionado());
        atalhosLista(listaJogadores, this::editarJogadorSelecionado, this::excluirJogadorSelecionado);
        listaJogadores.setToolTipText("Enter ou duplo clique: editar. Delete: excluir.");

        painel.add(cartao("Novo jogador", form, null), BorderLayout.WEST);
        painel.add(cartao("Jogadores cadastrados", listaComAcoes(listaJogadores, editar, excluir), null),
                BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarAbaCampanhas() {
        JPanel painel = painelBase();
        JTextField nome = campo("Ex.: A Maldição de Strahd");
        JTextField sistema = campo("Ex.: D&D 5e, Tormenta20");
        JPanel form = formulario();
        linha(form, "Nome da campanha", nome);
        linha(form, "Sistema de RPG", sistema);

        JButton cadastrar = botao("Cadastrar campanha", Variante.PRIMARIO, KeyEvent.VK_C,
                "Cadastra a campanha", e -> executar(() -> {
                    exigirTexto(nome, "Nome da campanha");
                    exigirTexto(sistema, "Sistema de RPG");
                    String n = nome.getText().trim();
                    String s = sistema.getText().trim();
                    emSegundoPlano(() -> gerenciador.cadastrarCampanha(new Campanha(n, s)), () -> {
                        nome.setText("");
                        sistema.setText("");
                        nome.requestFocusInWindow();
                    }, "Campanha cadastrada com sucesso.");
                }));
        botoesPadrao[1] = cadastrar;
        botaoLinha(form, cadastrar);
        fecharFormulario(form);

        JButton editar = botao("Editar selecionada", Variante.SECUNDARIO, KeyEvent.VK_E,
                "Edita a campanha selecionada", e -> editarCampanhaSelecionada());
        JButton excluir = botao("Excluir selecionada", Variante.PERIGO, KeyEvent.VK_X,
                "Exclui a campanha selecionada", e -> excluirCampanhaSelecionada());
        atalhosLista(listaCampanhas, this::editarCampanhaSelecionada, this::excluirCampanhaSelecionada);
        listaCampanhas.setToolTipText("Enter ou duplo clique: editar. Delete: excluir.");

        painel.add(cartao("Nova campanha", form, null), BorderLayout.WEST);
        painel.add(cartao("Campanhas cadastradas", listaComAcoes(listaCampanhas, editar, excluir), null),
                BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarAbaSessoes() {
        JPanel painel = painelBase();

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

        JButton criar = botao("Criar sessão", Variante.PRIMARIO, KeyEvent.VK_C,
                "Cria a sessão com os dados informados", e -> executar(() -> {
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
                    emSegundoPlano(() -> gerenciador.adicionarSessao(campanha, sessao), () -> {
                        titulo.setText("");
                        data.setText("");
                        plataforma.setText("");
                        detalhe.setText("");
                        titulo.requestFocusInWindow();
                    }, "Sessão criada com sucesso.");
                }));
        botoesPadrao[2] = criar;
        botaoLinha(form, criar);
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
                "Inscreve o jogador escolhido na sessão selecionada", e -> executar(() -> {
                    Sessao s = sessaoSelecionada();
                    Jogador j = jogadorSelecionado();
                    emSegundoPlano(() -> gerenciador.inscreverJogador(s, j), null, "Jogador inscrito.");
                }));
        JButton presenca = botao("Registrar presença", Variante.SECUNDARIO, KeyEvent.VK_P,
                "Registra presença ou ausência do jogador escolhido", e -> executar(() -> {
                    Sessao s = sessaoSelecionada();
                    Jogador j = jogadorSelecionado();
                    if (!s.getJogadores().contains(j))
                        throw new ErroValidacao("O jogador selecionado não está inscrito nesta sessão.",
                                comboJogadores);
                    Object[] opcoes = {"Presente", "Ausente"};
                    int escolha = JOptionPane.showOptionDialog(this, "Selecione a presença:", "Presença",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[0]);
                    if (escolha < 0) return;
                    StatusPresenca status = escolha == 0 ? StatusPresenca.PRESENTE : StatusPresenca.AUSENTE;
                    emSegundoPlano(() -> gerenciador.registrarPresenca(s, j, status), null,
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
        painel.add(divisao, BorderLayout.CENTER);
        return painel;
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

        return seletorConvidados.selecionar(this, gerenciador.getJogadores())
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

    private void alterarSessao(Consumer<Sessao> acao, String sucesso, String confirmacao) {
        executar(() -> {
            Sessao sessao = sessaoSelecionada();
            if (confirmacao != null && !confirmar("Confirmar ação",
                    confirmacao + " \"" + sessao.getTitulo() + "\"?", confirmacao, "Voltar")) return;
            emSegundoPlano(() -> acao.accept(sessao), null, sucesso);
        });
    }

    // ---------------------------------------------------------------------
    // Edição e exclusão
    // ---------------------------------------------------------------------
    private void editarJogadorSelecionado() {
        Jogador jogador = listaJogadores.getSelectedValue();
        if (jogador == null) {
            aviso("Selecione um jogador na lista.");
            listaJogadores.requestFocusInWindow();
            return;
        }

        JTextField nome = campo(jogador.getNome(), null);
        JTextField personagem = campo(jogador.getNomePersonagem(), null);
        JPanel form = formulario();
        linha(form, "Nome do jogador", nome);
        linha(form, "Nome do personagem", personagem);
        if (!dialogoEdicao("Editar jogador", form, nome, () -> {
            exigirTexto(nome, "Nome do jogador");
            exigirTexto(personagem, "Nome do personagem");
        })) return;

        String novoNome = nome.getText().trim();
        String novoPersonagem = personagem.getText().trim();
        emSegundoPlano(() -> gerenciador.atualizarJogador(jogador, novoNome, novoPersonagem), null,
                "Jogador atualizado com sucesso.");
    }

    private void excluirJogadorSelecionado() {
        Jogador jogador = listaJogadores.getSelectedValue();
        if (jogador == null) {
            aviso("Selecione um jogador na lista.");
            listaJogadores.requestFocusInWindow();
            return;
        }
        if (!confirmarExclusao("Excluir o jogador " + jogador.getNome() + "?")) return;

        emSegundoPlano(() -> gerenciador.excluirJogador(jogador), null, "Jogador excluído com sucesso.");
    }

    private void editarCampanhaSelecionada() {
        Campanha campanha = listaCampanhas.getSelectedValue();
        if (campanha == null) {
            aviso("Selecione uma campanha na lista.");
            listaCampanhas.requestFocusInWindow();
            return;
        }

        JTextField nome = campo(campanha.getNome(), null);
        JTextField sistema = campo(campanha.getSistemaRpg(), null);
        JPanel form = formulario();
        linha(form, "Nome da campanha", nome);
        linha(form, "Sistema de RPG", sistema);
        if (!dialogoEdicao("Editar campanha", form, nome, () -> {
            exigirTexto(nome, "Nome da campanha");
            exigirTexto(sistema, "Sistema de RPG");
        })) return;

        String novoNome = nome.getText().trim();
        String novoSistema = sistema.getText().trim();
        emSegundoPlano(() -> gerenciador.atualizarCampanha(campanha, novoNome, novoSistema), null,
                "Campanha atualizada com sucesso.");
    }

    private void excluirCampanhaSelecionada() {
        Campanha campanha = listaCampanhas.getSelectedValue();
        if (campanha == null) {
            aviso("Selecione uma campanha na lista.");
            listaCampanhas.requestFocusInWindow();
            return;
        }
        if (!confirmarExclusao("Excluir a campanha " + campanha.getNome() + "?")) return;

        emSegundoPlano(() -> gerenciador.excluirCampanha(campanha), null, "Campanha excluída com sucesso.");
    }

    private void excluirSessaoSelecionada() {
        Sessao sessao = listaSessoes.getSelectedValue();
        if (sessao == null) {
            aviso("Selecione uma sessão na lista.");
            listaSessoes.requestFocusInWindow();
            return;
        }
        if (!confirmarExclusao("Excluir a sessão " + sessao.getTitulo() + "?")) return;

        emSegundoPlano(() -> gerenciador.excluirSessao(sessao), null, "Sessão excluída com sucesso.");
    }

    /** Reabre o diálogo enquanto os dados forem inválidos, sem o usuário perder o que digitou. */
    private boolean dialogoEdicao(String titulo, JPanel form, JComponent primeiroCampo, Runnable validacao) {
        form.setPreferredSize(new Dimension(Math.max(400, form.getPreferredSize().width),
                form.getPreferredSize().height));
        JComponent proximoFoco = primeiroCampo;
        while (true) {
            JComponent alvo = proximoFoco;
            SwingUtilities.invokeLater(() -> alvo.requestFocusInWindow());
            int resposta = JOptionPane.showConfirmDialog(this, form, titulo,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (resposta != JOptionPane.OK_OPTION) return false;
            try {
                validacao.run();
                return true;
            } catch (IllegalArgumentException ex) {
                if (ex instanceof ErroValidacao) {
                    proximoFoco = ((ErroValidacao) ex).campo;
                    marcarErro(proximoFoco);
                }
                aviso(ex.getMessage());
            }
        }
    }

    private boolean confirmarExclusao(String mensagem) {
        return confirmar("Confirmar exclusão", mensagem, "Excluir", "Cancelar");
    }

    private boolean confirmar(String titulo, String mensagem, String textoConfirmar, String textoVoltar) {
        Object[] opcoes = {textoConfirmar, textoVoltar};
        return JOptionPane.showOptionDialog(this, mensagem, titulo, JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE, null, opcoes, opcoes[1]) == 0;
    }

    // ---------------------------------------------------------------------
    // Atualização da tela
    // ---------------------------------------------------------------------
    private void atualizarDetalhesSessao() {
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

    private void atualizarListas() {
        atualizando = true;
        try {
            Jogador jogadorNaLista = listaJogadores.getSelectedValue();
            Jogador jogadorNoCombo = (Jogador) comboJogadores.getSelectedItem();
            Campanha campanhaNaLista = listaCampanhas.getSelectedValue();
            Campanha campanhaNoCombo = (Campanha) comboCampanhas.getSelectedItem();
            Sessao sessaoNaLista = listaSessoes.getSelectedValue();

            modeloJogadores.clear();
            comboJogadores.removeAllItems();
            for (Jogador jogador : gerenciador.getJogadores()) {
                modeloJogadores.addElement(jogador);
                comboJogadores.addItem(jogador);
            }
            modeloCampanhas.clear();
            comboCampanhas.removeAllItems();
            modeloSessoes.clear();
            for (Campanha campanha : gerenciador.getCampanhas()) {
                modeloCampanhas.addElement(campanha);
                comboCampanhas.addItem(campanha);
                for (Sessao sessao : campanha.getSessoes()) {
                    modeloSessoes.addElement(sessao);
                }
            }

            // Mantém a seleção do usuário quando o item ainda existe
            if (jogadorNaLista != null) listaJogadores.setSelectedValue(jogadorNaLista, true);
            if (jogadorNoCombo != null) comboJogadores.setSelectedItem(jogadorNoCombo);
            if (campanhaNaLista != null) listaCampanhas.setSelectedValue(campanhaNaLista, true);
            if (campanhaNoCombo != null) comboCampanhas.setSelectedItem(campanhaNoCombo);
            if (sessaoNaLista != null) listaSessoes.setSelectedValue(sessaoNaLista, true);
            if (listaSessoes.getSelectedValue() == null && !modeloSessoes.isEmpty())
                listaSessoes.setSelectedIndex(0);
        } finally {
            atualizando = false;
        }
        atualizarDetalhesSessao();
    }

    private void mensagem(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Gerenciador de RPG", JOptionPane.INFORMATION_MESSAGE);
    }

    private void aviso(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Atenção", JOptionPane.WARNING_MESSAGE);
    }
}