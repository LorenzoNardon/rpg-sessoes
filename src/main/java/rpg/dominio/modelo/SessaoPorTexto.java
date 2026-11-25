package rpg.dominio.modelo;

import rpg.dominio.regra.RegraParticipacao;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class SessaoPorTexto extends Sessao {

    private final String plataforma;
    private final String canal;

    public SessaoPorTexto(String titulo, LocalDateTime dataHora,
                          int capacidadeMaxima, String plataforma,
                          String canal, RegraParticipacao regraParticipacao) {

        super(titulo, dataHora, capacidadeMaxima, regraParticipacao);
        validarDadosEspecificos(plataforma, canal);
        this.plataforma = plataforma;
        this.canal = canal;
    }

    private SessaoPorTexto(UUID id, String titulo, LocalDateTime dataHora,
                           int capacidadeMaxima, String plataforma, String canal,
                           RegraParticipacao regraParticipacao, StatusSessao status,
                           Map<Jogador, StatusPresenca> presencas) {
        super(id, titulo, dataHora, capacidadeMaxima, regraParticipacao, status, presencas, false);
        validarDadosEspecificos(plataforma, canal);
        this.plataforma = plataforma;
        this.canal = canal;
    }

    public static SessaoPorTexto reconstituir(UUID id, String titulo, LocalDateTime dataHora,
                                               int capacidadeMaxima, String plataforma, String canal,
                                               RegraParticipacao regraParticipacao, StatusSessao status,
                                               Map<Jogador, StatusPresenca> presencas) {
        return new SessaoPorTexto(id, titulo, dataHora, capacidadeMaxima, plataforma, canal,
                regraParticipacao, status, presencas);
    }

    private static void validarDadosEspecificos(String plataforma, String canal) {
        if (plataforma == null || plataforma.isBlank())
            throw new IllegalArgumentException("A plataforma é obrigatória.");
        if (canal == null || canal.isBlank())
            throw new IllegalArgumentException("O canal é obrigatório.");
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
    @Override
    public String toString() {
        return getTitulo() + " - " + getDataHora().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + " - " + getStatus();
    }
}
