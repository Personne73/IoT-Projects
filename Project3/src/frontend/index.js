document.addEventListener("DOMContentLoaded", () => {
    const temperatureList = document.getElementById("temperature-list");

    // Fonction pour récupérer les températures depuis l'API
    async function fetchTemperatures() {
        try {
            const response = await fetch("http://127.0.0.1:8000/api/temperatures");
            if (!response.ok) {
                throw new Error("Error during the recover of the data");
            }
            const temperatures = await response.json();
            updateUI(temperatures);
        } catch (error) {
            console.error("Error :", error);
            temperatureList.innerHTML = `<li>Error while loading the data, think about starting the backend ;)</li>`;
        }
    }

    // Fonction pour mettre à jour la liste des températures
    function updateUI(temperatures) {
        temperatureList.innerHTML = ""; // Réinitialise la liste
        temperatures.forEach(temp => {
            const listItem = document.createElement("li");
            listItem.textContent = `${temp.room} : ${temp.temperature}°C`;
            temperatureList.appendChild(listItem);
        });
    }

    // Rafraîchir les températures toutes les 10 secondes
    fetchTemperatures(); // Appeler immédiatement
    setInterval(fetchTemperatures, 10000); // Répéter toutes les 10 secondes
});
