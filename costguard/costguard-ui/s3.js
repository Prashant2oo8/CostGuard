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
            const buckets = data.s3Buckets;

            const tableBody = document.querySelector("#s3Table tbody");
            tableBody.innerHTML = "";

            // SUMMARY
            document.getElementById("totalBuckets").innerText = buckets.length;

            document.getElementById("totalStorage").innerText =
                buckets.reduce((sum, b) => sum + b.storageGB, 0);

            document.getElementById("s3Cost").innerText =
                "$" + buckets.reduce((sum, b) => sum + b.monthlyCost, 0).toFixed(3);

            // TABLE
            buckets.forEach(b => {

                const row = `
                    <tr>
                        <td>${b.bucketName}</td>
                        <td>${b.storageGB} GB</td>
                        <td>$${b.monthlyCost}</td>
                        <td>
                            <span class="rec-badge ${getS3RecClass(b.recommendation)}">
                                ${b.recommendation}
                            </span>
                        </td>
                    </tr>
                `;

                tableBody.innerHTML += row;
            });

        });
});

function getS3RecClass(rec) {

    rec = rec.toLowerCase();

    if (rec.includes("lifecycle")) return "high";
    if (rec.includes("monitor")) return "medium";

    return "low";
}