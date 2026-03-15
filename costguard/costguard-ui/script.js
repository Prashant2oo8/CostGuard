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

})
.catch(error => {
    console.log("Error:", error);
});