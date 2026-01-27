package org.acme.lambda;

public class Feedback {
    private String descricao; // Requisito: string [cite: 35]
    private int nota;        // Requisito: int (0 a 10) [cite: 36]

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public int getNota() { return nota; }
    public void setNota(int nota) { this.nota = nota; }
}
