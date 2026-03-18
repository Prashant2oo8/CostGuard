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

    // DATA
    fetch("http://localhost:8080/cloud/report")
        .then(response => response.json())
        .then(result => {

            const data = result.data;
            const tableBody = document.querySelector("#ec2Table tbody");

            tableBody.innerHTML = "";

            const instances = data.ec2Instances;

            document.getElementById("totalInstances").innerText = instances.length;

            document.getElementById("runningInstances").innerText =
                instances.filter(i => i.state === "running").length;

            document.getElementById("stoppedInstances").innerText =
                instances.filter(i => i.state === "stopped").length;

            document.getElementById("ec2Cost").innerText =
                "$" + instances.reduce((sum, i) => sum + i.monthlyCost, 0).toFixed(2);

            data.ec2Instances.forEach(instance => {

                const state = instance.state.toUpperCase();

                const row = `
                    <tr>
                        <td>${instance.instanceId}</td>
                        <td>${instance.instanceType.toUpperCase()}</td>
                        <td>
                            <span class="badge ${instance.state}">
                                ${state}
                            </span>
                        </td>
                        <td>${instance.cpuUtilization}%</td>
                        <td>$${instance.monthlyCost}</td>
                        <td>${instance.recommendation}</td>
                    </tr>
                `;

                tableBody.innerHTML += row;
            });

        });
});