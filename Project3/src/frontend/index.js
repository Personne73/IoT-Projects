document.addEventListener("DOMContentLoaded", () => {
    const temperatureGrid = document.getElementById("temperature-grid");
    const topicsList = document.getElementById("topics-list");
    const responseTimeElement = document.getElementById("response-time");
    const subscribeAllButton = document.getElementById("subscribe-all");
    const statusList = document.getElementById("status-list");

    const topics = [
        "home/temperature/kitchen",
        "home/temperature/livingroom",
        "home/temperature/bedroom",
        "home/temperature/bathroom"
    ];

    let subscribedTopics = new Set(topics);

    // Fonction pour récupérer les températures depuis l'API
    async function fetchTemperatures() {
        try {
            const startTime = performance.now();
            const response = await fetch("http://127.0.0.1:8000/api/temperatures");
            if (!response.ok) throw new Error("Error loading data");

            const temperatures = await response.json();
            updateTemperatureGrid(temperatures);
            updateTemperatureStatus(temperatures);

            const endTime = performance.now();
            updateResponseTime(Math.round(endTime - startTime));
        } catch (error) {
            console.error("Error:", error);
            const mockTemperatures = [
                { room: "kitchen", temperature: 33 },
                { room: "livingroom", temperature: 22 },
                { room: "bathroom", temperature: 18 },
                { room: "bedroom", temperature: 28 }
            ];
            updateTemperatureGrid(mockTemperatures);
            updateTemperatureStatus(mockTemperatures);
        }
    }

    // Fonction pour mettre à jour la grille des températures
    function updateTemperatureGrid(temperatures) {
        temperatureGrid.innerHTML = "";
        temperatures.forEach(temp => {
            if(subscribedTopics.has(`home/temperature/${temp.room}`)) {
                const bubble = document.createElement("div");
                bubble.className = "temperature-bubble";

                // Détermine la couleur de la température
                const temperatureColor = getTemperatureColor(temp.temperature);
                bubble.innerHTML = `
                    <div class="temperature" style="color: ${temperatureColor}">${temp.temperature}°C</div>
                    <div class="room-name">${temp.room}</div>
                `;
                temperatureGrid.appendChild(bubble);
            }
        });
    }

    // Fonction pour déterminer la couleur de la température
    function getTemperatureColor(temperature) {
        if (temperature > 30) {
            return "#ff1900"; // Rouge
        } else if (temperature > 25) {
            return "#ffa500"; // Orange
        } else {
            return "#007bff"; // Bleu
        }
    }

    // Fonction pour mettre à jour l'état des températures dans l'aside
    function updateTemperatureStatus(temperatures) {
        statusList.innerHTML = "";
        temperatures.forEach(temp => {
            if (subscribedTopics.has(`home/temperature/${temp.room}`)) {
                const statusItem = document.createElement("li");
                const status = temp.temperature > 25 ? "Too Hot" : "Normal";
                statusItem.textContent = `${temp.room}: ${status}`;
                statusList.appendChild(statusItem);
            }
        });
    }

    // Fonction pour mettre à jour le temps de réponse
    function updateResponseTime(time) {
        responseTimeElement.textContent = `Response delay: ${time} ms`;
    }

    // Ajouter les topics dans le menu
    topics.forEach(topic => {
        const listItem = document.createElement("li");
        const isSubscribed = subscribedTopics.has(topic);
        listItem.innerHTML = `
            <span>${topic}</span>
            <button class="subscribe-btn" style="background-color: ${isSubscribed ? "#ff1900" : "#007bff"};">
                ${isSubscribed ? "Unsubscribe" : "Subscribe"}
            </button>
        `;
        topicsList.appendChild(listItem);
    });
    updateSubscribeAllButton(); // Mettre à jour le texte et la couleur du bouton "Subscribe to all"

    // Gérer les abonnements individuels
    topicsList.addEventListener("click", (event) => {
        if (event.target.classList.contains("subscribe-btn")) {
            const topic = event.target.parentElement.querySelector("span").textContent;
            if (subscribedTopics.has(topic)) {
                subscribedTopics.delete(topic);
                event.target.textContent = "Subscribe";
                event.target.style.backgroundColor = "#007bff"; // Bleu pour Subscribe
            } else {
                subscribedTopics.add(topic);
                event.target.textContent = "Unsubscribe";
                event.target.style.backgroundColor = "#ff1900"; // Rouge pour Unsubscribe
            }
            updateSubscribeAllButton();
            fetchTemperatures(); // Rafraîchir les données
        }
    });

    // Gérer le bouton "Subscribe to all"
    subscribeAllButton.addEventListener("click", () => {
        if (subscribedTopics.size === topics.length) {
            subscribedTopics.clear();
            document.querySelectorAll(".subscribe-btn").forEach(button => {
                button.textContent = "Subscribe";
                button.style.backgroundColor = "#007bff"; // Bleu
            });
            subscribeAllButton.textContent = "Subscribe to all";
            subscribeAllButton.style.backgroundColor = "#007bff"; // Bleu
        } else {
            topics.forEach(topic => subscribedTopics.add(topic));
            document.querySelectorAll(".subscribe-btn").forEach(button => {
                button.textContent = "Unsubscribe";
                button.style.backgroundColor = "#ff1900"; // Rouge
            });
            subscribeAllButton.textContent = "Unsubscribe to all";
            subscribeAllButton.style.backgroundColor = "#ff1900"; // Rouge
        }
        fetchTemperatures(); // Rafraîchir les données
    });

    // Met à jour le texte et la couleur du bouton "Subscribe to all"
    function updateSubscribeAllButton() {
        if (subscribedTopics.size === topics.length) {
            subscribeAllButton.textContent = "Unsubscribe to all";
            subscribeAllButton.style.backgroundColor = "#ff1900"; // Rouge
        } else {
            subscribeAllButton.textContent = "Subscribe to all";
            subscribeAllButton.style.backgroundColor = "#007bff"; // Bleu
        }
    }

    // Rafraîchir les températures toutes les 10 secondes
    fetchTemperatures();
    setInterval(fetchTemperatures, 10000);
});
