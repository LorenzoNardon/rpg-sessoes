
package rpg.modelo;

public class Jogador {

    private final String nome;
    private final String nomePersonagem;

    public Jogador(String nome, String nomePersonagem) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do jogador é obrigatório.");
        }

        if (nomePersonagem == null || nomePersonagem.isBlank()) {
            throw new IllegalArgumentException("O nome do personagem é obrigatório.");
        }

        this.nome = nome;
        this.nomePersonagem = nomePersonagem;
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
}
