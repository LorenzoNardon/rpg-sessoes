
package rpg.dominio.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa uma campanha de RPG, contendo suas informações principais
 * e as sessões associadas a ela.
 *
 * Cada campanha possui um identificador único, um nome e o sistema de RPG
 * utilizado. Também permite adicionar e consultar suas sessões.
 */
public class Campanha {

    /** Identificador único da campanha. */
    private final UUID id;

    /** Nome da campanha. */
    private final String nome;

    /** Sistema de RPG utilizado na campanha. */
    private final String sistemaRpg;

    /** Lista de sessões pertencentes à campanha. */
    private final List<Sessao> sessoes;

    /**
     * Cria uma nova campanha, gerando automaticamente seu identificador.
     *
     * @param nome nome da campanha
     * @param sistemaRpg sistema de RPG utilizado
     */
    public Campanha(String nome, String sistemaRpg) {
        this(UUID.randomUUID(), nome, sistemaRpg);
    }

    /**
     * Construtor privado que inicializa uma campanha com os dados informados.
     *
     * @param id identificador único da campanha
     * @param nome nome da campanha
     * @param sistemaRpg sistema de RPG utilizado
     * @throws IllegalArgumentException se algum dado obrigatório for inválido
     */
    private Campanha(UUID id, String nome, String sistemaRpg) {
        if (id == null) {
            throw new IllegalArgumentException("O identificador da campanha é obrigatório.");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da campanha é obrigatório.");
        }

        if (sistemaRpg == null || sistemaRpg.isBlank()) {
            throw new IllegalArgumentException("O sistema de RPG é obrigatório.");
        }
        this.id = id;
        this.nome = nome;
        this.sistemaRpg = sistemaRpg;
        this.sessoes = new ArrayList<>();
    }

    /**
     * Reconstrói uma campanha a partir de um identificador e seus dados.
     *
     * @param id identificador da campanha
     * @param nome nome da campanha
     * @param sistemaRpg sistema de RPG utilizado
     * @return campanha reconstruída
     */
    public static Campanha reconstituir(UUID id, String nome, String sistemaRpg) {
        return new Campanha(id, nome, sistemaRpg);
    }

    /**
     * Retorna o identificador único da campanha.
     *
     * @return identificador da campanha
     */
    public UUID getId() {
        return id;
    }

    /**
     * Retorna o nome da campanha.
     *
     * @return nome da campanha
     */
    public String getNome() {
        return nome;
    }

    /**
     * Retorna o sistema de RPG utilizado na campanha.
     *
     * @return sistema de RPG
     */
    public String getSistemaRpg() {
        return sistemaRpg;
    }

    /**
     * Retorna a lista de sessões da campanha em modo somente leitura.
     *
     * @return lista não modificável de sessões
     */
    public List<Sessao> getSessoes() {
        return Collections.unmodifiableList(sessoes);
    }

    /**
     * Adiciona uma sessão à campanha.
     *
     * @param sessao sessão que será adicionada
     * @throws IllegalArgumentException se a sessão for nula ou já estiver cadastrada
     */
    public void adicionarSessao(Sessao sessao) {
        if (sessao == null) {
            throw new IllegalArgumentException("A sessão não pode ser nula.");
        }

        if (sessoes.contains(sessao)) {
            throw new IllegalArgumentException("Essa sessão já está cadastrada na campanha.");
        }

        sessoes.add(sessao);
    }

    /**
     * Retorna a quantidade de sessões cadastradas na campanha.
     *
     * @return quantidade de sessões
     */
    public int quantidadeSessoes() {
        return sessoes.size();
    }

    /**
     * Retorna uma representação textual da campanha.
     *
     * @return nome da campanha seguido do sistema de RPG entre parênteses
     */
    @Override
    public String toString() {
        return nome + " (" + sistemaRpg + ")";
    }

    /**
     * Verifica se duas campanhas possuem o mesmo identificador.
     *
     * @param objeto objeto que será comparado
     * @return true se os objetos forem a mesma campanha, false caso contrário
     */
    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof Campanha campanha)) return false;
        return id.equals(campanha.id);
    }

    /**
     * Retorna o código hash da campanha, calculado a partir do identificador.
     *
     * @return código hash da campanha
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
