import asyncio
from aiocoap import resource, Message, Context
from aiocoap.numbers import Code
import random


'''
Represent a CoAP resource, a specific room temperature in this case.
'''
class TemperatureResource(resource.Resource):
    def __init__(self, room_name):
        super().__init__()
        self.room_name = room_name

    # GET request handler
    async def render_get(self, request):
        # Emulate a random temperature for the room
        temperature = random.uniform(18.0, 33.0)
        response = f'{{"room": "{self.room_name}", "temperature": {temperature:.2f}}}'
        print(f"[INFO] Sending temperature data: {response}")
        return Message(payload=response.encode('utf-8'), content_format=50)


'''
Represent a CoAP server that can handle multiple resources.
'''
class CoAPServer:
    def __init__(self):
        self.root = resource.Site()

    def add_room(self, room_name):
        path = f'{room_name}'
        self.root.add_resource((path,), TemperatureResource(room_name))
        print(f"[INFO] Added CoAP resource for {room_name} at path: /{path}")

    async def start(self):
        print("[INFO] Starting CoAP Server...")
        # await Context.create_server_context(self.root)
        await Context.create_server_context(self.root, bind=('127.0.0.1', 5683))
        await asyncio.get_running_loop().create_future()  # Keep the server running

if __name__ == "__main__":
    server = CoAPServer()
    # Add the ressource for each room
    server.add_room("kitchen")
    server.add_room("livingroom")
    server.add_room("bedroom")
    server.add_room("bathroom")

    # Start the server
    asyncio.run(server.start()) # default port (5683)
