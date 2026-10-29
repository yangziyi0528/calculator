/*
 * Calculator web frontend logic (Lab 7)
 *
 * All requests use the RELATIVE path /api/... The browser sends them
 * to the web container (nginx), which proxies them to the backend
 * container by its Compose service name. No localhost, no hard-coded
 * host - the page works wherever Compose deploys it.
 */

// Remember the currently selected operator.
let operator = "+";

const aInput = document.getElementById("operand-a");
const bInput = document.getElementById("operand-b");
const resultBox = document.getElementById("result");
const historyBox = document.getElementById("history");
const computeBtn = document.getElementById("compute");

// Operator buttons: click selects the operator (visual highlight).
document.querySelectorAll(".op").forEach((btn) => {
  btn.addEventListener("click", () => {
    document.querySelector(".op.selected").classList.remove("selected");
    btn.classList.add("selected");
    operator = btn.dataset.op;
  });
});

computeBtn.addEventListener("click", calculate);
// Enter in either input also computes.
[aInput, bInput].forEach((el) => el.addEventListener("keydown", (e) => {
  if (e.key === "Enter") calculate();
}));

async function calculate() {
  const a = aInput.value.trim();
  const b = bInput.value.trim();
  if (a === "" || b === "") {
    show("Please fill in both operands.", "err");
    return;
  }

  computeBtn.disabled = true;
  show("Computing ...", "");

  try {
    // encodeURIComponent makes sure "+" travels as %2B, otherwise a
    // URL-decoder would turn it into a space on the server side.
    const url = "/api/eval?a=" + encodeURIComponent(a)
              + "&op=" + encodeURIComponent(operator)
              + "&b=" + encodeURIComponent(b);

    const response = await fetch(url);
    const data = await response.json();

    if (response.ok) {
      show(`${data.a} ${data.op} ${data.b} = ${data.result}`, "ok");
      addHistory(`${data.a} ${data.op} ${data.b} = ${data.result}`);
    } else {
      show("Backend says: " + (data.error || response.status), "err");
    }
  } catch (err) {
    // fetch only rejects on network-level problems (proxy down, DNS, ...).
    show("Cannot reach the backend service.", "err");
  } finally {
    computeBtn.disabled = false;
  }
}

function show(text, cls) {
  resultBox.textContent = text;
  resultBox.className = "result" + (cls ? " " + cls : "");
}

function addHistory(entry) {
  const row = document.createElement("div");
  const name = document.createElement("span");
  name.textContent = entry;
  const time = document.createElement("span");
  time.textContent = new Date().toLocaleTimeString();
  row.appendChild(name);
  row.appendChild(time);
  historyBox.prepend(row);
  while (historyBox.children.length > 5) {
    historyBox.removeChild(historyBox.lastChild);
  }
}
