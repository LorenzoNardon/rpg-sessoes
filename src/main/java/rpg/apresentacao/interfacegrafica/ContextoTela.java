package rpg.apresentacao.interfacegrafica;

import javax.swing.JFrame;

/**
 * O que as abas precisam da janela principal: o frame (para diálogos) e a execução
 * de operações com tratamento de erro e, quando demoradas, em segundo plano.
 */
interface ContextoTela {

    /**
     * Retorna a janela principal, usada como componente pai dos diálogos.
     *
     * @return janela principal
     */
    JFrame janela();

    /**
     * Executa na thread da interface; erros viram aviso ao usuário.
     *
     * @param operacao operação a ser executada
     */
    void executar(Runnable operacao);

    /**
     * Executa fora da thread da interface, sem congelar a janela, e atualiza as abas ao final.
     *
     * @param tarefa          tarefa executada em segundo plano
     * @param aoSucesso       ação executada na interface após o sucesso (pode ser {@code null})
     * @param mensagemSucesso mensagem exibida na barra de status ao concluir
     */
    void emSegundoPlano(Runnable tarefa, Runnable aoSucesso, String mensagemSucesso);
}
