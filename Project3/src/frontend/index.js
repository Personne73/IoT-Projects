// document.addEventListener("DOMContentLoaded", () => {
//     const temperatureList = document.getElementById("temperature-list");

//     // Fonction pour récupérer les températures depuis l'API
//     async function fetchTemperatures() {
//         try {
//             const response = await fetch("http://127.0.0.1:8000/api/temperatures");
//             if (!response.ok) {
//                 throw new Error("Error during the recover of the data");
//             }
//             const temperatures = await response.json();
//             updateUI(temperatures);
//         } catch (error) {
//             console.error("Error :", error);
//             temperatureList.innerHTML = `<li>Error while loading the data, think about starting the backend ;)</li>`;
//         }
//     }

//     // Fonction pour mettre à jour la liste des températures
//     function updateUI(temperatures) {
//         temperatureList.innerHTML = ""; // Réinitialise la liste
//         temperatures.forEach(temp => {
//             const listItem = document.createElement("li");
//             listItem.textContent = `${temp.room} : ${temp.temperature}°C`;
//             temperatureList.appendChild(listItem);
//         });
//     }

//     // Rafraîchir les températures toutes les 10 secondes
//     fetchTemperatures(); // Appeler immédiatement
//     setInterval(fetchTemperatures, 10000); // Répéter toutes les 10 secondes
// });

// document.addEventListener("DOMContentLoaded", () => {
//     const temperatureList = document.getElementById("temperature-list");
//     const topicsList = document.getElementById("topics-list");
//     const responseTimeElement = document.getElementById("response-time");
//     const topics = [
//         "home/temperature/kitchen",
//         "home/temperature/livingroom",
//         "home/temperature/bedroom",
//         "home/temperature/bathroom"
//     ];

//     // Fonction pour récupérer les températures depuis l'API
//     async function fetchTemperatures() {
//         try {
//             const startTime = performance.now(); // Mesurer le temps de réponse
//             const response = await fetch("http://127.0.0.1:8000/api/temperatures");
//             if (!response.ok) {
//                 throw new Error("Error during the recovery of the data");
//             }
//             const temperatures = await response.json();
//             updateUI(temperatures);

//             const endTime = performance.now();
//             const responseTime = Math.round(endTime - startTime);
//             updateResponseTime(responseTime);
//         } catch (error) {
//             console.error("Error :", error);
//             temperatureList.innerHTML = `<li>Error while loading the data, think about starting the backend ;)</li>`;
            
//             const listItem = document.createElement("li");
//             listItem.textContent = `kitchen : 33°C`;
//             temperatureList.appendChild(listItem);

//             const listItem2 = document.createElement("li");
//             listItem2.textContent = `livingroom : 33°C`;
//             temperatureList.appendChild(listItem2);

//             const listItem3 = document.createElement("li");
//             listItem3.textContent = `bathroom : 33°C`;
//             temperatureList.appendChild(listItem3);

//             const listItem4 = document.createElement("li");
//             listItem4.textContent = `bedroom : 33°C`;
//             temperatureList.appendChild(listItem4);
//         }
//     }

//     // Fonction pour mettre à jour la liste des températures
//     function updateUI(temperatures) {
//         temperatureList.innerHTML = ""; // Réinitialise la liste
//         temperatures.forEach(temp => {
//             const listItem = document.createElement("li");
//             listItem.textContent = `${temp.room} : ${temp.temperature}°C`;
//             temperatureList.appendChild(listItem);
//         });
//     }

//     // Fonction pour mettre à jour le temps de réponse
//     function updateResponseTime(time) {
//         responseTimeElement.textContent = `Temps de réponse: ${time} ms`;
//     }

//     // Ajouter les topics dans le menu
//     topics.forEach(topic => {
//         const listItem = document.createElement("li");
//         listItem.innerHTML = `
//             <span>${topic}</span>
//             <button class="subscribe-btn">S'abonner</button>
//         `;
//         topicsList.appendChild(listItem);
//     });

//     // Gérer le clic sur les boutons individuels d'abonnement
//     topicsList.addEventListener("click", (event) => {
//         if (event.target.classList.contains("subscribe-btn")) {
//             const topic = event.target.parentElement.querySelector("span").textContent;
//             console.log(`S'abonner au topic : ${topic}`);
//             // Ajouter ici la logique pour gérer l'abonnement au topic
//         }
//     });

//     // Gérer le clic sur "S'abonner à tous"
//     document.getElementById("subscribe-all").addEventListener("click", () => {
//         console.log("S'abonner à tous les topics");
//         // Logique pour s'abonner à tous les topics
//     });

//     // Rafraîchir les températures toutes les 10 secondes
//     fetchTemperatures(); // Appeler immédiatement
//     setInterval(fetchTemperatures, 10000); // Répéter toutes les 10 secondes
// });

