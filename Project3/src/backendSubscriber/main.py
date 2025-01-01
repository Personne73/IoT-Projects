from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from MQTTClientSubscriber import start_mqtt_client, subscribed_data

app = FastAPI()

# Middleware CORS to allow the frontend to access the API
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Autorisez l'URL de votre frontend
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Exemple de données de températures
temperatures = [
    {"room": "kitchen", "temperature": 22.5},
    {"room": "livingroom", "temperature": 21.3},
    {"room": "bedroom", "temperature": 19.8},
    {"room": "bathroom", "temperature": 24.1},
]

mqtt_client_subscriber = start_mqtt_client()

@app.get("/api/temperatures")
def get_temperatures():
    return list(subscribed_data.values())

@app.get("/api/temperature/{room}")
def get_temperature(room: str):
    for temp in temperatures:
        if temp["room"] == room:
            return temp
    return {"error": "Room not found"}

# start the backend : uvicorn main:app --reload
# http://127.0.0.1:8000/api/temperatures
