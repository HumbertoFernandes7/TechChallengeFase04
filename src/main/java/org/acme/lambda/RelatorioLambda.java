package org.acme.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import java.util.Map;
import java.util.stream.Collectors;

@Named("relatorio")
public class RelatorioLambda implements RequestHandler<Map<String, Object>, String> {

    @Inject
    DynamoDbClient dynamoDb;

    @Inject
    SnsService snsService;

    @Override
    public String handleRequest(Map<String, Object> event, Context context) {
        ScanResponse response = dynamoDb.scan(ScanRequest.builder().tableName("Feedback").build());

        // 1. Contagem por Urgência
        Map<String, Long> porUrgencia = response.items().stream()
                .collect(Collectors.groupingBy(item -> {
                    int nota = Integer.parseInt(item.get("nota").n());
                    if (nota <= 3) return "CRÍTICO";
                    if (nota <= 7) return "MÉDIO";
                    return "NORMAL";
                }, Collectors.counting()));

        // 2. Contagem por Dia (Extraindo a data do campo dataEnvio)
        Map<String, Long> porDia = response.items().stream()
                .collect(Collectors.groupingBy(item ->
                        item.get("dataEnvio").s().substring(0, 10), Collectors.counting()));

        double mediaAvaliacoes = response.items().stream()
                .mapToInt(item -> Integer.parseInt(item.get("nota").n()))
                .average()
                .orElse(0.0);

        // 3. Gerar o relatório em formato de texto
        StringBuilder relatorio = new StringBuilder("=== RELATÓRIO SEMANAL DE SATISFAÇÃO ===\n\n");
        relatorio.append("📊 RESUMO ESTATÍSTICO:\n");
        relatorio.append("Quantidade por Urgência: ").append(porUrgencia).append("\n");
        relatorio.append("Quantidade por Dia: ").append(porDia).append("\n\n");
        relatorio.append("Media avaliações: ").append(mediaAvaliacoes).append("\n\n");

        relatorio.append("📋 LISTAGEM DETALHADA:\n");
        response.items().forEach(item -> {
            String urgencia = Integer.parseInt(item.get("nota").n()) <= 3 ? "ALTA" : "NORMAL";
            relatorio.append(String.format("- [%s] Urgência: %s | Descrição: %s\n",
                    item.get("dataEnvio").s(),
                    urgencia,
                    item.get("descricao").s()));
        });

        snsService.enviarMensagem("Tech Challenge - Relatório Semanal de Feedbacks", relatorio.toString());
        return "Relatório gerado com sucesso";
    }
}