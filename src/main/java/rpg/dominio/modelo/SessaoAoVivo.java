package rpg.dominio.modelo;

import rpg.dominio.regra.RegraParticipacao;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class SessaoAoVivo extends Sessao {

    private final String plataforma;
    private final String linkSala;

    public SessaoAoVivo(String titulo, LocalDateTime dataHora,
                        int capacidadeMaxima, String plataforma,
                        String linkSala, RegraParticipacao regraParticipacao) {

        super(titulo, dataHora, capacidadeMaxima, regraParticipacao);
        validarDadosEspecificos(plataforma, linkSala);
        this.plataforma = plataforma;
        this.linkSala = linkSala;
    }

    private SessaoAoVivo(UUID id, String titulo, LocalDateTime dataHora,
                         int capacidadeMaxima, String plataforma, String linkSala,
                         RegraParticipacao regraParticipacao, StatusSessao status,
                         Map<Jogador, StatusPresenca> presencas) {
        super(id, titulo, dataHora, capacidadeMaxima, regraParticipacao, status, presencas, false);
        validarDadosEspecificos(plataforma, linkSala);
        this.plataforma = plataforma;
        this.linkSala = linkSala;
    }

    public static SessaoAoVivo reconstituir(UUID id, String titulo, LocalDateTime dataHora,
                                             int capacidadeMaxima, String plataforma, String linkSala,
                                             RegraParticipacao regraParticipacao, StatusSessao status,
                                             Map<Jogador, StatusPresenca> presencas) {
        return new SessaoAoVivo(id, titulo, dataHora, capacidadeMaxima, plataforma, linkSala,
                regraParticipacao, status, presencas);
    }

    private static void validarDadosEspecificos(String plataforma, String linkSala) {
        if (plataforma == null || plataforma.isBlank())
            throw new IllegalArgumentException("A plataforma é obrigatória.");
        if (linkSala == null || linkSala.isBlank())
            throw new IllegalArgumentException("O link da sala é obrigatório.");
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
