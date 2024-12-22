# import json
# from paho.mqtt.client import Client

# data_store = []  # Stocke les données reçues du broker MQTT

# def on_connect(client, userdata, flags, rc):
#     print("[INFO] Connected to MQTT broker")
#     client.subscribe("home/temperature/#")  # S'abonner à tous les topics de température

# def on_message(client, userdata, msg):
#     print(f"[INFO] Message received on {msg.topic}: {msg.payload.decode()}")
#     try:
#         payload = json.loads(msg.payload.decode())
#         data_store.append(payload)
#         # Limiter la taille de la liste
#         if len(data_store) > 50:
#             data_store.pop(0)
#     except json.JSONDecodeError:
#         print("[ERROR] Failed to parse message payload")

# def start_mqtt_client():
#     client = Client()
#     client.on_connect = on_connect
#     client.on_message = on_message
#     client.connect("localhost", 1883)  # Adresse et port du broker MQTT
#     client.loop_start()
#     return client


import paho.mqtt.client as mqtt
import json

BROKER_ADDRESS = "localhost"
BROKER_PORT = 1883
TOPIC_BASE = "home/temperature/#"

subscribed_data = {}  # store subscribed data from the broker


def on_connect(client, userdata, flags, reasonCode, properties=None):
    if reasonCode == 0:
        print("[INFO] Connected to MQTT broker successfully!")
        client.subscribe(TOPIC_BASE)  # Subscribe to all temperature topics
        print(f"[INFO] Subscribed to: {TOPIC_BASE}")
    else:
        print(f"[ERROR] Failed to connect to broker with return code {reasonCode}")


def on_message(client, userdata, message):
    print(f"[INFO] Message received on topic {message.topic}")
    
    try:
        # Decode the message payload
        payload = json.loads(message.payload.decode())
        print(f"[INFO] Payload: {payload}")
        subscribed_data[message.topic] = payload
    except json.JSONDecodeError:
        print("[ERROR] Invalid JSON format in message payload, failed to parse message payload")
    except Exception as e:
        print(f"[ERROR] Failed to process message: {e}")


def start_mqtt_client():
    client = mqtt.Client(client_id="BackendClientSubscriber", protocol=mqtt.MQTTv5)
    client.on_connect = on_connect
    client.on_message = on_message

    try:
        client.connect(BROKER_ADDRESS, BROKER_PORT)
        client.loop_start()
        print("[INFO] MQTT client started successfully")
    except Exception as e:
        print(f"[ERROR] Failed to start MQTT client: {e}")
        client.loop_stop()

    return client


if __name__ == "__main__":
    mqtt_client_subscriber = start_mqtt_client()

    try:
        print("[INFO] MQTT client is running and waiting for data...")
        while True:
            print("[INFO] Listening for messages...")
            print("[INFO] Current subscribed data:", subscribed_data)
    except KeyboardInterrupt:
        print("[INFO] Stopping MQTT client...")
        mqtt_client_subscriber.loop_stop()
        mqtt_client_subscriber.disconnect()
        print("[INFO] MQTT client stopped")
        print("[INFO] Exiting...")