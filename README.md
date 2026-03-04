# Document Processor API

Projeto desenvolvido para estudo e portfólio com foco em:

* Java 21 + Spring Boot
* JPA / Hibernate
* PostgreSQL
* AWS SQS
* AWS S3
* Terraform
* LocalStack
* Ambiente híbrido: **WSL (infraestrutura)** + **Windows (aplicação)**

---

## Arquitetura

Fluxo da aplicação:

1. `POST /documents`
2. Documento salvo no PostgreSQL
3. Evento publicado na fila SQS
4. Consumer realiza polling da fila
5. Status do documento evolui:

   * RECEIVED → PROCESSING → DONE
6. Resultado salvo no S3

```
API → PostgreSQL → SQS → Worker → S3
```

---

## Estrutura do Ambiente

### Windows

* Aplicação Spring Boot
* PostgreSQL rodando via Docker (WSL)

### WSL

* Docker
* LocalStack
* AWS CLI
* Terraform

---

## Configurações de Ambiente

### 1️⃣ Subir PostgreSQL (caso não esteja rodando)

Dentro do WSL:

```bash
docker start postgres
docker ps
```

---

### 2️⃣ Subir LocalStack

Dentro do WSL:

```bash
docker run -d \
  --name localstack \
  -p 4566:4566 \
  -e LOCALSTACK_AUTH_TOKEN=SEU_TOKEN_AQUI \
  localstack/localstack
```

Verifique se está rodando:

```bash
docker ps
```

---

### 3️⃣ Instalar AWS CLI (WSL)

Documentação oficial:
https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html

Comandos utilizados:

```bash
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
aws --version
```

---

### 4️⃣ Testar conexão com LocalStack

```bash
aws --endpoint-url=http://localhost:4566 sqs list-queues
aws --endpoint-url=http://localhost:4566 s3 ls
```

---

## Infraestrutura com Terraform (WSL)

### Instalação do Terraform

```bash
sudo apt update
sudo apt install terraform
terraform -version
```

Criar diretório:

```bash
mkdir document-processor-infra
cd document-processor-infra
touch main.tf
```

---

### main.tf

```hcl
provider "aws" {
  access_key                  = "test"
  secret_key                  = "test"
  region                      = "us-east-1"

  s3_use_path_style           = true

  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true

  endpoints {
    sqs = "http://localhost:4566"
    s3  = "http://localhost:4566"
    sts = "http://localhost:4566"
  }
}
```

Aplicar:

```bash
terraform init
terraform apply
```

Confirmar:

```bash
aws --endpoint-url=http://localhost:4566 sqs list-queues
aws --endpoint-url=http://localhost:4566 s3 ls
```

---

## Aplicação Spring Boot (Windows)

### application.properties

```properties
spring.application.name=file-processing
server.port=8080

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

spring.datasource.url=jdbc:postgresql://localhost:5432/seu_banco
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
```

---

## Testando o Fluxo

### Criar documento

```
POST http://localhost:8080/documents
```

Body:

```json
{
  "fileName": "teste.pdf"
}
```

---

### Verificar fila

No WSL:

```bash
aws --endpoint-url=http://localhost:4566 sqs receive-message \
--queue-url http://localhost:4566/000000000000/document-queue
```

---

### Verificar S3

```bash
aws --endpoint-url=http://localhost:4566 s3 ls s3://document-results
```

---

## Documentações Utilizadas

AWS CLI:
https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html

LocalStack:
https://docs.localstack.cloud/aws/integrations/aws-native-tools/aws-cli/

---

Obrigada!