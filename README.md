# Tech Challenge Fase 4 — Sistema de Feedback com Quarkus + AWS Lambda

Projeto serverless em **Java 21** com **Quarkus**, composto por três funções Lambda integradas para:

1. receber feedbacks via API,
2. notificar automaticamente feedbacks críticos,
3. gerar relatório semanal consolidado.

A infraestrutura é definida com **AWS SAM** (`template.yaml`) e utiliza **DynamoDB** e **SNS**.

---

## Arquitetura

### Fluxo principal

1. **API Gateway** recebe `POST /avaliacao`.
2. **FeedbackLambda** (`QUARKUS_LAMBDA_HANDLER=feedback`) valida o payload e salva no DynamoDB (`Feedback`).
3. A inserção na tabela dispara evento de **DynamoDB Stream**.
4. **NotificacaoLambda** (`QUARKUS_LAMBDA_HANDLER=notificacao`) verifica a nota e, se crítica (`<= 3`), publica alerta no SNS.
5. Semanalmente, via `cron`, a **RelatorioLambda** (`QUARKUS_LAMBDA_HANDLER=relatorio`) faz scan da tabela, calcula estatísticas e envia relatório por SNS.

### Componentes

- **Tabela DynamoDB:** `Feedback`
- **Tópico SNS:** `AlertaCriticoTopic`
- **Lambdas Quarkus:**
  - `feedback` → `org.acme.lambda.FeedbackLambda`
  - `notificacao` → `org.acme.lambda.NotificacaoLambda`
  - `relatorio` → `org.acme.lambda.RelatorioLambda`
- **Serviço compartilhado:** `org.acme.lambda.SnsService`

---

## Estrutura do projeto

```text
src/main/java/org/acme/lambda/
├── Feedback.java
├── FeedbackLambda.java
├── NotificacaoLambda.java
├── RelatorioLambda.java
└── SnsService.java

src/main/resources/
└── application.properties

template.yaml
pom.xml
```

---

## Pré-requisitos

- Java **21**
- Maven (ou usar `./mvnw`)
- AWS CLI configurado (para deploy)
- AWS SAM CLI (para `sam build/deploy`)

---

## Build do projeto

Gerar artefatos Quarkus/Lambda:

```bash
./mvnw clean package
```

Para empacotamento Lambda (ZIP em `target/function.zip`):

```bash
./mvnw package -DskipTests
```

> O `template.yaml` espera `CodeUri: target/function.zip`.

---

## Deploy com AWS SAM

### 1) Build SAM

```bash
sam build
```

### 2) Deploy guiado (primeira vez)

```bash
sam deploy --guided
```

### 3) Deploy usando configurações salvas

```bash
sam deploy
```

Após o deploy, a URL da API será exibida no output `FeedbackApi`.

---

## Endpoint de ingestão

### `POST /avaliacao`

Exemplo de payload:

```json
{
  "descricao": "Aplicativo travou ao finalizar pedido",
  "nota": 2
}
```

Resposta de sucesso (`201`):

```json
{"mensagem": "Feedback salvo com sucesso!"}
```

Resposta de erro (`500`):

```json
{"erro": "<detalhe do erro>"}
```

### Exemplo com cURL

```bash
curl -X POST "https://<api-id>.execute-api.<regiao>.amazonaws.com/Prod/avaliacao" \
  -H "Content-Type: application/json" \
  -d '{"descricao":"Muito lento no login","nota":3}'
```

---

## Regras de negócio implementadas

- `FeedbackLambda`
  - Persiste `id`, `descricao`, `nota`, `dataEnvio` na tabela `Feedback`.
- `NotificacaoLambda`
  - Processa apenas eventos `INSERT` do stream.
  - Envia alerta SNS para feedback com `nota <= 3`.
- `RelatorioLambda`
  - Executa semanalmente (segunda, 09:00 UTC).
  - Consolida:
    - quantidade por urgência (`CRÍTICO`, `MÉDIO`, `NORMAL`),
    - quantidade por dia,
    - média das avaliações,
    - listagem detalhada.
  - Envia relatório via SNS.

---

## Variáveis de ambiente relevantes

Definidas no `template.yaml`:

- `QUARKUS_LAMBDA_HANDLER`
  - Seleciona qual handler nomeado será executado (`feedback`, `notificacao`, `relatorio`).
- `TOPICO_ALERTA_ARN`
  - ARN do tópico SNS para alertas/relatórios.

---

## Observações

- O e-mail inscrito no tópico SNS precisa ser confirmado para receber notificações.
- A tabela DynamoDB está com throughput provisionado no template (5 RCUs / 5 WCUs).
- O cron está em UTC: `cron(0 9 ? * MON *)`.

---

## Tecnologias

- Quarkus 3
- AWS Lambda
- AWS API Gateway
- AWS DynamoDB + Streams
- AWS SNS
- AWS SAM
- Java 21

