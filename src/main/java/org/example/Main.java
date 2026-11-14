package org.example;


import rpg.modelo.Jogador;
import rpg.regra.EntradaLivre;
import rpg.regra.EntradaPorConvite;
import rpg.regra.RegraParticipacao;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        Jogador jogador1 = new Jogador("Lorenzo", "Aragorn");
        Jogador jogador2 = new Jogador("Miguel", "Gandalf");
        Jogador jogador3 = new Jogador("João", "Legolas");

        RegraParticipacao entradaLivre = new EntradaLivre();

        RegraParticipacao entradaPorConvite =
                new EntradaPorConvite(List.of(jogador1, jogador2));

        System.out.println("=== ENTRADA LIVRE ===");

        System.out.println("Lorenzo: "
                + entradaLivre.permiteEntrada(jogador1));

        System.out.println("João: "
                + entradaLivre.permiteEntrada(jogador3));

        System.out.println("\n=== ENTRADA POR CONVITE ===");

        System.out.println("Lorenzo: "
                + entradaPorConvite.permiteEntrada(jogador1));

        System.out.println("Miguel: "
                + entradaPorConvite.permiteEntrada(jogador2));

        System.out.println("João: "
                + entradaPorConvite.permiteEntrada(jogador3));
    }
}