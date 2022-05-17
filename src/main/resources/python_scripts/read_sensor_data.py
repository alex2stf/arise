
import sys
import traceback

try:
    import Adafruit_DHT
    DHT_SENSOR = Adafruit_DHT.DHT22
    DHT_PIN = int(sys.argv[1])
    FORMAT="Senzor %2d and %2d"

    if len(sys.argv) > 2:
        FORMAT = sys.argv[2]

    humidity, temperature = Adafruit_DHT.read_retry(Adafruit_DHT.DHT22, DHT_PIN)
    print(FORMAT % (humidity, temperature))
except:
    print("ERR: " + traceback.format_exc()  )