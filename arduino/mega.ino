#include "DHT.h"
#include <Wire.h>
#include <Keypad.h>
#include <LiquidCrystal_I2C.h>
#include "EmonLib.h"

// --- Configurações ---
#define DHTPIN1 50      
#define DHTPIN2 51
#define DHTPIN3 52
#define trigPin 48
#define echoPin 49
#define pinLedRed 46
#define pinLedGreen 47
#define pinBuzz 45

#define DHTTYPE DHT22   
#define DHTTYPE2 DHT11
#define pinSCT A0

const int DISTANCIA_PORTA_ABERTA = 15; //
const float MAX_TEMP_LIMIT = 25.5;
int doorCount = 0;
bool isDoorOpen = false;
bool lastDoorState = false;

DHT dht1(DHTPIN1, DHTTYPE);
DHT dht2(DHTPIN2, DHTTYPE2);
DHT dht3(DHTPIN3, DHTTYPE2);

LiquidCrystal_I2C lcd(0x27, 16, 2);
EnergyMonitor emon1;

const byte Linhas = 4; const byte Colunas = 4;
char keyMap[Linhas][Colunas] = {
  {'1','2','3','A'}, {'4','5','6','B'}, {'7','8','9','C'}, {'*','0','#','D'}
};
byte pinlinhas[Linhas] = {2,3,4,5};
byte pincolunas[Colunas] = {6,7,8,9};
Keypad teclado = Keypad(makeKeymap(keyMap), pinlinhas, pincolunas, Linhas, Colunas);

char digito = 'A';
unsigned long previousMillis = 0;
const long interval = 2000; 

float t1 = 0, t2 = 0, t3 = 0;
float distancia = 0;

void setup() {
  lcd.init(); lcd.backlight();
  Serial.begin(115200);
  Serial1.begin(115200); // Comunicação ESP
  pinMode(trigPin, OUTPUT); pinMode(echoPin, INPUT);
  pinMode(pinLedRed, OUTPUT); pinMode(pinLedGreen, OUTPUT);
  pinMode(pinBuzz, OUTPUT);
  emon1.current(pinSCT, 20);
  dht1.begin(); dht2.begin(); dht3.begin();
}


// --- CORREÇÃO CRÍTICA: ENVIA SEMPRE ---
void mandarProEsp8266(float tempA, float tempB, float tempC, int doorCountR, boolean isDoorOpenR, double corrente) {
  // Se for NaN (erro), troca por 0.0 para não travar o site
  if (isnan(tempA)) tempA = 0.0;
  if (isnan(tempB)) tempB = 0.0;
  if (isnan(tempC)) tempC = 0.0;
  if (isnan(doorCountR)) doorCountR = -1;

  // Formato estrito: "A:25.0,B:12.0,C:10.0"
  String dataString = "A:" + String(tempA, 1) + 
                      ",B:" + String(tempB, 1) + 
                      ",C:" + String(tempC, 1) +
                      ",D:" + String(doorCountR, 1) +
                      ",E:" + String(isDoorOpenR, 1) +
                      ",F:" + String(corrente, 1);
                      
  Serial1.println(dataString);
  Serial.println("Enviado: " + dataString);
}

void atualizarLCD() {
  lcd.clear();
  lcd.setCursor(0,0);
  if (digito == 'A') { lcd.print("Sensor 1:"); lcd.setCursor(6,1); lcd.print(t1); }
  else if (digito == 'B') { lcd.print("Sensor 2:"); lcd.setCursor(6,1); lcd.print(t2); }
  else if (digito == 'C') { lcd.print("Sensor 3:"); lcd.setCursor(6,1); lcd.print(t3); }
  else { lcd.print("Distancia:"); lcd.setCursor(6,1); lcd.print(distancia); }
}

void verfificarTemperatura(float tempA, float tempB, float tempC)
{
  if(tempA > MAX_TEMP_LIMIT || tempB > MAX_TEMP_LIMIT || tempC > MAX_TEMP_LIMIT)
  {
    digitalWrite(pinLedGreen, LOW);
    digitalWrite(pinLedRed, HIGH);
    digitalWrite(pinBuzz, HIGH);
    tone(pinBuzz, 261, 100);
    delay(100);
    digitalWrite(pinLedRed, LOW);
    digitalWrite(pinBuzz, LOW);
  }
  else
  {
    digitalWrite(pinLedGreen, HIGH);
  }
}

void loop() {
  
  char key = teclado.getKey();
  double corrente = emon1.calcIrms(1480);

  if (distancia > DISTANCIA_PORTA_ABERTA) {
    isDoorOpen = true;
  } else {
    isDoorOpen = false;
  }

  if (isDoorOpen == true && lastDoorState == false) {
    doorCount++;
  }
  lastDoorState = isDoorOpen;

  if (key) { digito = key; atualizarLCD(); }

  unsigned long currentMillis = millis();
  if (currentMillis - previousMillis >= interval) {
    previousMillis = currentMillis;
    
    t1 = dht1.readTemperature();
    t2 = dht2.readTemperature();
    t3 = dht3.readTemperature();
    verfificarTemperatura(t1, t2, t3);
    
    digitalWrite(trigPin, LOW); delayMicroseconds(2);
    digitalWrite(trigPin, HIGH); delayMicroseconds(10); digitalWrite(trigPin, LOW);
    long duracao = pulseIn(echoPin, HIGH, 30000);
    distancia = (duracao * 0.0343) / 2;
    
    atualizarLCD();
    mandarProEsp8266(t1, t2, t3, doorCount, isDoorOpen, corrente);
  }
}


// código do Mega2560