// document.addEventListener("DOMContentLoaded", () => {
//     const temperatureGrid = document.getElementById("temperature-grid");
//     const topicsList = document.getElementById("topics-list");
//     const responseTimeElement = document.getElementById("response-time");
//     const subscribeAllButton = document.getElementById("subscribe-all");

//     const topics = [
//         "home/temperature/kitchen",
//         "home/temperature/livingroom",
//         "home/temperature/bedroom",
//         "home/temperature/bathroom"
//     ];

//     let subscribedTopics = new Set();

//     // Fonction pour récupérer les températures depuis l'API
//     async function fetchTemperatures() {
//         try {
//             const startTime = performance.now();
//             const response = await fetch("http://127.0.0.1:8000/api/temperatures");
//             if (!response.ok) {
//                 throw new Error("Error during the recovery of the data");
//             }
//             const temperatures = await response.json();
//             updateTemperatureGrid(temperatures);

//             const endTime = performance.now();
//             updateResponseTime(Math.round(endTime - startTime));
//         } catch (error) {
//             console.error("Error:", error);
//             temperatureGrid.innerHTML = `<p>Error loading data. Start your backend! :)</p>`;
//             const temperatures = [
//                 { room: "kitchen", temperature: 33 },
//                 { room: "livingroom", temperature: 33 },
//                 { room: "bathroom", temperature: 33 },
//                 { room: "bedroom", temperature: 33 }
//             ];
//             updateTemperatureGrid(temperatures);
//         }
//     }

//     // Fonction pour mettre à jour la grille des températures
//     function updateTemperatureGrid(temperatures) {
//         temperatureGrid.innerHTML = "";
//         temperatures.forEach(temp => {
//             const bubble = document.createElement("div");
//             bubble.className = "temperature-bubble";
//             bubble.innerHTML = `
//                 <div class="temperature">${temp.temperature}°C</div>
//                 <div class="room-name">${temp.room}</div>
//             `;
//             temperatureGrid.appendChild(bubble);
//         });
//     }

//     // Fonction pour mettre à jour le temps de réponse
//     function updateResponseTime(time) {
//         responseTimeElement.textContent = `Response delay: ${time} ms`;
//     }

//     // Ajouter les topics dans le menu
//     topics.forEach(topic => {
//         const listItem = document.createElement("li");
//         listItem.innerHTML = `
//             <span>${topic}</span>
//             <button class="subscribe-btn">Subscribe</button>
//         `;
//         topicsList.appendChild(listItem);
//     });

//     // Gérer les abonnements individuels
//     topicsList.addEventListener("click", (event) => {
//         if (event.target.classList.contains("subscribe-btn")) {
//             const topic = event.target.parentElement.querySelector("span").textContent;
//             if (subscribedTopics.has(topic)) {
//                 subscribedTopics.delete(topic);
//                 event.target.textContent = "Subscribe";
//             } else {
//                 subscribedTopics.add(topic);
//                 event.target.textContent = "Unsubscribe";
//                 event.target.style.backgroundColor = "red";
//             }
//             updateSubscribeAllButton();
//         }
//     });

//     // Gérer le bouton "Subscribe to all"
//     subscribeAllButton.addEventListener("click", () => {
//         if (subscribedTopics.size === topics.length) {
//             subscribedTopics.clear();
//             document.querySelectorAll(".subscribe-btn").forEach(button => button.textContent = "Subscribe");
//             subscribeAllButton.textContent = "Subscribe to all";
//         } else {
//             topics.forEach(topic => subscribedTopics.add(topic));
//             document.querySelectorAll(".subscribe-btn").forEach(button => button.textContent = "Unsubscribe");
//             subscribeAllButton.textContent = "Unsubscribe to all";
//         }
//     });

//     // Met à jour le texte du bouton "Subscribe to all"
//     function updateSubscribeAllButton() {
//         if (subscribedTopics.size === topics.length) {
//             subscribeAllButton.textContent = "Unsubscribe to all";
//         } else {
//             subscribeAllButton.textContent = "Subscribe to all";
//         }
//     }

//     // Rafraîchir les températures toutes les 10 secondes
//     fetchTemperatures();
//     setInterval(fetchTemperatures, 10000);
// });

//document.addEventListener("DOMContentLoaded", () => {
//     const temperatureGrid = document.getElementById("temperature-grid");
//     const topicsList = document.getElementById("topics-list");
//     const responseTimeElement = document.getElementById("response-time");
//     const subscribeAllButton = document.getElementById("subscribe-all");
//     const statusList = document.getElementById("status-list");

//     const topics = [
//         "home/temperature/kitchen",
//         "home/temperature/livingroom",
//         "home/temperature/bedroom",
//         "home/temperature/bathroom"
//     ];

//     let subscribedTopics = new Set();

//     async function fetchTemperatures() {
//         try {
//             const startTime = performance.now();
//             const response = await fetch("http://127.0.0.1:8000/api/temperatures");
//             if (!response.ok) throw new Error("Error loading data");

//             const temperatures = await response.json();
//             updateTemperatureGrid(temperatures);
//             updateTemperatureStatus(temperatures);

