// Button control
const int buttonPin = 13;     // the number of the pushbutton pin
bool blockPress = false; 
int buttonState = 0;

// LED control
int activeLed = 0;
const int baseLedPin = 30;

//LED colors: {active, correct, wrong}
const int statusRed[3] = {HIGH, LOW, HIGH};
const int statusGreen[3] = {HIGH, LOW, LOW};
const int statusBlue[3] = {HIGH, HIGH, LOW};


void setActiveLed(int led){
  activeLed = led;
}

void setCurrentLedActive(){ 
    digitalWrite(baseLedPin + activeLed * 3, statusRed[0]);
    digitalWrite(baseLedPin + activeLed * 3 + 1, statusGreen[0]);
    digitalWrite(baseLedPin + activeLed * 3 + 2, statusBlue[0]);
}

void setCurrentLedCorrect(){
    digitalWrite(baseLedPin + activeLed * 3, statusRed[1]);
    digitalWrite(baseLedPin + activeLed * 3 + 1, statusGreen[1]);
    digitalWrite(baseLedPin + activeLed * 3 + 2, statusBlue[1]);
}

void setCurrentLedWrong(){
    digitalWrite(baseLedPin + activeLed * 3, statusRed[2]);
    digitalWrite(baseLedPin + activeLed * 3 + 1, statusGreen[2]);
    digitalWrite(baseLedPin + activeLed * 3 + 2, statusBlue[2]);
}


void setup() {
  pinMode(buttonPin, INPUT);

  for(int i = baseLedPin; i < baseLedPin +15; i++) {
    pinMode(i, OUTPUT);
  } 

   Serial.begin(9600);  
}





void loop() {

  buttonState = digitalRead(buttonPin);

  if (Serial.available() > 0) {    
    byte incomingByte = 0;
    incomingByte = Serial.read(); // read the incoming byte:
    if (incomingByte != -1) { // -1 means no data is available
        switch(incomingByte){
          case 0:
            setActiveLed(0);
            break;
           case 1:
               setActiveLed(1);
            break;
           case 2:
             setActiveLed(2);
            break;
           case 3:
             setActiveLed(3);
            break;
            case 4:
             setActiveLed(4);
            break;
            case 6:
             setCurrentLedActive();
            break;
            case 7:
             setCurrentLedCorrect();
            break;
            case 8:
             setCurrentLedWrong();
            break;
        }
    }
  }

  if (buttonState == HIGH) {
    if(!blockPress){
      Serial.print(1);
      delay(300);
      blockPress = true;
    }
  } else {
    blockPress = false;
  }

  


}
