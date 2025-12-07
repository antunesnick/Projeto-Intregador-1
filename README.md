````markdown
# 🌡️ Sistema de Monitoramento IoT com Alertas via WhatsApp

Este projeto é uma solução completa de IoT (Internet of Things) para monitoramento de temperatura, corrente elétrica e acesso físico (portas). O sistema integra sensores de hardware, um gateway Wi-Fi, uma API Backend robusta e um sistema de notificações em tempo real via WhatsApp.

## 📋 Visão Geral do Projeto

O sistema opera em um fluxo de comunicação em três camadas:
1.  **Camada Física (Hardware):** Arduino Mega coleta dados de múltiplos sensores e os transmite via Serial para um ESP8266.
2.  **Camada de Gateway:** O ESP8266 processa os dados e envia requisições HTTP POST para a API.
3.  **Camada de Aplicação (Backend):** Uma API Java (Spring Boot) armazena os logs, gera relatórios e dispara alertas críticos via WhatsApp (usando Evolution API).

## 🚀 Tecnologias Utilizadas

### Backend & Software
* **Java 21** com **Spring Boot 3.5.7**
* **Spring Data JPA** (Persistência de dados)
* **PostgreSQL** (Banco de dados relacional)
* **Docker & Docker Compose** (Orquestração de containers)
* **Evolution API v2.2.2** (Gateway de WhatsApp)
* **Thymeleaf** (Dashboard Web Server-side)

### Hardware / Firmware
* **C++ (Arduino Framework)**
* **Arduino Mega 2560** (Controlador principal)
* **ESP8266 (NodeMCU)** (Gateway Wi-Fi)
* **Sensores:** DHT22/DHT11 (Temp/Umidade), SCT-013 (Corrente), HC-SR04 (Ultrassônico), Keypad 4x4.

---

## 🏗️ Arquitetura e Estrutura

### 1. Hardware (Arduino/ESP)
* **Monitoramento:** Leitura de 3 zonas de temperatura, corrente elétrica e status da porta.
* **Interface Local:** LCD I2C e Teclado Matricial 4x4 para interação local.
* **Comunicação:** O Arduino envia strings formatadas (`A:25.0,B:12.0...`) para o ESP8266 via Serial. O ESP faz o parse e envia JSON para o servidor.

### 2. API Rest (Java)
A API possui os seguintes endpoints principais:
* `POST /api/arduino/data`: Recebe logs de rotina e salva no banco.
* `POST /api/arduino/alert/*`: Dispara mensagens de WhatsApp para situações críticas (Temp > 25°C ou Corrente anormal).
* `GET /api/reports/summary`: Retorna médias de temperatura e contagem de acessos.
* `GET /painel`: Dashboard visual renderizado com Thymeleaf.

### 3. Infraestrutura (Docker)
O projeto utiliza containers para facilitar o deploy de serviços auxiliares:
* **PostgreSQL:** Porta 5433 (Mapeada externamente).
* **Redis:** Gerenciamento de sessão e cache para a Evolution API.
* **Evolution API:** Serviço responsável pela conexão com o WhatsApp.

---

## ⚙️ Como Executar

### Pré-requisitos
* Java JDK 21
* Maven
* Docker e Docker Compose
* Arduino IDE (para subir os códigos nas placas)

### Passo 1: Subir a Infraestrutura
Na raiz do projeto (onde está o `docker-compose.yml`), execute:
```bash
docker-compose up -d
````

Isso iniciará o Banco de Dados, o Redis e a Evolution API.

### Passo 2: Configurar a API Java

1.  Certifique-se de que o `application.properties` (ou `.yml`) do Spring Boot esteja apontando para o PostgreSQL na porta correta (padrão configurado no docker: `5433`).
2.  Execute a aplicação:

<!-- end list -->

```bash
mvn spring-boot:run
```

### Passo 3: Configurar o Hardware

1.  **Arduino Mega:** Carregue o arquivo `mega_main.ino` (código referente ao `1cod.txt`). Certifique-se de que as bibliotecas (DHT, EmonLib, Keypad, LiquidCrystal\_I2C) estão instaladas.
2.  **ESP8266:**
      * Abra o código do ESP (`esp_gateway.ino`).
      * Edite as variáveis `ssid`, `password` e `springServer` com o IP da sua máquina onde o Java está rodando.
      * Carregue o código.

### Passo 4: Autenticar o WhatsApp

1.  Acesse a interface da Evolution API (geralmente `http://localhost:8080` ou via Postman seguindo a documentação da Evolution).
2.  Crie uma instância e escaneie o QR Code para habilitar o envio de mensagens.

-----

## 📊 Estrutura do Banco de Dados

A entidade principal `SensorLog` armazena:

  * `temp1`, `temp2`, `temp3` (Temperaturas)
  * `current` (Corrente Elétrica)
  * `doorOpenCount` (Contador de aberturas de porta)
  * `isDoorCurrentlyOpen` (Status atual da porta)
  * `timestamp` (Hora do registro)

-----

## 🛡️ Segurança e Alertas

O sistema possui "Debounce" de alertas. O ESP8266 verifica localmente se os limites foram excedidos (Temp \> 25.0 ou Corrente Anormal) e envia um POST específico de alerta. O backend Java processa isso e usa o `WhatsappService` para notificar o usuário imediatamente, evitando spam (intervalo de 60s entre alertas).

-----

Desenvolvido como parte das atividades acadêmicas da disciplina Projeto Intregador 1 de Ciência da Computação.

```
