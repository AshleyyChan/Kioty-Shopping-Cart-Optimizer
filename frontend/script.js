document.addEventListener("DOMContentLoaded", () => {
  let items = [];
  const HISTORY_KEY = "cartHistory";
  const isLocal = window.location.hostname === "localhost" ||
                  window.location.hostname === "127.0.0.1" ||
                  window.location.protocol === "file:" ||
                  !window.location.hostname;
  const API_BASE = isLocal ? "http://localhost:5050" : "https://kioty.onrender.com";

  // DOM helpers
  const $ = (id) => document.getElementById(id);
  const createEl = (tag, className = "", text = "") => {
    const el = document.createElement(tag);
    if (className) el.className = className;
    if (text) el.textContent = text;
    return el;
  };

  // Theme persistence
  const savedTheme = localStorage.getItem("theme");
  if (savedTheme === "dark") document.body.classList.add("dark");
  if ($("themeToggle")) {
    $("themeToggle").checked = document.body.classList.contains("dark");
    $("themeToggle").addEventListener("change", () => {
      document.body.classList.toggle("dark");
      localStorage.setItem("theme", document.body.classList.contains("dark") ? "dark" : "light");
      showToast(`Theme: ${document.body.classList.contains("dark") ? "🌙 Dark" : "☀️ Light"}`, "info");
    });
  }

  // Enter key navigation
  ["itemName", "itemPrice", "itemValue"].forEach((id, idx, arr) => {
    if ($(id)) {
      $(id).addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
          e.preventDefault();
          idx < arr.length - 1 ? $(arr[idx + 1]).focus() : $("addItem")?.click();
        }
      });
    }
  });

  // Add item
  if ($("addItem")) {
    $("addItem").addEventListener("click", (e) => {
      e.preventDefault();
      const name = $("itemName")?.value.trim();
      const price = parseFloat($("itemPrice")?.value);
      const value = parseFloat($("itemValue")?.value);

      if (!name || isNaN(price) || isNaN(value) || price <= 0 || value <= 0) {
        showToast("⚠️ Please enter valid item details", "warning");
        return;
      }

      items.push({ name, price, value });
      updateItemList();
      clearInputs();
      updateProgressBar();
      $("itemName")?.focus();
      showToast(`✅ Added: ${name}`);
    });
  }

  function clearInputs() {
    ["itemName", "itemPrice", "itemValue"].forEach((id) => {
      if ($(id)) $(id).value = "";
    });
  }

  function updateItemList() {
    const list = $("itemList");
    if (!list) return;

    list.innerHTML = items.length
      ? ""
      : `<li class="list-group-item text-muted">🛒 Cart is empty</li>`;

    items.forEach((item, index) => {
      const li = createEl("li", "list-group-item d-flex justify-content-between align-items-center fade-in");
      li.innerHTML = `
        <span>${item.name} — ₹${item.price}, Value: ${item.value}</span>
        <button class="btn btn-sm btn-danger" data-index="${index}">Remove</button>
      `;
      li.querySelector("button").addEventListener("click", () => removeItem(index));
      list.appendChild(li);
    });
  }

  function removeItem(index) {
    const removed = items[index].name;
    items.splice(index, 1);
    updateItemList();
    updateProgressBar();
    showToast(`🗑️ Removed: ${removed}`, "danger");
  }

  function showToast(message, type = "success") {
    const toast = createEl("div", `toast-message bg-${type}`);
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => toast.classList.add("show"), 10);
    setTimeout(() => {
      toast.classList.remove("show");
      setTimeout(() => toast.remove(), 300);
    }, 2500);
  }

  function updateProgressBar() {
    const budget = parseFloat($("budget")?.value) || 0;
    const spent = items.reduce((sum, item) => sum + item.price, 0);
    const progress = budget > 0 ? Math.min((spent / budget) * 100, 100) : 0;

    const progressBar = $("budgetProgress");
    if (!progressBar) return;

    progressBar.style.width = `${progress}%`;
    progressBar.className = "progress-bar";
    if (progress < 50) progressBar.classList.add("bg-success");
    else if (progress < 90) progressBar.classList.add("bg-warning");
    else progressBar.classList.add("bg-danger");
  }

  // Optimize cart via backend
  if ($("optimizeBtn")) {
    $("optimizeBtn").addEventListener("click", async () => {
      const budget = parseFloat($("budget")?.value);
      if (isNaN(budget) || budget <= 0) return showToast("⚠️ Please enter a valid budget", "warning");
      if (!items.length) return showToast("🛒 Cart is empty", "warning");

      try {
        const res = await fetch(`${API_BASE}/optimize`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ budget, items })
        });

        if (!res.ok) {
          const err = await res.json();
          throw new Error(err.error || "Optimization failed");
        }

        const data = await res.json();
        displayResults(data.selectedItems, data.totalPrice, data.totalValue, data.sessionId);
        saveHistory(data.selectedItems, budget, data.totalPrice, data.totalValue);
        showToast("🎯 Optimization Complete!");
      } catch (err) {
        console.error(err);
        showToast(`❌ ${err.message}`, "danger");
      }
    });
  }

  function displayResults(selected, totalPrice, totalValue, sessionId) {
    const resultItems = $("resultItems");
    if (!resultItems) return;

    resultItems.innerHTML = selected.length
      ? ""
      : `<li class="list-group-item text-danger">❌ No items fit the budget</li>`;

    selected.forEach(item => {
      resultItems.appendChild(createEl("li", "list-group-item fade-in", `${item.name} — ₹${item.price}, Value: ${item.value}`));
    });

    $("totalPrice").textContent = totalPrice.toFixed(2);
    $("totalValue").textContent = totalValue.toFixed(2);
    $("itemCount").textContent = selected.length;
    $("sessionId").textContent = `#${sessionId || Math.random().toString(36).slice(2, 8).toUpperCase()}`;
    $("resultSection")?.classList.remove("d-none");
    $("resultSection")?.scrollIntoView({ behavior: "smooth" });
  }

  function saveHistory(selectedItems, budget, totalPrice, totalValue) {
    let history = JSON.parse(localStorage.getItem(HISTORY_KEY)) || [];
    history.push({
      date: new Date().toLocaleString(),
      budget,
      items: selectedItems,
      totalPrice,
      totalValue
    });
    localStorage.setItem(HISTORY_KEY, JSON.stringify(history));
  }

  function loadHistory() {
    const history = JSON.parse(localStorage.getItem(HISTORY_KEY)) || [];
    const historyList = $("historyList");
    if (!historyList) return;

    historyList.innerHTML = history.length
      ? ""
      : "<li>No history found.</li>";

    history.forEach(entry => {
      const li = document.createElement("li");
      li.innerHTML = `
        <strong>${entry.date}</strong><br>
        Budget: ₹${entry.budget}<br>
        Items: ${entry.items.map(i => `${i.name} (₹${i.price})`).join(", ")}<br>
        Total Price: ₹${entry.totalPrice} | Total Value: ${entry.totalValue}
        <hr>
      `;
      historyList.appendChild(li);
    });
  }

  if ($("toggleHistoryBtn")) {
    $("toggleHistoryBtn").addEventListener("click", () => {
      const section = $("historySection");
      if (section.style.display === "none" || section.style.display === "") {
        section.style.display = "block";
        loadHistory();
      } else {
        section.style.display = "none";
      }
    });
  }
});
