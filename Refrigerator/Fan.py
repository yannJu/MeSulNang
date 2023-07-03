import RPi.GPIO as GPIO
import time

GPIO.setmode(GPIO.BCM)
class Fan:
    def __init__(self, pinNum):
        self.GPIO_RP = pinNum[0]
        self.GPIO_RN = pinNum[1]
        self.GPIO_EN = pinNum[2]

        GPIO.setup(self.GPIO_RP, GPIO.OUT)
        GPIO.setup(self.GPIO_RN, GPIO.OUT)
        GPIO.setup(self.GPIO_EN, GPIO.OUT)
        
        self.fan_pwm = GPIO.PWM(self.GPIO_EN, 20)
        self.fan_pwm.start(0)
        
    def __del__(self):
        GPIO.cleanup()
        
    def run(self):
        GPIO.output(self.GPIO_RP, True)
        GPIO.output(self.GPIO_RN, False)
      
    def shortBreak(self):
        GPIO.output(self.GPIO_RP, True)
        GPIO.output(self.GPIO_RN, True)
        
    def setSpeed(self, speed):
        self.fan_pwm.ChangeDutyCycle(speed)
        
if __name__ == "__main__":
    fan = Fan((20, 21, 16))
    
    while(True):
        print("Run")
        fan.run()
        time.sleep(1)
        
        # print("ChangeSpeed(UP)")
        # for i in range(0, 101, 5):
        #     fan.setSpeed(i)
        #     print("UP : ", i)
        #     time.sleep(0.5)
            
        # print("ChangeSpeed(Down)")
        # for i in range(100, -1, -5):
        #     fan.setSpeed(i)
        #     print("Down : ", i)
        #     time.sleep(0.5)
        
        print("40")
        fan.setSpeed(20)
        time.sleep(5)
        
        print("60")
        fan.setSpeed(60)
        time.sleep(5)
        
        print("100")
        fan.setSpeed(100)
        time.sleep(5)
        