
package rpg.dominio.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Campanha {

    private final UUID id;
    private final String nome;
    private final String sistemaRpg;
    private final List<Sessao> sessoes;

    public Campanha(String nome, String sistemaRpg) {
        this(UUID.randomUUID(), nome, sistemaRpg);
    }

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

    public static Campanha reconstituir(UUID id, String nome, String sistemaRpg) {
        return new Campanha(id, nome, sistemaRpg);
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getSistemaRpg() {
        return sistemaRpg;
    }

    public List<Sessao> getSessoes() {
        return Collections.unmodifiableList(sessoes);
    }

    public void adicionarSessao(Sessao sessao) {
        if (sessao == null) {
            throw new IllegalArgumentException("A sessão não pode ser nula.");
        }

        if (sessoes.contains(sessao)) {
            throw new IllegalArgumentException("Essa sessão já está cadastrada na campanha.");
        }

        sessoes.add(sessao);
    }

    public int quantidadeSessoes() {
        return sessoes.size();
    }

    @Override
    public String toString() {
        return nome + " (" + sistemaRpg + ")";
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof Campanha campanha)) return false;
        return id.equals(campanha.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
