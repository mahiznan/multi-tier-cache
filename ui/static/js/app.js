document.getElementById("loadBtn").addEventListener("click", () => {
    const box = document.getElementById("responseBox");
    box.textContent = "Loading....";

    fetch("/data?id=1")
        .then(res => res.json())
        .then(data => {
            box.textContent = JSON.stringify(data, null, 2);
        })
        .catch(err => {
            box.textContent = "Error: " + err;
        });
});
