package rpg.apresentacao.interfacegrafica;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

import static rpg.apresentacao.interfacegrafica.Tema.COR_FOCO;
import static rpg.apresentacao.interfacegrafica.Tema.COR_PRIMARIA_ESCURA;

/**
 * Representa um dado de 20 lados (D20), desenhado utilizando Java2D.
 * O ícone pode ser redimensionado e personalizado com diferentes cores e números.
 */
public class IconeD20 implements Icon {

    /** Tamanho do ícone em pixels (largura e altura). */
    private final int tamanho;

    /** Número exibido no centro do dado. */
    private final String numero;

    /** Cor do corpo (contorno externo preenchido) do dado. */
    private final Color corpo;

    /** Cor da face central triangular. */
    private final Color faceCentral;

    /** Cor das linhas que dividem as faces. */
    private final Color linhas;

    /** Cor do número desenhado no centro. */
    private final Color corNumero;

    /**
     * Cria um D20 personalizando tamanho, número e cores.
     *
     * @param tamanho     tamanho do ícone em pixels
     * @param numero      número exibido no centro (pode ser {@code null})
     * @param corpo       cor do corpo do dado
     * @param faceCentral cor da face central
     * @param linhas      cor das linhas
     * @param corNumero   cor do número
     */
    public IconeD20(int tamanho, String numero, Color corpo, Color faceCentral, Color linhas, Color corNumero) {
        this.tamanho = tamanho;
        this.numero = numero;
        this.corpo = corpo;
        this.faceCentral = faceCentral;
        this.linhas = linhas;
        this.corNumero = corNumero;
    }

    /**
     * Cria uma versão dourada do D20 com contorno e número em índigo escuro.
     *
     * @param tamanho tamanho do ícone em pixels
     * @param numero  número exibido no centro
     * @return ícone dourado configurado
     */
    public static IconeD20 dourado(int tamanho, String numero) {
        return new IconeD20(tamanho, numero, COR_FOCO, new Color(0xF0B33C),
                COR_PRIMARIA_ESCURA, COR_PRIMARIA_ESCURA);
    }

    /**
     * Gera uma imagem do D20 dourado com o número 20, útil, por exemplo,
     * como ícone da janela principal.
     *
     * @param tamanho tamanho da imagem em pixels
     * @return imagem com suporte a transparência
     */
    public static Image imagemDourada(int tamanho) {
        BufferedImage imagem = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagem.createGraphics();
        dourado(tamanho, "20").paintIcon(null, g, 0, 0);
        g.dispose();
        return imagem;
    }

    /**
     * Cria um polígono a partir de um centro, um raio e uma lista de ângulos,
     * que determinam a posição de cada vértice.
     *
     * @param cx      coordenada horizontal do centro
     * @param cy      coordenada vertical do centro
     * @param raio    distância do centro até os vértices
     * @param angulos ângulos dos vértices, em graus
     * @return caminho fechado representando o polígono
     */
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

    /**
     * Desenha o dado na tela: contorno, face central, divisões entre as faces
     * e, quando o ícone é grande o bastante, o número central.
     *
     * @param c componente de referência (pode ser {@code null})
     * @param g contexto gráfico
     * @param x posição horizontal do ícone
     * @param y posição vertical do ícone
     */
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

        for (double angulo : new double[]{-90, 30, 150}) {
            double ax = cx + raioTriangulo * Math.cos(Math.toRadians(angulo));
            double ay = cy + raioTriangulo * Math.sin(Math.toRadians(angulo));

            for (double desvio : new double[]{-60, 0, 60}) {
                double bx = cx + raio * Math.cos(Math.toRadians(angulo + desvio));
                double by = cy + raio * Math.sin(Math.toRadians(angulo + desvio));

                g2.draw(new Line2D.Double(ax, ay, bx, by));
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

    /**
     * Informa ao Swing a largura do ícone.
     *
     * @return largura em pixels
     */
    @Override
    public int getIconWidth() {
        return tamanho;
    }

    /**
     * Informa ao Swing a altura do ícone.
     *
     * @return altura em pixels
     */
    @Override
    public int getIconHeight() {
        return tamanho;
    }
}
