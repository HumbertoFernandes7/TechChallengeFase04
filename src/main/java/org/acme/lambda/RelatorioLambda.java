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

    @Override
    public String handleRequest(Map<String, Object> event, Context context) {
        ScanResponse response = dynamoDb.scan(ScanRequest.builder().tableName("Feedback").build());

        // 1. Contagem por Urgência (Ex: 0-3 Crítico, 4-7 Médio, 8-10 Bom)
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

        // 3. Montagem do corpo do relatório conforme o PDF
        StringBuilder sb = new StringBuilder("=== RELATÓRIO DE FEEDBACKS ===\n\n");
        sb.append("RESUMO POR URGÊNCIA:\n").append(porUrgencia).append("\n\n");
        sb.append("RESUMO POR DIA:\n").append(porDia).append("\n\n");
        sb.append("DETALHES:\n");

        response.items().forEach(item -> {
            sb.append(String.format("- [%s] Urgência: %s | Descrição: %s\n",
                    item.get("dataEnvio").s(),
                    Integer.parseInt(item.get("nota").n()) <= 3 ? "ALTA" : "BAIXA",
                    item.get("descricao").s()));
        });

        System.out.println(sb.toString());
        return "Relatório gerado com sucesso";
    }
}