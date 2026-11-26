package rpg.apresentacao.interfacegrafica;

import rpg.dominio.modelo.StatusPresenca;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static rpg.apresentacao.interfacegrafica.Tema.*;

/** Fábrica de componentes padronizados: botões, campos, listas, cartões e formulários. */
public final class Componentes {

    private static final String PROP_ERRO = "campoComErro";

    private Componentes() {
    }

    // ---------------------------------------------------------------------
    // Bordas
    // ---------------------------------------------------------------------
    public static Border borda(Color cor, int espessura, int vertical, int horizontal) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(cor, espessura),
                new EmptyBorder(vertical - espessura, horizontal - espessura,
                        vertical - espessura, horizontal - espessura));
    }

    // ---------------------------------------------------------------------
    // Campos de texto
    // ---------------------------------------------------------------------
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

    public static JTextField campo(String dica) {
        return campo("", dica);
    }

    public static JTextField campo(String texto, String dica) {
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
        campo.getDocument().addDocumentListener(new DocumentListener() {
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
        });
        return campo;
    }

    private static void atualizarBordaCampo(JComponent campo) {
        if (Boolean.TRUE.equals(campo.getClientProperty(PROP_ERRO))) {
            campo.setBorder(borda(COR_PERIGO, 2, 8, 10));
        } else if (campo.isFocusOwner()) {
            campo.setBorder(borda(COR_FOCO, 2, 8, 10));
        } else {
            campo.setBorder(borda(COR_BORDA, 1, 8, 10));
        }
        campo.repaint();
    }

    /** Destaca o campo em vermelho até o usuário voltar a digitar nele. */
    public static void marcarErro(JComponent campo) {
        if (campo instanceof JTextComponent) {
            campo.putClientProperty(PROP_ERRO, Boolean.TRUE);
            atualizarBordaCampo(campo);
        }
    }

    private static void limparErro(JComponent campo) {
        if (Boolean.TRUE.equals(campo.getClientProperty(PROP_ERRO))) {
            campo.putClientProperty(PROP_ERRO, Boolean.FALSE);
            atualizarBordaCampo(campo);
        }
    }

    public static void exigirTexto(JTextField campo, String rotulo) {
        if (campo.getText().trim().isEmpty())
            throw new ErroValidacao("Preencha o campo \"" + rotulo + "\".", campo);
    }

    // ---------------------------------------------------------------------
    // Botões
    // ---------------------------------------------------------------------
    public enum Variante {
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

    public static JButton botao(String texto, Variante variante, int mnemonico, String dica,
                                ActionListener acao) {
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

    // ---------------------------------------------------------------------
    // Listas e combos
    // ---------------------------------------------------------------------
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

    public static <T> void estilizarLista(JList<T> lista, String nomeAcessivel) {
        lista.setCellRenderer(new RenderizadorLista());
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setBackground(Color.WHITE);
        lista.getAccessibleContext().setAccessibleName(nomeAcessivel);
    }

    public static <T> void estilizarCombo(JComboBox<T> combo) {
        combo.setRenderer(new RenderizadorLista());
        combo.setBackground(Color.WHITE);
        combo.setForeground(COR_TEXTO);
    }

    public static JScrollPane rolagem(JComponent conteudo) {
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

    public static JPanel listaComAcoes(JList<?> lista, JButton... botoes) {
        JPanel conteudo = new JPanel(new BorderLayout(0, 10));
        conteudo.setOpaque(false);
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acoes.setOpaque(false);
        for (JButton b : botoes) acoes.add(b);
        conteudo.add(rolagem(lista), BorderLayout.CENTER);
        conteudo.add(acoes, BorderLayout.SOUTH);
        return conteudo;
    }

    /** Enter/duplo clique edita e Delete exclui o item selecionado. Passe null para ignorar uma ação. */
    public static void atalhosLista(JList<?> lista, Runnable editar, Runnable excluir) {
        InputMap entradas = lista.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap acoes = lista.getActionMap();
        if (editar != null) {
            entradas.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "editarItem");
            acoes.put("editarItem", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
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
                public void actionPerformed(ActionEvent e) {
                    excluir.run();
                }
            });
        }
    }

    // ---------------------------------------------------------------------
    // Cartões e formulários
    // ---------------------------------------------------------------------
    public static JPanel cartao(String titulo, JComponent conteudo, JComponent acaoCabecalho) {
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

    // Formulários em duas colunas (rótulo + campo), iguais em todas as telas
    public static JPanel formulario() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        return form;
    }

    private static int proximaLinha(JPanel form) {
        Integer atual = (Integer) form.getClientProperty("linha");
        int linha = atual == null ? 0 : atual;
        form.putClientProperty("linha", linha + 1);
        return linha;
    }

    public static void linha(JPanel form, String rotulo, JComponent campo) {
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

    public static void botaoLinha(JPanel form, JButton botao) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = proximaLinha(form);
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(14, 0, 0, 0);
        form.add(botao, c);
    }

    /** Empurra os campos para o topo quando o formulário é mais alto que o conteúdo. */
    public static void fecharFormulario(JPanel form) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = proximaLinha(form);
        c.gridwidth = 2;
        c.weighty = 1;
        form.add(Box.createVerticalGlue(), c);
    }
}
