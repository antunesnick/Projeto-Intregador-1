/**
 * ESP8266 Gateway - Serial para Spring Boot API
 * Função: Ler dados do Mega e enviar para o Java via HTTP POST.
 * NÃO serve mais página HTML.
 */

#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <WiFiClient.h>

// --- ⚠ CONFIGURAÇÕES DE REDE ---
const char* ssid = "S24 FE de Nickolas";
const char* password = "9748rafa";

// IP DO SEU PC (Onde o Spring Boot roda)
const char* springServer = "10.128.200.13"; 
const int springPort = 8081;

// --- Variáveis para os Dados do Mega ---
String strTemp1 = "0.0";      // Chave A
String strTemp2 = "0.0";      // Chave B
String strTemp3 = "0.0";      // Chave C
String strCount = "0";        // Chave D
String strDoorStatus = "0";   // Chave E (0 ou 1)
String strCorrente = "0.0";   // Chave F
String serialData = "";

// --- Controle de Envio para API (Spring Boot) ---
unsigned long lastRoutineSend = 0;
const long routineInterval = 20000; // Envia para o banco a cada 20 segundos

// --- Controle de Alertas ---
unsigned long lastAlertTime = 0;
const long alertDebounce = 60000; // 60s entre alertas (para não spamar o WhatsApp)

// --- Limites de Alerta ---
const float MAX_TEMP_LIMIT = 25.0;
const float MIN_CURRENT_LIMIT = 0.5;
const float MAX_CURRENT_LIMIT = 10.0;


// ====================================================================
// FUNÇÃO: ENVIAR POST PARA O SPRING BOOT
// ====================================================================
void sendPostRequest(String endpoint, String jsonPayload) {
  if (WiFi.status() == WL_CONNECTED) {
    WiFiClient client;
    HTTPClient http;

    // Timeouts curtos para garantir que o loop não trave
    client.setTimeout(3000); 
    http.setTimeout(3000);

    String url = "http://" + String(springServer) + ":" + String(springPort) + endpoint;

    http.begin(client, url);
    http.addHeader("Content-Type", "application/json");

    int httpResponseCode = http.POST(jsonPayload);

    if (httpResponseCode > 0) {
      Serial.print("API POST Sucesso (");
      Serial.print(endpoint);
      Serial.print("): ");
      Serial.println(httpResponseCode);
    } else {
      Serial.print("Erro no envio POST: ");
      Serial.println(httpResponseCode);
    }
    http.end();
  } else {
    Serial.println("Erro: WiFi desconectado. Tentando reconectar...");
    WiFi.reconnect();
  }
}

// ====================================================================
// FUNÇÃO: CHECAR ALERTAS (Regra de Negócio Imediata)
// ====================================================================
void checkAlerts(float temp, float current) {
  unsigned long now = millis();
  
  // Só verifica se passou o tempo de debounce
  if (now - lastAlertTime > alertDebounce) {
    
    // 1. Alerta Temperatura
    if (temp > MAX_TEMP_LIMIT) {
      String json = "{\"value\": " + String(temp) + ", \"message\": \"Temperatura acima do limite!\"}";
      sendPostRequest("/api/arduino/alert/temperature", json);
      lastAlertTime = now;
      Serial.println("ALERTA: Temperatura enviada!");
      return;
    }

    // 2. Alerta Corrente
    if (current > MAX_CURRENT_LIMIT || (current > 0 && current < MIN_CURRENT_LIMIT)) {
       String json = "{\"value\": " + String(current) + ", \"message\": \"Corrente anormal detectada!\"}";
       sendPostRequest("/api/arduino/alert/current", json);
       lastAlertTime = now;
       Serial.println("ALERTA: Corrente enviada!");
    }
  }
}

// ====================================================================
// HELPER: PARSE SERIAL (Lê "A:25,B:10...")
// ====================================================================
String parseValue(String data, String key) {
  int keyStart = data.indexOf(key);
  if (keyStart == -1) return "--";
  
  int valStart = keyStart + key.length();
  int valEnd = data.indexOf(",", valStart);
  if (valEnd == -1) valEnd = data.length();
  
  return data.substring(valStart, valEnd);
}

// ====================================================================
// PROCESSAR DADOS SERIAL (Vindos do Arduino Mega)
// ====================================================================
void processSerialData() {
  while (Serial.available()) {
    char c = Serial.read();
    serialData += c;

    if (c == '\n') { 
      serialData.trim();
      if (serialData.length() > 0 && serialData.indexOf("A:") > -1) {
        
        // A: Temp1, B: Temp2, C: Temp3, D: Count, E: Status, F: Corrente
        strTemp1 = parseValue(serialData, "A:");
        strTemp2 = parseValue(serialData, "B:");
        strTemp3 = parseValue(serialData, "C:");
        strCount = parseValue(serialData, "D:");
        strDoorStatus = parseValue(serialData, "E:");
        strCorrente = parseValue(serialData, "F:");

        // Checagem de Alertas (Exemplo: Se qualquer temp passar de 30)
        float t1 = strTemp1.toFloat();
        float curr = strCorrente.toFloat();
        checkAlerts(t1, curr); 
      }
      serialData = "";
    }
  }
}

// ====================================================================
// SETUP
// ====================================================================
void setup() {
  Serial.begin(115200); 
  Serial.println("\nESP8266 Gateway Iniciando...");
  
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  
  Serial.println("\nWiFi conectado.");
  Serial.print("Gateway IP: ");
  Serial.println(WiFi.localIP());
  Serial.print("Enviando dados para: ");
  Serial.println(springServer);
}

// ====================================================================
// LOOP PRINCIPAL
// ====================================================================
void loop() {
  // 1. Ler dados do Mega (O tempo todo)
  processSerialData();

  // 2. Enviar Rotina para o Spring Boot (A cada 20 segundos)
  unsigned long now = millis();
  if (now - lastRoutineSend > routineInterval) {
    
    // Só envia se os dados forem válidos (evita enviar lixo ou "--")
  if (strTemp1 != "--") {
        String jsonRoutine = "{";
        jsonRoutine += "\"temp1\": " + strTemp1 + ",";
        jsonRoutine += "\"temp2\": " + strTemp2 + ",";
        jsonRoutine += "\"temp3\": " + strTemp3 + ",";
        // Garante que doorOpenCount seja numérico (inteiro)
        if(strCount == "" || strCount == "--") {
          strCount = 0;
        } 
        else {
              jsonRoutine += "\"doorOpenCount\": " + strCount + ",";
        }

        // Lógica Correta para Boolean (sem aspas no true/false)
        // Verifica se o status recebido contém "1" (True) ou qualquer outra coisa (False)
        if (strDoorStatus.indexOf("1") >= 0) {
            jsonRoutine += "\"isDoorOpen\": true,";
        } else {
            jsonRoutine += "\"isDoorOpen\": false,";
        }
        // Corrente (Double)
        jsonRoutine += "\"current\": " + strCorrente;
        jsonRoutine += "}";
        Serial.print("Enviando JSON: "); Serial.println(jsonRoutine);
        sendPostRequest("/api/arduino/data", jsonRoutine);
      }
    lastRoutineSend = now;
  }
}

//código do esp8266