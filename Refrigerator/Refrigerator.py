#!/usr/bin/python
import threading
import time
import cv2
import numpy as np
# S3 Upload
import boto3
import S3Key as s3
# Temp Sensor
import Adafruit_DHT
from Fan import Fan
# Sensors
import RPi.GPIO as GPIO
from gpiozero import Button, LED
# MQTT 통신
import paho.mqtt.client as mqtt

# host_id = '172.30.1.68' # local IP(=Android)
host_id = "team4-mqtt-lb-2494f2a6d28b9a68.elb.us-east-2.amazonaws.com"
port = 1883
# GPIO ----------------------
GPIO.setmode(GPIO.BCM)
# GPIO ----------------------

class Refrigerator:
    def __init__(self, id, led_pin1, led_pin2, reed_pin, temp_pin, fan_pin):
        # State =====================
        self.ID = id
        self.isClosed = 0
        self.temp = 0
        self.speed = 100
        self.func_position = 0
        self.tempAry = [[-1, -1], [4, 8], [15, 19], [9, 12], [20, 20]] #목표 온도 범위
        self.goalTempRange = [0, 0]
        # Cam ======================
        self.cap_left = cv2.VideoCapture(0)
        self.cap_right = cv2.VideoCapture(2)
        # pin -----------------------
        self.temp_pin = temp_pin
        GPIO.setup(fan_pin, GPIO.OUT)
        # sensor ====================
        self.led1 = LED(led_pin1)
        self.led2 = LED(led_pin2)
        self.reed = Button(reed_pin) 
        self.temp_sensor = Adafruit_DHT.DHT11
        self.fan = Fan(fan_pin)
        # MQTT --------------
        self.topic = f"refri/sensors/"
        self.client = mqtt.Client()
        self.client.on_connect = self.on_connect
        self.client.on_message = self.on_message
        # --------------------
        
        # start ====================
        self.start()
        
    def start(self):
        # MQTT ======================
        try:
            self.client.connect(host_id, port)
            self.client.loop_start()
        except Exception as err:
            print(f"ERR ! /{err}/")
        # MQTT ======================
            
        # Threads ====================
        reed = threading.Thread(target=self.handle_reed)
        temp = threading.Thread(target=self.handle_temp, args=(self.temp_sensor, self.temp_pin))
        fan = threading.Thread(target=self.handle_fan)
        # Threads ====================
        
        # Thread start ================
        reed.start()
        temp.start()
        fan.start()
        # Thread start ================
        
        # Thread Join ================
        reed.join()
        temp.join()
        fan.join()
        # Thread start ================
       
    # MQTT ========================
    def getClient(self):
        return self.client
        
    def on_connect(self, client, userdata, flags, rc):
        print("Connected with result code " + str(rc))
        if rc == 0:
            print("MQTT 연결 성공, [[refri/#]] . . ")
            client.subscribe("refri/#")
        else: print("연결 실패 : ", rc)
        
    def on_message(self, client, userdata, msg):
        value = str(msg.payload.decode())
        print("MQTT : ", msg.topic, value)
        
        if (value == "disconnected"): print("Disconnected --")
        else:
            router = msg.topic.split("/")
            
            if (router[-1] ==  "refriname" and value == self.ID):
                topic = self.topic + "init"
                msg = "{0}/{1:0.1f}℃/{2}".format(self.ID, self.getTemp(self.temp_sensor, self.temp_pin), self.func_position)
                
                self.client.publish(topic, msg)
            elif (router[-1] == "selectFunc"):
                valueAry = value.split("/")
                
                if valueAry[0] == self.ID:
                    self.goalTempRange =  self.tempAry[int(valueAry[1])]
                    self.func_position = int(valueAry[1])
    # MQTT ========================
                    
    def handle_upload_img(self, img):
        img_name = img.split("/")[-1]
        print("Img name : ", img_name)
        s3_client = boto3.client(
                                's3',
                                aws_access_key_id = s3.ACCESS_KEY_ID,
                                aws_secret_access_key = s3.ACCESS_SECRET_KEY
                                )
        response = s3_client.upload_file(
            img, s3.BUCKET_NAME, img_name
        )
        
    def handle_reed(self):
        cnt = 0
        while True:
            ret_left, img_left = self.cap_left.read()
            ret_right, img_right = self.cap_right.read()
            
            # ========== if door close ================
            if self.reed.is_pressed: 
                if self.isClosed == 0:                    
                    self.isClosed = 1
                    cnt += 1
                    print("Door is Close-")
                    
                    if (cnt > 1): #처음이 아닌 경우
                        # ============ img cvt Color (bgr2gray) ============
                        gray = []
                        if ret_left == True: 
                            gray_left = cv2.cvtColor(img_left, cv2.COLOR_BGR2GRAY)
                        if ret_right == True: 
                            gray_right = cv2.cvtColor(img_right, cv2.COLOR_BGR2GRAY)
                        gray = [gray_left, gray_right]
                        # ============ img cvt Color (bgr2gray) ============
                        result_img = np.hstack((gray[1], gray[0]))
                        img_file = f"./media/{self.ID}_{cnt}.jpg"
                        
                        cv2.imwrite(f"{img_file}", result_img)
                        self.handle_upload_img(img_file)
                        self.led1.off()
                        self.led2.off()
            # ========== if door close ================
            # ========== if door open ================
            else: 
                self.isClosed = 0
                print("Door is Open-")
                self.led1.on()
                self.led2.on()
            # ========== if door open ================
            
            time.sleep(0.5)
            
    def getTemp(self, sensor, temp_pin):
        _, temperature = Adafruit_DHT.read_retry(sensor, temp_pin)
        
        return temperature
            
    def handle_temp(self, sensor, temp_pin):
        topic = ""
        
        while True:
            temperature = self.getTemp(sensor, temp_pin)
            
            if temperature is not None:
                topic = self.topic + "temp"
                msg = "{0}/{1:0.1f}℃".format(self.ID, self.temp)
                self.temp = temperature
                
                print("Temp : ", topic, msg)
                self.client.publish(topic, msg)
                
            time.sleep(30)
            
    def setSpeed(self, speed):
        #모터 속도 제어 PWM
        self.fan_pwm.ChangeDutyCycle(speed)
            
    def handle_fan(self):
        self.fan.setSpeed(self.speed)
        
        while True:
            self.fan.run()
            
            if (self.temp < self.goalTempRange[0]): 
                if (self.speed >= 50): self.speed -= 10
            elif (self.temp > self.goalTempRange[1]):
                if (self.speed < 90): self.speed += 10
            self.fan.setSpeed(self.speed) 
            
            time.sleep(0.5)
            
# sensor Pins ===================
refri_ID = "yanfri1"
led_pin1 = 20
led_pin2 = 17
reed_pin = 21
temp_pin = 4
fan_pin = (23, 24, 16)
# sensor Pins ===================
if __name__ == "__main__":  
    refrigerator = Refrigerator(refri_ID, led_pin1, led_pin2, reed_pin, temp_pin, fan_pin)