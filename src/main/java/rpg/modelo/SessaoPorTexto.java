package rpg.modelo;

import rpg.regra.RegraParticipacao;

import java.time.LocalDateTime;

public class SessaoPorTexto extends Sessao {

    private final String plataforma;
    private final String canal;

    public SessaoPorTexto(String titulo, LocalDateTime dataHora,
                          int capacidadeMaxima, String plataforma,
                          String canal, RegraParticipacao regraParticipacao) {

        super(titulo, dataHora, capacidadeMaxima, regraParticipacao);

        if (plataforma == null || plataforma.isBlank()) {
            throw new IllegalArgumentException("A plataforma é obrigatória.");
        }

        if (canal == null || canal.isBlank()) {
            throw new IllegalArgumentException("O canal é obrigatório.");
        }

        this.plataforma = plataforma;
        this.canal = canal;
    }

    public String getPlataforma() {
        return plataforma;
    }

    public String getCanal() {
        return canal;
    }

    @Override
    public String gerarResumo() {
        return "Sessão por texto: " + getTitulo()
                + " | Plataforma: " + plataforma
                + " | Canal: " + canal;
    }
}