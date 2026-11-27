
package rpg.dominio.modelo;

import java.util.Objects;
import java.util.UUID;

/**
 * Representa um jogador participante de uma campanha de RPG.
 *
 * Cada jogador possui um identificador único, seu nome e o nome
 * do personagem que interpreta.
 */
public class Jogador {

    /** Identificador único do jogador. */
    private final UUID id;

    /** Nome do jogador. */
    private final String nome;

    /** Nome do personagem interpretado pelo jogador. */
    private final String nomePersonagem;

    /**
     * Cria um novo jogador, gerando automaticamente seu identificador.
     *
     * @param nome nome do jogador
     * @param nomePersonagem nome do personagem interpretado
     */
    public Jogador(String nome, String nomePersonagem) {
        this(UUID.randomUUID(), nome, nomePersonagem);
    }

    /**
     * Construtor privado que inicializa um jogador com os dados informados.
     *
     * @param id identificador único do jogador
     * @param nome nome do jogador
     * @param nomePersonagem nome do personagem interpretado
     * @throws IllegalArgumentException se algum dado obrigatório for inválido
     */
    private Jogador(UUID id, String nome, String nomePersonagem) {
        if (id == null) {
            throw new IllegalArgumentException("O identificador do jogador é obrigatório.");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do jogador é obrigatório.");
        }

        if (nomePersonagem == null || nomePersonagem.isBlank()) {
            throw new IllegalArgumentException("O nome do personagem é obrigatório.");
        }
        this.id = id;
        this.nome = nome;
        this.nomePersonagem = nomePersonagem;
    }

    /**
     * Reconstrói um jogador a partir de seu identificador e seus dados.
     *
     * @param id identificador do jogador
     * @param nome nome do jogador
     * @param nomePersonagem nome do personagem interpretado
     * @return jogador reconstruído
     */
    public static Jogador reconstituir(UUID id, String nome, String nomePersonagem) {
        return new Jogador(id, nome, nomePersonagem);
    }

    /**
     * Retorna o identificador único do jogador.
     *
     * @return identificador do jogador
     */
    public UUID getId() {
        return id;
    }

    /**
     * Retorna o nome do jogador.
     *
     * @return nome do jogador
     */
    public String getNome() {
        return nome;
    }

    /**
     * Retorna o nome do personagem interpretado pelo jogador.
     *
     * @return nome do personagem
     */
    public String getNomePersonagem() {
        return nomePersonagem;
    }

    /**
     * Gera um resumo textual relacionando o jogador ao seu personagem.
     *
     * @return resumo no formato "nome interpreta personagem"
     */
    public String gerarResumo() {
        return nome + " interpreta " + nomePersonagem;
    }

    /**
     * Retorna a representação textual do jogador.
     *
     * @return resumo do jogador e de seu personagem
     */
    @Override
    public String toString() {
        return gerarResumo();
    }

    /**
     * Verifica se dois jogadores possuem o mesmo identificador.
     *
     * @param objeto objeto que será comparado
     * @return true se representarem o mesmo jogador, false caso contrário
     */
    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof Jogador jogador)) return false;
        return id.equals(jogador.id);
    }

    /**
     * Retorna o código hash do jogador, calculado a partir do identificador.
     *
     * @return código hash do jogador
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
