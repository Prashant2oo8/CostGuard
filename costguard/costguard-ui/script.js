let costChart;
let compareChart;

/* =========================
   THEME (RUN ONCE)
========================= */
function initTheme() {
    const btn = document.getElementById("themeBtn");

    if (!btn) return;

    btn.addEventListener("click", () => {
        document.body.classList.toggle("dark");

        btn.innerText =
            document.body.classList.contains("dark")
                ? "Light Mode"
                : "Dark Mode";
    });
}

/* =========================
   LOAD DATA
========================= */
async function loadData() {
    try {
        const res = await fetch("http://localhost:8080/cloud/report");

        if (!res.ok) throw new Error("API error");

        const result = await res.json();
        const data = result.data;

        /* =========================
           CARDS
        ========================= */
        document.getElementById("currentCost").innerText =
            "$" + (data.summary.currentMonthlyCost || 0);

        document.getElementById("savings").innerText =
            "$" + (data.summary.potentialSavings || 0);

        document.getElementById("savingsPercent").innerText =
            (data.summary.savingsPercentage || 0).toFixed(2) + "%";

        /* =========================
           CHART DATA
        ========================= */
        const entries = Object.entries(data.serviceCostBreakdown || {});

        const services = entries.map(e => {
            if (e[0] === "autoscaling") return "ASG";
            return e[0].toUpperCase();
        });

        const costs = entries.map(e => e[1]);

        const savings = costs.map(c => c * 0.3); // UI demo

        /* =========================
           DESTROY OLD CHARTS
        ========================= */
        if (costChart) costChart.destroy();
        if (compareChart) compareChart.destroy();

        /* =========================
           PIE CHART
        ========================= */
        costChart = new Chart(document.getElementById("costChart"), {
            type: "doughnut",
            data: {
                labels: services,
                datasets: [{
                    data: costs
                }]
            }
        });

        /* =========================
           BAR CHART
        ========================= */
        compareChart = new Chart(document.getElementById("compareChart"), {
            type: "bar",
            data: {
                labels: services,
                datasets: [
                    {
                        label: "Service Cost",
                        data: costs,
                        backgroundColor: "#3b82f6"
                    },
                    {
                        label: "Potential Savings",
                        data: savings,
                        backgroundColor: "#22c55e"
                    }
                ]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        labels: { font: { size: 12 } }
                    }
                },
                scales: {
                    x: {
                        ticks: {
                            maxRotation: 30,
                            minRotation: 30
                        }
                    }
                }
            }
        });

        /* =========================
           INSIGHTS
        ========================= */
        const insights = document.getElementById("insights");
        insights.innerHTML = "";

        (data.insights || []).forEach(i => {
            insights.innerHTML += `<div>${i}</div>`;
        });

        /* =========================
           EXPENSIVE
        ========================= */
        const exp = document.getElementById("expensive");
        exp.innerHTML = "";

        (data.expensiveResources || []).forEach(r => {
            exp.innerHTML += `<div>${r.name} - $${r.monthlyCost}</div>`;
        });

        /* =========================
           WASTEFUL
        ========================= */
        const waste = document.getElementById("wasteful");
        waste.innerHTML = "";

        (data.wasteResources || []).forEach(r => {
            waste.innerHTML += `<div>${r.name} - $${r.potentialSaving}</div>`;
        });

    } catch (err) {
        console.error("Error:", err);
        alert("Failed to load data. Check backend.");
    }
}

/* =========================
   INIT
========================= */
document.addEventListener("DOMContentLoaded", () => {
    initTheme();
    loadData();
});