document.addEventListener("DOMContentLoaded", function () {

    fetch("http://localhost:8080/cloud/report")
    .then(response => response.json())
    .then(result => {

        const data = result.data;

        const currentCost = document.getElementById("currentCost");
        const savings = document.getElementById("savings");
        const optimizedCost = document.getElementById("optimizedCost");

        if (currentCost)
            currentCost.innerText = data.summary.currentMonthlyCost;

        if (savings)
            savings.innerText = data.summary.potentialSavings;

        if (optimizedCost)
            optimizedCost.innerText = data.summary.optimizedMonthlyCost;


        const chartElement = document.getElementById("costChart");

        if (chartElement) {

            const services = Object.keys(data.serviceCostBreakdown);
            const costs = Object.values(data.serviceCostBreakdown);

            new Chart(chartElement, {
                type: "pie",
                data: {
                    labels: services,
                    datasets: [{
                        data: costs
                    }]
                }
            });

        }

    })
    .catch(error => {
        console.error("API Error:", error);
    });

});