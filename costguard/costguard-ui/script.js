document.addEventListener("DOMContentLoaded", function () {

    fetch("http://localhost:8080/cloud/report")
        .then(response => response.json())
        .then(result => {

            const data = result.data;

            /* -----------------------
               SUMMARY CARDS
            ----------------------- */

            const currentCost = document.getElementById("currentCost");
            const savings = document.getElementById("savings");
            const optimizedCost = document.getElementById("optimizedCost");

            if (currentCost)
                currentCost.innerText = "$" + data.summary.currentMonthlyCost;

            if (savings)
                savings.innerText = "$" + data.summary.potentialSavings;

            if (optimizedCost)
                optimizedCost.innerText = "$" + data.summary.optimizedMonthlyCost;


            /* -----------------------
               SERVICE COST PIE CHART
            ----------------------- */

            const services = Object.keys(data.serviceCostBreakdown);
            const costs = Object.values(data.serviceCostBreakdown);

            const costChart = document.getElementById("costChart");

            if (costChart) {

                new Chart(costChart, {
                    type: "pie",
                    data: {
                        labels: services,
                        datasets: [{
                            label: "Service Cost",
                            data: costs
                        }]
                    }
                });

            }


            /* -----------------------
               COST VS SAVINGS BAR CHART
            ----------------------- */

            const savingsChart = document.getElementById("savingsChart");

            if (savingsChart) {

                const savingsData = services.map(service => {

                    if (service === "ec2") {
                        return data.summary.potentialSavings;
                    }

                    return 0;
                });

                new Chart(savingsChart, {
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
                                data: savingsData,
                                backgroundColor: "#22c55e"
                            }
                        ]
                    },
                    options: {
                        responsive: true,
                        plugins: {
                            legend: {
                                position: "top"
                            }
                        }
                    }
                });

            }

        })
        .catch(error => {
            console.error("API Error:", error);
        });

});