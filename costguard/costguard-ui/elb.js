document.addEventListener("DOMContentLoaded", function () {

    if (localStorage.getItem("theme") === "dark") {
        document.body.classList.add("dark");
    }

    document.querySelectorAll(".sidebar-menu li").forEach(item => {
        const page = item.getAttribute("data-page");

        if (window.location.pathname.includes(page)) {
            item.classList.add("active");
        }

        item.onclick = () => page && (window.location.href = page);
    });

    fetch("http://localhost:8080/cloud/report")
        .then(res => res.json())
        .then(result => {

            const data = result.data;
            const container = document.getElementById("elbContainer");

            if (data.elb && data.elb.status === "Not Initialized") {

                container.innerHTML = `
                    <div class="card">
                        <h2>ELB Status</h2>
                        <p>${data.elb.reason}</p>
                    </div>
                `;
                return;
            }

            container.innerHTML = `
                <div class="card">
                    <h2>ELB Data</h2>
                    <p>Load balancer data will appear here.</p>
                </div>
            `;
        });
});