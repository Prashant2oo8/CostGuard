fetch("http://localhost:8080/cloud/report")
.then(response => response.json())
.then(result => {

    const data = result.data;

    document.getElementById("currentCost").innerText =
        data.summary.currentMonthlyCost;

    document.getElementById("savings").innerText =
        data.summary.potentialSavings;

    document.getElementById("optimizedCost").innerText =
        data.summary.optimizedMonthlyCost;


    // Chart Data
    const services = Object.keys(data.serviceCostBreakdown);
    const costs = Object.values(data.serviceCostBreakdown);

    new Chart(document.getElementById("costChart"), {
        type: 'pie',
        data: {
            labels: services,
            datasets: [{
                data: costs
            }]
        }
    });


    // Expensive Resources Table
    const expensiveTable = document.querySelector("#expensiveTable tbody");

    data.topExpensiveResources.forEach(resource => {
        const row = `
        <tr>
            <td>${resource.name}</td>
            <td>${resource.type}</td>
            <td>${resource.monthlyCost}</td>
        </tr>`;
        expensiveTable.innerHTML += row;
    });


    // Waste Resources Table
    const wasteTable = document.querySelector("#wasteTable tbody");

    data.topWastefulResources.forEach(resource => {
        const row = `
        <tr>
            <td>${resource.name}</td>
            <td>${resource.reason}</td>
            <td>${resource.potentialSaving}</td>
        </tr>`;
        wasteTable.innerHTML += row;
    });

});