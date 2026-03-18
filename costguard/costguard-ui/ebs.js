document.addEventListener("DOMContentLoaded", function () {

    // THEME
    if (localStorage.getItem("theme") === "dark") {
        document.body.classList.add("dark");
    }

    // SIDEBAR
    document.querySelectorAll(".sidebar-menu li").forEach(item => {

        const page = item.getAttribute("data-page");

        if (window.location.pathname.includes(page)) {
            item.classList.add("active");
        }

        item.onclick = () => window.location.href = page;
    });

    // FETCH DATA
    fetch("http://localhost:8080/cloud/report")
        .then(res => res.json())
        .then(result => {

            const data = result.data;
            const volumes = data.ebsVolumes;

            const tableBody = document.querySelector("#ebsTable tbody");
            tableBody.innerHTML = "";

            // SUMMARY
            document.getElementById("totalVolumes").innerText = volumes.length;

            document.getElementById("inUseVolumes").innerText =
                volumes.filter(v => v.state === "in-use").length;

            document.getElementById("availableVolumes").innerText =
                volumes.filter(v => v.state === "available").length;

            document.getElementById("ebsCost").innerText =
                "$" + volumes.reduce((sum, v) => sum + v.monthlyCost, 0).toFixed(2);

            // TABLE
            volumes.forEach(v => {

                const state = v.state.toUpperCase();

                const row = `
                    <tr>
                        <td>${v.volumeId}</td>
                        <td>${v.volumeType.toUpperCase()}</td>
                        <td>
                            <span class="badge ${v.state}">
                                ${state}
                            </span>
                        </td>
                        <td>${v.size} GB</td>
                        <td>$${v.monthlyCost}</td>
                        <td>
                            <span class="rec-badge ${getRecClass(v.recommendation)}">
                                ${v.recommendation}
                            </span>
                        </td>
                    </tr>
                `;

                tableBody.innerHTML += row;
            });

        });
});

function getRecClass(rec) {

    rec = rec.toLowerCase();

    if (rec.includes("safe to delete")) return "high";
    if (rec.includes("create snapshot")) return "medium";
    if (rec.includes("review")) return "medium";

    return "low";
}