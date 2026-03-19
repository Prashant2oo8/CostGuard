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

        item.onclick = () => {
            if (page) window.location.href = page;
        };
    });

    // FETCH DATA
    fetch("http://localhost:8080/cloud/report")
        .then(res => res.json())
        .then(result => {

            const data = result.data;
            const container = document.getElementById("rdsContainer");

            container.innerHTML = "";

            /* =========================
               CASE 1: NOT INITIALIZED
            ========================= */
            if (data.rds && data.rds.status === "Not Initialized") {

                container.innerHTML = `
                    <div class="card">
                        <h2>RDS Status</h2>
                        <p>${data.rds.reason}</p>
                    </div>
                `;

                return;
            }

            // Placeholder for future RDS table
            container.innerHTML = `
                <div class="card">
                    <h2>RDS Data</h2>
                    <p>RDS instances will be displayed here.</p>
                </div>
            `;

        });
});