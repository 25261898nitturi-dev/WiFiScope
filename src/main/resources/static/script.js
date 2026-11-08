
// SIGNAL CHART

const ctx = document.getElementById("signalChart");

const chart = new Chart(ctx, {

    type: "line",

    data: {

        labels: [],

        datasets: [{

            label: "WiFi Signal (%)",

            data: [],

            borderColor: "#38bdf8",

            backgroundColor: "rgba(56,189,248,0.15)",

            fill: true,

            tension: 0.4

        }]

    },

    options: {

        responsive: true,

        scales: {

            y: {

                min: 0,

                max: 100

            }

        }

    }

});


// CONNECTED WIFI DETAILS

async function getWifiData() {

    try {

        const response = await fetch("/api/wifi");

        if (!response.ok) {
            throw new Error("WiFi API error");
        }

        const data = await response.json();

        const percentage = parseInt(data.signal);

        document.getElementById("wifiName").textContent =
            data.ssid || "Unknown";

        document.getElementById("connection").textContent =
            data.status === "Connected"
                ? "● Connected"
                : "● Disconnected";

        document.getElementById("signal").textContent =
            isNaN(percentage) ? "--%" : percentage + "%";

        document.getElementById("quality").textContent =

            isNaN(percentage) ? "Unknown" :

            percentage >= 80 ? "Excellent" :

            percentage >= 60 ? "Good" :

            percentage >= 40 ? "Fair" : "Weak";

        document.getElementById("rssi").textContent =
            "RSSI: " + (data.rssi || "Not available");

        document.getElementById("band").textContent =
            data.band || "N/A";

        document.getElementById("channel").textContent =
            data.channel || "N/A";

        document.getElementById("radio").textContent =
            data.standard || "N/A";

        document.getElementById("security").textContent =
            data.security || "N/A";

        document.getElementById("receive").textContent =
            (data.receiveRate || "N/A") + " Mbps";

        document.getElementById("transmit").textContent =
            (data.transmitRate || "N/A") + " Mbps";


        // UPDATE CHART

        if (!isNaN(percentage)) {

            chart.data.labels.push(
                new Date().toLocaleTimeString()
            );

            chart.data.datasets[0].data.push(percentage);

            if (chart.data.labels.length > 20) {

                chart.data.labels.shift();

                chart.data.datasets[0].data.shift();

            }

            chart.update();

        }

    }

    catch (error) {

        console.error("WiFi Error:", error);

        document.getElementById("connection").textContent =
            "● API Unavailable";

    }

}


// NEARBY WIFI NETWORKS

async function loadNearbyNetworks() {

    const table = document.getElementById("nearbyNetworks");

    try {

        const response = await fetch("/api/wifi/nearby");

        if (!response.ok) {

            throw new Error("API error: " + response.status);

        }

        const networks = await response.json();

        table.innerHTML = "";

        if (!Array.isArray(networks) || networks.length === 0) {

            table.innerHTML =
                '<tr><td colspan="5">No nearby networks found</td></tr>';

            return;

        }

        networks.forEach(network => {

            const row = document.createElement("tr");

            const values = [

                network.ssid,

                network.bssid,

                network.signal,

                network.channel,

                network.security

            ];

            values.forEach(value => {

                const cell = document.createElement("td");

                cell.textContent = value || "N/A";

                row.appendChild(cell);

            });

            table.appendChild(row);

        });

    }

    catch (error) {

        console.error("Nearby WiFi Error:", error);

        table.innerHTML =
            '<tr><td colspan="5">Unable to load nearby networks</td></tr>';

    }

}


// AUTOMATIC REFRESH

async function updateDashboard() {

    await getWifiData();

    await loadNearbyNetworks();

}

updateDashboard();

setInterval(updateDashboard, 10000);