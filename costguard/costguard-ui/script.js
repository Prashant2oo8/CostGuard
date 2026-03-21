let costChartInstance;
let savingsChartInstance;
let dashboardData;

// THEME COLORS
function getThemeColors() {
    const isDark = document.body.classList.contains("dark");

    return {
        text: isDark ? "#e5e7eb" : "#111827",
        grid: isDark ? "#334155" : "#e5e7eb"
    };
}

// SIDEBAR NAV
function initSidebar() {
    document.querySelectorAll(".sidebar-menu li").forEach(item => {

        const page = item.getAttribute("data-page");

        if (page && window.location.pathname.includes(page)) {
            item.classList.add("active");
        }

        item.addEventListener("click", () => {
            if (page) window.location.href = page;
        });
    });
}

// CHART RENDER
function renderCharts(services, costs, savingsData) {

    const colors = getThemeColors();

    if (costChartInstance) costChartInstance.destroy();
    if (savingsChartInstance) savingsChartInstance.destroy();

    costChartInstance = new Chart(document.getElementById("costChart"), {
        type: "doughnut",
        data: { labels: services, datasets: [{ data: costs }] },
        options: {
            plugins: {
                legend: { labels: { color: colors.text } },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return context.label + ": $" + context.raw;
                        }
                    }
                }
            }
        }
    });

    savingsChartInstance = new Chart(document.getElementById("savingsChart"), {
        type: "bar",
        data: {
            labels: services,
            datasets: [
                {
                    label: "Cost",
                    data: costs,
                    backgroundColor: "#3b82f6",
                    barThickness: 25
                },
                {
                    label: "Savings",
                    data: savingsData,
                    backgroundColor: "#22c55e",
                    barThickness: 25
                }
            ]
        },
        options: {
            plugins: {
                legend: { labels: { color: colors.text } },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return context.dataset.label + ": $" + context.raw;
                        }
                    }
                }
            },
            scales: {
                x: { ticks: { color: colors.text }, grid: { color: colors.grid } },
                y: { ticks: { color: colors.text }, grid: { color: colors.grid } }
            }
        }
    });
}

// MAIN
document.addEventListener("DOMContentLoaded", function () {

    initSidebar();

    // THEME LOAD
    const savedTheme = localStorage.getItem("theme");
    if (savedTheme === "dark") document.body.classList.add("dark");

    const toggleBtn = document.getElementById("themeToggle");

    toggleBtn.innerText = document.body.classList.contains("dark") ? "Light Mode" : "Dark Mode";

    toggleBtn.addEventListener("click", () => {

        document.body.classList.toggle("dark");

        const isDark = document.body.classList.contains("dark");

        localStorage.setItem("theme", isDark ? "dark" : "light");
        toggleBtn.innerText = isDark ? "Light Mode" : "Dark Mode";

        if (dashboardData) {
            updateCharts(dashboardData);
        }
    });

    // FETCH DATA
    fetch("http://localhost:8080/cloud/report")
        .then(res => res.json())
        .then(result => {

            const data = result.data;
            dashboardData = data;

            // SUMMARY
            document.getElementById("currentCost").innerText = "$" + data.summary.currentMonthlyCost;
            document.getElementById("savings").innerText = "$" + data.summary.potentialSavings;
            document.getElementById("optimizedCost").innerText = "$" + data.summary.optimizedMonthlyCost;

            document.getElementById("efficiencyScore").innerText =
                data.summary.efficiencyScore + "/10";

            document.getElementById("savingsPercent").innerText =
                data.summary.savingsPercentage.toFixed(2) + "%";

            updateCharts(data);

            /* =========================
               INSIGHTS SECTION
            ========================= */

            const insightsContainer = document.getElementById("insightsContainer");

            if (insightsContainer) {

                insightsContainer.innerHTML = "";

                if (data.optimizationInsights && data.optimizationInsights.length > 0) {

                    data.optimizationInsights.forEach(insight => {
                        insightsContainer.innerHTML += `
                            <div class="insight">
                                ${insight}
                            </div>
                        `;
                    });

                } else {
                    insightsContainer.innerHTML = `
                        <div class="insight">
                            All resources are already optimized
                        </div>
                    `;
                }

                // Only ONE max service logic
                const maxService = Object.entries(data.serviceCostBreakdown)
                    .reduce((a, b) => a[1] > b[1] ? a : b);

                insightsContainer.innerHTML += `
                    <div class="insight">
                        Highest cost service: ${maxService[0].toUpperCase()}
                    </div>
                `;
            }

            /* =========================
               MAIN PROBLEM SERVICE
            ========================= */

            if (insightsContainer) {
                const maxService = Object.entries(data.serviceCostBreakdown)
                    .reduce((a, b) => a[1] > b[1] ? a : b);

                const serviceName = maxService[0].toUpperCase();

                insightsContainer.innerHTML += `
                    <div class="insight">
                        Highest cost service: ${serviceName}
                    </div>
                `;
            }

            /* =========================
               TOP EXPENSIVE RESOURCES
            ========================= */

            const expensiveContainer = document.getElementById("expensiveContainer");

            if (expensiveContainer) {
                expensiveContainer.innerHTML = "";

                data.topExpensiveResources.forEach(r => {
                    expensiveContainer.innerHTML += `
                        <div class="recommendation high">
                            <div>
                                <div class="rec-title">${r.name}</div>
                                <div class="rec-desc">${r.type}</div>
                            </div>
                            <div class="rec-saving">$${r.monthlyCost}</div>
                        </div>
                    `;
                });
            }

            if (data.topExpensiveResources.length === 0) {
                expensiveContainer.innerHTML = `
                    <div class="recommendation low">
                        <div class="rec-title">NO EXPENSIVE RESOURCES</div>
                        <div class="rec-desc">All services are cost-efficient</div>
                    </div>
                `;
            }

            /* =========================
                  TOP Wasteful  RESOURCES
                 ========================= */

            const wastefulContainer = document.getElementById("wastefulContainer");

            if (wastefulContainer) {
                wastefulContainer.innerHTML = "";

                data.topWastefulResources.forEach(r => {
                    wastefulContainer.innerHTML += `
                        <div class="recommendation high">
                            <div>
                                <div class="rec-title">${r.name}</div>
                                <div class="rec-desc">${r.reason}</div>
                            </div>
                            <div class="rec-saving">$${r.potentialSaving}</div>
                        </div>
                    `;
                });
            }

            if (data.topWastefulResources.length === 0) {
                wastefulContainer.innerHTML = `
                    <div class="recommendation low">
                        <div class="rec-title">NO WASTEFUL RESOURCES</div>
                        <div class="rec-desc">No optimization needed</div>
                    </div>
                `;
            }


        });
});

// UPDATE CHARTS
function updateCharts(data) {

    const entries = Object.entries(data.serviceCostBreakdown)
        .sort((a, b) => b[1] - a[1]); // descending order

    const services = entries.map(e => e[0]);
    const costs = entries.map(e => e[1]);

    const savingsData = services.map(service => {
        if (service === "ec2") {
            return data.summary.potentialSavings;
        }
        return 0;
    });

    renderCharts(services, costs, savingsData);
}