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
        type: "pie",
        data: { labels: services, datasets: [{ data: costs }] },
        options: {
            plugins: { legend: { labels: { color: colors.text } } }
        }
    });

    savingsChartInstance = new Chart(document.getElementById("savingsChart"), {
        type: "bar",
        data: {
            labels: services,
            datasets: [
                { label: "Cost", data: costs, backgroundColor: "#3b82f6" },
                { label: "Savings", data: savingsData, backgroundColor: "#22c55e" }
            ]
        },
        options: {
            plugins: { legend: { labels: { color: colors.text } } },
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

            document.getElementById("currentCost").innerText = "$" + data.summary.currentMonthlyCost;
            document.getElementById("savings").innerText = "$" + data.summary.potentialSavings;
            document.getElementById("optimizedCost").innerText = "$" + data.summary.optimizedMonthlyCost;

            updateCharts(data);

            // RECOMMENDATIONS
            const container = document.getElementById("recommendationsContainer");
            container.innerHTML = "";

            /* =========================
               1. MAIN BACKEND RECOMMENDATIONS
            ========================= */

            if (data.topWastefulResources.length > 0) {

                data.topWastefulResources.forEach(r => {

                    container.innerHTML += `
                        <div class="recommendation">
                            <div>
                                <div class="rec-title">${r.name.toUpperCase()}</div>
                                <div class="rec-desc">${r.reason}</div>
                            </div>
                            <div class="rec-saving">$${r.potentialSaving}</div>
                        </div>
                    `;
                });

            }

            /* =========================
               2. SUPPORTING INSIGHTS (OTHER SERVICES)
            ========================= */

            // EBS
            data.ebsVolumes.forEach(ebs => {
                container.innerHTML += `
                    <div class="recommendation low">
                        <div>
                            <div class="rec-title">EBS: ${ebs.volumeId}</div>
                            <div class="rec-desc">${ebs.recommendation}</div>
                        </div>
                    </div>
                `;
            });

            // S3
            data.s3Buckets.forEach(s3 => {
                container.innerHTML += `
                    <div class="recommendation low">
                        <div>
                            <div class="rec-title">S3: ${s3.bucketName}</div>
                            <div class="rec-desc">${s3.recommendation}</div>
                        </div>
                    </div>
                `;
            });

            // RDS / ELB / ASG
            if (data.rds?.status === "Not Initialized") {
                container.innerHTML += `
                    <div class="recommendation low">
                        <div>
                            <div class="rec-title">RDS</div>
                            <div class="rec-desc">${data.rds.reason}</div>
                        </div>
                    </div>
                `;
            }

            if (data.elb?.status === "Not Initialized") {
                container.innerHTML += `
                    <div class="recommendation low">
                        <div>
                            <div class="rec-title">ELB</div>
                            <div class="rec-desc">${data.elb.reason}</div>
                        </div>
                    </div>
                `;
            }

            if (data.autoscaling?.status === "Not Initialized") {
                container.innerHTML += `
                    <div class="recommendation low">
                        <div>
                            <div class="rec-title">AUTO SCALING</div>
                            <div class="rec-desc">${data.autoscaling.reason}</div>
                        </div>
                    </div>
                `;
            }

            /* =========================
               3. FALLBACK MESSAGE
            ========================= */

            if (data.topWastefulResources.length === 0) {
                container.innerHTML += `
                    <div class="recommendation low">
                        <div>
                            <div class="rec-title">NO MAJOR OPTIMIZATION NEEDED</div>
                            <div class="rec-desc">Your cloud resources are already optimized</div>
                        </div>
                    </div>
                `;
            }

        });
});

// UPDATE CHARTS
function updateCharts(data) {

    const services = Object.keys(data.serviceCostBreakdown);
    const costs = Object.values(data.serviceCostBreakdown);

    const savingsData = services.map(service => {

        // Only EC2 has real savings
        if (service === "ec2") {
            return data.summary.potentialSavings;
        }

        return 0;
    });

    renderCharts(services, costs, savingsData);
}