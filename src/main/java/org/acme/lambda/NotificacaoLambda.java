package org.acme.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Map;

/**
 * Lambda que envia notificações SNS quando um feedback crítico é inserido no DynamoDB.
 * Um feedback é crítico quando sua nota é menor ou igual a 3.
 */
@ApplicationScoped
@Named("notificacao")
public class NotificacaoLambda implements RequestHandler<DynamodbEvent, String> {

    @Inject
    private SnsService snsService;

    @Override
    public String handleRequest(DynamodbEvent dynamodbEvent, Context context) {
        // Itera cada evento que chegou do DynamoDB Streams
        for (DynamodbEvent.DynamodbStreamRecord record : dynamodbEvent.getRecords()) {
            
            // Processa apenas inserções novas
            if ("INSERT".equals(record.getEventName())) {
                // Extrai os dados do novo item inserido
                Map<String, AttributeValue> feedback = record.getDynamodb().getNewImage();
                
                // Obtém os valores do feedback
                int nota = Integer.parseInt(feedback.get("nota").getN());
                String descricao = feedback.get("descricao").getS();
                String dataEnvio = feedback.get("dataEnvio").getS();
                
                // Se a nota for crítica (≤ 3), envia alerta
                if (nota <= 3) {
                    String mensagem = String.format(
                        "ALERTA: Feedback Crítico!\nNota: %d\nData: %s\nDescrição: %s",
                        nota,
                        dataEnvio,
                        descricao
                    );
                    snsService.enviarMensagem("Tech Challenge - Alerta Critico", mensagem);
                }
            }
        }
        
        return "Notificação processada com sucesso!";
    }
}