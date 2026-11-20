package rpg.modelo;

import rpg.regra.RegraParticipacao;

import java.time.LocalDateTime;

public class SessaoAoVivo extends Sessao {

    private final String plataforma;
    private final String linkSala;

    public SessaoAoVivo(String titulo, LocalDateTime dataHora,
                        int capacidadeMaxima, String plataforma,
                        String linkSala, RegraParticipacao regraParticipacao) {

        super(titulo, dataHora, capacidadeMaxima, regraParticipacao);

        if (plataforma == null || plataforma.isBlank()) {
            throw new IllegalArgumentException("A plataforma é obrigatória.");
        }

        if (linkSala == null || linkSala.isBlank()) {
            throw new IllegalArgumentException("O link da sala é obrigatório.");
        }

        this.plataforma = plataforma;
        this.linkSala = linkSala;
    }

    public String getPlataforma() {
        return plataforma;
    }

    public String getLinkSala() {
        return linkSala;
    }

    @Override
    public String gerarResumo() {
        return "Sessão ao vivo: " + getTitulo()
                + " | Plataforma: " + plataforma
                + " | Link: " + linkSala;
    }
    @Override
    public String toString() {
        return getTitulo() + " - " + getDataHora().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + " - " + getStatus();
    }
}