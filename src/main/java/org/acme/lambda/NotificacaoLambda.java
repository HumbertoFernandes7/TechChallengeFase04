package org.acme.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.Date;
import java.util.Map;

@ApplicationScoped
@Named("notificacao")
public class NotificacaoLambda implements RequestHandler<DynamodbEvent, String> {

    @Inject
    SnsService snsService;

    @Override
    public String handleRequest(DynamodbEvent dynamodbEvent, Context context) {
        for (DynamodbEvent.DynamodbStreamRecord record : dynamodbEvent.getRecords()) {
            if ("INSERT".equals(record.getEventName())) {
                Map<String, AttributeValue> item = record.getDynamodb().getNewImage();

                int nota = Integer.parseInt(item.get("nota").getN());
                String descricao = item.get("descricao").getS();
                String dataEnvio = item.get("dataEnvio").getS();

                if (nota <= 3) {
                    String mensagem = String.format("ALERTA: Feedback Crítico!\nNota: %d\nData: %s\nDescrição: %s", nota, descricao, dataEnvio);
                    snsService.enviarMensagem("Tech Challenge - Alerta Critico", mensagem);
                }
            }
        }
        return "Notificação processada com sucesso!";
    }
}