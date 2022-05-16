import Adafruit_DHT
import sys

DHT_SENSOR = Adafruit_DHT.DHT22
DHT_PIN = int(sys.argv[1])

FORMAT="sesnzor %2d and %2d"

if len(sys.argv) > 2:
    FORMAT = sys.argv[2]


humidity, temperature = Adafruit_DHT.read_retry(Adafruit_DHT.DHT22, DHT_PIN)
print(FORMAT % (humidity, temperature))