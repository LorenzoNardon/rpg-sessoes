
package rpg.dominio.modelo;

import java.util.Objects;
import java.util.UUID;

public class Jogador {

    private final UUID id;
    private final String nome;
    private final String nomePersonagem;

    public Jogador(String nome, String nomePersonagem) {
        this(UUID.randomUUID(), nome, nomePersonagem);
    }

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

    public static Jogador reconstituir(UUID id, String nome, String nomePersonagem) {
        return new Jogador(id, nome, nomePersonagem);
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getNomePersonagem() {
        return nomePersonagem;
    }

    public String gerarResumo() {
        return nome + " interpreta " + nomePersonagem;
    }

    @Override
    public String toString() {
        return gerarResumo();
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof Jogador jogador)) return false;
        return id.equals(jogador.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
