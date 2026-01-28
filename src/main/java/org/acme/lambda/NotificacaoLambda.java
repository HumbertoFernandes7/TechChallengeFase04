package org.acme.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.util.Map;

@ApplicationScoped
@Named("notificacao")
public class NotificacaoLambda implements RequestHandler<DynamodbEvent, String> {

    @Override
    public String handleRequest(DynamodbEvent dynamodbEvent, Context context) {
        for (DynamodbEvent.DynamodbStreamRecord record : dynamodbEvent.getRecords()) {
            if ("INSERT".equals(record.getEventName())){
                Map<String, AttributeValue> item = record.getDynamodb().getNewImage();

                int nota = Integer.parseInt(item.get("nota").getN());
                String descricao = item.get("descricao").getS();

                if (nota <=3) {
                    processarUrgencia(descricao, nota);
                }
            }
        }
        return "Notificação processada com sucesso!";
    }

    private void processarUrgencia(String descricao, int nota){
        System.out.println("ALERTA DE URGÊNCIA: Feedback Crítico Recebido!");
        System.out.println("Nota: " + nota + " | Descrição: " + descricao);
    }
}