import socket
import paho.mqtt.client as mqtt
import json

HOST = 'localhost'
PORT = 1880


def start_gateway(mqtt_client_publisher):
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
                    publish_to_mqtt(mqtt_client_publisher, data)


def create_mqtt_client():
    client = mqtt.Client(client_id="GatewayClientPublisher", protocol=mqtt.MQTTv5)
    client.enable_logger()
    client.connect("localhost", 1883)
    client.loop_start()
    return client


def publish_to_mqtt(mqtt_client_publisher, data):
    try:
        payload = json.loads(data)
        room = payload.get("room")
        temperature = payload.get("temperature")

        if room and temperature is not None:
            topic = f"home/temperature/{room}"
            message = json.dumps({"room": room, "temperature": temperature})
            result = mqtt_client_publisher.publish(topic, message, qos=1, retain=True)

            if result.rc == mqtt.MQTT_ERR_SUCCESS:
                print(f"[INFO] Published to MQTT - Topic: {topic}, Message: {message}")
            else:
                print(f"[ERROR] MQTT publish failed with result code: {result.rc}")
        else:
            print("[ERROR] Payload missing room or temperature data.")
    except json.JSONDecodeError:
        print("[ERROR] Invalid JSON received:", data)
    except Exception as e:
        print(f"[ERROR] Failed to publish to MQTT:", e)


if __name__ == "__main__":
    mqtt_client_publisher = create_mqtt_client()
    start_gateway(mqtt_client_publisher)

# import paho.mqtt.client as mqtt

# def on_publish(client, userdata, mid):
#     print(f"[INFO] Message published. MID: {mid}")

# client = mqtt.Client(client_id="TestPublisher", protocol=mqtt.MQTTv5)
# client.on_publish = on_publish
# client.connect("localhost", 1883)

# topic = "home/temperature/kitchen"
# message = '{"room": "test", "temperature": 25}'
# result = client.publish(topic, message, qos=1, retain=True)

# if result.rc == mqtt.MQTT_ERR_SUCCESS:
#     print(f"[INFO] Message published successfully to {topic}")
# else:
#     print(f"[ERROR] Publish failed with code {result.rc}")

# client.disconnect()
