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

/** Dado de 20 lados (icosaedro visto de frente) desenhado em Java2D, em qualquer tamanho. */
public class IconeD20 implements Icon {

    private final int tamanho;
    private final String numero;
    private final Color corpo;
    private final Color faceCentral;
    private final Color linhas;
    private final Color corNumero;

    public IconeD20(int tamanho, String numero, Color corpo, Color faceCentral, Color linhas, Color corNumero) {
        this.tamanho = tamanho;
        this.numero = numero;
        this.corpo = corpo;
        this.faceCentral = faceCentral;
        this.linhas = linhas;
        this.corNumero = corNumero;
    }

    /** D20 dourado com contorno e número em índigo escuro. */
    public static IconeD20 dourado(int tamanho, String numero) {
        return new IconeD20(tamanho, numero, COR_FOCO, new Color(0xF0B33C),
                COR_PRIMARIA_ESCURA, COR_PRIMARIA_ESCURA);
    }

    /** Imagem do d20 dourado, útil para o ícone da janela. */
    public static Image imagemDourada(int tamanho) {
        BufferedImage imagem = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagem.createGraphics();
        dourado(tamanho, "20").paintIcon(null, g, 0, 0);
        g.dispose();
        return imagem;
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

    @Override
    public int getIconWidth() {
        return tamanho;
    }

    @Override
    public int getIconHeight() {
        return tamanho;
    }
}