//             const endTime = performance.now();
//             updateResponseTime(Math.round(endTime - startTime));
//         } catch (error) {
//             console.error("Error:", error);
//             const mockTemperatures = [
//                 { room: "kitchen", temperature: 33 },
//                 { room: "livingroom", temperature: 22 },
//                 { room: "bathroom", temperature: 18 },
//                 { room: "bedroom", temperature: 28 }
//             ];
//             updateTemperatureGrid(mockTemperatures);
//             updateTemperatureStatus(mockTemperatures);
//         }
//     }

//     function updateTemperatureGrid(temperatures) {
//         temperatureGrid.innerHTML = "";
//         temperatures.forEach(temp => {
//             const bubble = document.createElement("div");
//             bubble.className = "temperature-bubble";
//             const temperatureColor =
//                 temp.temperature > 30
//                     ? "temperature-hot"
//                     : temp.temperature > 25
//                     ? "temperature-warm"
//                     : "temperature-normal";

//             bubble.innerHTML = `
//                 <div class="temperature ${temperatureColor}">${temp.temperature}°C</div>
//                 <div class="room-name">${temp.room}</div>
//             `;
//             temperatureGrid.appendChild(bubble);
//         });
//     }

//     function updateTemperatureStatus(temperatures) {
//         statusList.innerHTML = "";
//         temperatures.forEach(temp => {
//             const statusItem = document.createElement("li");
//             const status = temp.temperature > 25 ? "Too Hot" : "Normal";
//             statusItem.textContent = `${temp.room}: ${status}`;
//             statusList.appendChild(statusItem);
//         });
//     }

//     function updateResponseTime(time) {
//         responseTimeElement.textContent = `Response delay: ${time} ms`;
//     }

//     topics.forEach(topic => {
//         const listItem = document.createElement("li");
//         listItem.innerHTML = `
//             <span>${topic}</span>
//             <button class="subscribe-btn">Subscribe</button>
//         `;
//         topicsList.appendChild(listItem);
//     });

//     topicsList.addEventListener("click", event => {
//         if (event.target.classList.contains("subscribe-btn")) {
//             const topic = event.target.parentElement.querySelector("span").textContent;
//             if (subscribedTopics.has(topic)) {
//                 subscribedTopics.delete(topic);
//                 event.target.textContent = "Subscribe";
//                 event.target.classList.remove("unsubscribe");
//                 event.target.style.backgroundColor = "#007bff";
//             } else {
//                 subscribedTopics.add(topic);
//                 event.target.textContent = "Unsubscribe";
//                 event.target.classList.add("unsubscribe");
//             }
//             updateSubscribeAllButton();
//         }
//     });

//     subscribeAllButton.addEventListener("click", () => {
//         if (subscribedTopics.size === topics.length) {
//             subscribedTopics.clear();
//             document.querySelectorAll(".subscribe-btn").forEach(button => {
//                 button.textContent = "Subscribe";
//                 button.classList.remove("unsubscribe");
//             });
//             subscribeAllButton.textContent = "Subscribe to all";
//         } else {
//             topics.forEach(topic => subscribedTopics.add(topic));
//             document.querySelectorAll(".subscribe-btn").forEach(button => {
//                 button.textContent = "Unsubscribe";
//                 button.classList.add("unsubscribe");
//             });
//             subscribeAllButton.textContent = "Unsubscribe to all";
//         }
//     });

//     function updateSubscribeAllButton() {
//         if (subscribedTopics.size === topics.length) {
//             subscribeAllButton.textContent = "Unsubscribe to all";
//         } else {
//             subscribeAllButton.textContent = "Subscribe to all";
//         }
//     }

//     fetchTemperatures();
//     setInterval(fetchTemperatures, 10000);
// });

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

    let subscribedTopics = new Set();

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
            const bubble = document.createElement("div");
            bubble.className = "temperature-bubble";

            // Détermine la couleur de la température
            const temperatureColor = getTemperatureColor(temp.temperature);
            bubble.innerHTML = `
                <div class="temperature" style="color: ${temperatureColor}">${temp.temperature}°C</div>
                <div class="room-name">${temp.room}</div>
            `;
            temperatureGrid.appendChild(bubble);
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
            const statusItem = document.createElement("li");
            const status = temp.temperature > 25 ? "Too Hot" : "Normal";
            statusItem.textContent = `${temp.room}: ${status}`;
            statusList.appendChild(statusItem);
        });
    }

    // Fonction pour mettre à jour le temps de réponse
    function updateResponseTime(time) {
        responseTimeElement.textContent = `Response delay: ${time} ms`;
    }

    // Ajouter les topics dans le menu
    topics.forEach(topic => {
        const listItem = document.createElement("li");
        listItem.innerHTML = `
            <span>${topic}</span>
            <button class="subscribe-btn" style="background-color: #007bff;">Subscribe</button>
        `;
        topicsList.appendChild(listItem);
    });

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
