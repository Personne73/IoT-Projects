import socket
import paho.mqtt.client as mqtt
import json

HOST = 'localhost'
PORT = 1880

def start_gateway():
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server_socket:
        server_socket.bind((HOST, PORT))
        server_socket.listen(1)
        print("[INFO] Gateway is running and waiting for data...")

        while True:
            conn, addr = server_socket.accept()
            with conn:
                print(f"[INFO] Connected to {addr}")
                data = conn.recv(1024).decode()
                if data:
                    print(f"[INFO] Received data: {data}")
                    # Publier sur MQTT ici
                    publish_to_mqtt(data)

# def publish_to_mqtt(data):
#     # Exemple de publication via MQTT
#     client = mqtt.Client("GatewayClientPublisher")
#     client.connect("localhost", 1883)
#     client.publish("home/temperature", data)
#     client.disconnect()


def publish_to_mqtt(data):
    try:
        # Parser le payload JSON reçu
        payload = json.loads(data)
        room = payload.get("room")
        temperature = payload.get("temperature")

        if room and temperature is not None:
            # Construire le message pour le publier
            mqtt_message = json.dumps({
                "room": room,
                "temperature": temperature
            })

            # Utilisez le protocole MQTT 5
            client_publisher = mqtt.Client(client_id="GatewayClientPublisher", protocol=mqtt.MQTTv5)
            client_publisher.enable_logger()  # Activez les logs pour déboguer
            client_publisher.connect("localhost", 1883)

            topic = f"home/temperature/{room}"  # Exemple : home/temperature/kitchen
            result = client_publisher.publish(topic, mqtt_message, retain=True)

            # Vérifiez si la publication a réussi
            if result.rc == mqtt.MQTT_ERR_SUCCESS:
                print(f"[INFO] Published to MQTT - Topic: {topic}, Message: {mqtt_message}")
            else:
                print(f"[ERROR] MQTT publish failed with result code: {result.rc}")

            client_publisher.disconnect()
        else:
            print("[ERROR] Payload missing room or temperature data.")
    except json.JSONDecodeError:
        print("[ERROR] Invalid JSON received:", data)
    except Exception as e:
        print("[ERROR] Failed to publish to MQTT:", e)



if __name__ == "__main__":
    start_gateway()