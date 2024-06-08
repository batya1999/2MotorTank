// Define motor control pins
const int motorA_in1 = 7;
const int motorA_in2 = 15;
const int motorB_in3 = 16;
const int motorB_in4 = 17;
const int enableA = 5;
const int enableB = 6;

void setup() {
  // Set all the motor control pins to outputs
  pinMode(motorA_in1, OUTPUT);
  pinMode(motorA_in2, OUTPUT);
  pinMode(motorB_in3, OUTPUT);
  pinMode(motorB_in4, OUTPUT);
  pinMode(enableA, OUTPUT);
  pinMode(enableB, OUTPUT);

  // Initialize motors to stop
  stop();
}

void loop() {
  // Example movements
  moveForward(255);  // Move forward at full speed
  delay(2000);       // Move for 2 seconds
  stop();            // Stop
  delay(1000);       // Wait for 1 second

  moveBackward(255); // Move backward at full speed
  delay(2000);       // Move for 2 seconds
  stop();            // Stop
  delay(1000);       // Wait for 1 second

  turnLeft(255);     // Turn left at full speed
  delay(1000);       // Turn for 1 second
  stop();            // Stop
  delay(1000);       // Wait for 1 second

  turnRight(255);    // Turn right at full speed
  delay(1000);       // Turn for 1 second
  stop();            // Stop
  delay(1000);       // Wait for 1 second
}

void moveForward(int speed) {
  digitalWrite(motorA_in1, HIGH);
  digitalWrite(motorA_in2, LOW);
  digitalWrite(motorB_in3, HIGH);
  digitalWrite(motorB_in4, LOW);
  analogWrite(enableA, speed);
  analogWrite(enableB, speed);
}

void moveBackward(int speed) {
  digitalWrite(motorA_in1, LOW);
  digitalWrite(motorA_in2, HIGH);
  digitalWrite(motorB_in3, LOW);
  digitalWrite(motorB_in4, HIGH);
  analogWrite(enableA, speed);
  analogWrite(enableB, speed);
}

void turnLeft(int speed) {
  digitalWrite(motorA_in1, LOW);
  digitalWrite(motorA_in2, HIGH);
  digitalWrite(motorB_in3, HIGH);
  digitalWrite(motorB_in4, LOW);
  analogWrite(enableA, speed);
  analogWrite(enableB, speed);
}

void turnRight(int speed) {
  digitalWrite(motorA_in1, HIGH);
  digitalWrite(motorA_in2, LOW);
  digitalWrite(motorB_in3, LOW);
  digitalWrite(motorB_in4, HIGH);
  analogWrite(enableA, speed);
  analogWrite(enableB, speed);
}

void stop() {
  digitalWrite(motorA_in1, LOW);
  digitalWrite(motorA_in2, LOW);
  digitalWrite(motorB_in3, LOW);
  digitalWrite(motorB_in4, LOW);
  analogWrite(enableA, 0);
  analogWrite(enableB, 0);
}
