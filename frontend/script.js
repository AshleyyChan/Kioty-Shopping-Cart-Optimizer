document.addEventListener("DOMContentLoaded", () => {
  let items = [];
  const HISTORY_KEY = "cartHistory";
  const CART_STORAGE_KEY = "kiotyCurrentCart";
  const BUDGET_STORAGE_KEY = "kiotyCurrentBudget";

  // Determine API base URL
  const isLocal =
    window.location.hostname === "localhost" ||
    window.location.hostname === "127.0.0.1" ||
    window.location.protocol === "file:" ||
    !window.location.hostname;

  const API_BASE = isLocal
    ? (window.location.port === "5050" ? window.location.origin : "http://localhost:5050")
    : "https://kioty.onrender.com";

  // DOM Helpers
  const $ = (id) => document.getElementById(id);
  const createEl = (tag, className = "", text = "") => {
    const el = document.createElement(tag);
    if (className) el.className = className;
    if (text) el.textContent = text;
    return el;
  };

  // Theme Sync
  const savedTheme = localStorage.getItem("theme") || "light";
  if (savedTheme === "dark") {
    document.body.classList.add("dark");
  }
  const themeToggle = $("themeToggle");
  if (themeToggle) {
    themeToggle.checked = savedTheme === "dark";
    themeToggle.addEventListener("change", () => {
      const isDark = themeToggle.checked;
      document.body.classList.toggle("dark", isDark);
      localStorage.setItem("theme", isDark ? "dark" : "light");
      showToast(isDark ? "🌙 Dark theme activated" : "☀️ Light theme activated", "info");
    });
  }

  // Load Saved Cart & Budget from LocalStorage
  loadCartFromStorage();

  // Real-time Budget Progress Listener
  const budgetInput = $("budget");
  if (budgetInput) {
    budgetInput.addEventListener("input", () => {
      localStorage.setItem(BUDGET_STORAGE_KEY, budgetInput.value.trim());
      updateProgressBar();
    });
  }

  // Enter Key Navigation
  const inputOrder = ["itemName", "itemPrice", "itemValue"];
  inputOrder.forEach((id, idx) => {
    const el = $(id);
    if (el) {
      el.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
          e.preventDefault();
          if (idx < inputOrder.length - 1) {
            $(inputOrder[idx + 1])?.focus();
          } else {
            $("addItem")?.click();
          }
        }
      });
    }
  });

  // Add Item Handler
  const addItemBtn = $("addItem");
  if (addItemBtn) {
    addItemBtn.addEventListener("click", (e) => {
      e.preventDefault();
      const name = $("itemName")?.value.trim();
      const priceRaw = $("itemPrice")?.value.trim();
      const valueRaw = $("itemValue")?.value.trim();

      const price = parseInt(priceRaw, 10);
      const value = parseInt(valueRaw, 10);

      if (!name) {
        showToast("⚠️ Please enter an item name.", "warning");
        $("itemName")?.focus();
        return;
      }

      if (isNaN(price) || price <= 0) {
        showToast("⚠️ Price must be a positive whole number (₹).", "warning");
        $("itemPrice")?.focus();
        return;
      }

      if (isNaN(value) || value < 1 || value > 10) {
        showToast("⚠️ Importance must be between 1 and 10.", "warning");
        $("itemValue")?.focus();
        return;
      }

      items.push({ name, price, value });
      saveCartToStorage();
      updateItemList();
      clearInputs();
      updateProgressBar();
      $("itemName")?.focus();
      showToast(`✅ Added "${name}" (₹${price}, Importance: ${value})`, "success");
    });
  }

  // Sample Cart Loader
  const loadSampleBtn = $("loadSampleBtn");
  if (loadSampleBtn) {
    loadSampleBtn.addEventListener("click", (e) => {
      e.preventDefault();
      items = [
        { name: "Noise Cancelling Headphones", price: 1500, value: 9 },
        { name: "Wireless Mechanical Keyboard", price: 1200, value: 8 },
        { name: "Ergonomic Desk Mouse", price: 600, value: 7 },
        { name: "Coffee Thermos Tumbler", price: 350, value: 6 },
        { name: "USB-C Fast Charger Cable", price: 250, value: 5 }
      ];
      if (budgetInput && (!budgetInput.value || parseInt(budgetInput.value, 10) < 2000)) {
        budgetInput.value = "2500";
        localStorage.setItem(BUDGET_STORAGE_KEY, "2500");
      }
      saveCartToStorage();
      updateItemList();
      updateProgressBar();
      showToast("✨ Sample cart loaded!", "info");
    });
  }

  // Clear Cart Handler
  const clearCartBtn = $("clearCartBtn");
  if (clearCartBtn) {
    clearCartBtn.addEventListener("click", (e) => {
      e.preventDefault();
      if (!items.length) {
        showToast("🛒 Cart is already empty.", "info");
        return;
      }
      items = [];
      saveCartToStorage();
      updateItemList();
      updateProgressBar();
      $("resultSection")?.classList.add("d-none");
      showToast("🗑️ Cart cleared.", "info");
    });
  }

  function clearInputs() {
    ["itemName", "itemPrice", "itemValue"].forEach((id) => {
      if ($(id)) $(id).value = "";
    });
  }

  function saveCartToStorage() {
    try {
      localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(items));
    } catch (e) {
      console.warn("Failed to save cart to localStorage:", e);
    }
  }

  function loadCartFromStorage() {
    try {
      const savedCart = localStorage.getItem(CART_STORAGE_KEY);
      if (savedCart) {
        items = JSON.parse(savedCart);
        if (!Array.isArray(items)) items = [];
      }
      const savedBudget = localStorage.getItem(BUDGET_STORAGE_KEY);
      if (savedBudget && $("budget")) {
        $("budget").value = savedBudget;
      }
    } catch (e) {
      items = [];
    }
    updateItemList();
    updateProgressBar();
  }

  function getImportanceBadge(val) {
    if (val >= 8) return `<span class="badge-value badge-value-high">⭐ Priority: ${val}/10</span>`;
    if (val >= 5) return `<span class="badge-value badge-value-med">⭐ Priority: ${val}/10</span>`;
    return `<span class="badge-value badge-value-low">⭐ Priority: ${val}/10</span>`;
  }

  function updateItemList() {
    const list = $("itemList");
    const countBadge = $("cartCountBadge");
    if (countBadge) {
      countBadge.textContent = `${items.length} ${items.length === 1 ? 'item' : 'items'}`;
    }
    if (!list) return;

    if (!items.length) {
      list.innerHTML = `<li class="list-group-item text-muted py-3 text-center">🛒 Your cart is empty. Add items above to get started.</li>`;
      return;
    }

    list.innerHTML = "";
    items.forEach((item, index) => {
      const li = createEl("li", "list-group-item item-slide-in");
      li.innerHTML = `
        <div class="d-flex align-items-center gap-3">
          <strong>${escapeHtml(item.name)}</strong>
          <span class="text-muted">₹${item.price}</span>
          ${getImportanceBadge(item.value)}
        </div>
        <button class="btn btn-sm btn-danger remove-btn" aria-label="Remove item">Remove</button>
      `;
      li.querySelector(".remove-btn").addEventListener("click", () => removeItem(index, li));
      list.appendChild(li);
    });
  }

  function removeItem(index, element) {
    if (element) {
      element.classList.remove("item-slide-in");
      element.classList.add("item-fade-out");
      setTimeout(() => {
        const removed = items[index]?.name || "Item";
        items.splice(index, 1);
        saveCartToStorage();
        updateItemList();
        updateProgressBar();
        showToast(`🗑️ Removed: ${removed}`, "danger");
      }, 300);
    } else {
      const removed = items[index]?.name || "Item";
      items.splice(index, 1);
      saveCartToStorage();
      updateItemList();
      updateProgressBar();
      showToast(`🗑️ Removed: ${removed}`, "danger");
    }
  }

  function updateProgressBar() {
    const budget = parseFloat($("budget")?.value) || 0;
    const spent = items.reduce((sum, item) => sum + item.price, 0);
    const progress = budget > 0 ? Math.min((spent / budget) * 100, 100) : 0;

    const progressBar = $("budgetProgress");
    const label = $("budgetProgressLabel");

    if (label) {
      if (budget > 0) {
        label.textContent = `Cart total: ₹${spent} / Budget: ₹${budget} (${progress.toFixed(0)}%)`;
      } else {
        label.textContent = `Enter a budget to track spending`;
      }
    }

    if (!progressBar) return;

    progressBar.style.width = `${progress}%`;
    progressBar.className = "progress-bar";

    if (progress <= 60) {
      progressBar.classList.add("bg-success");
    } else if (progress <= 90) {
      progressBar.classList.add("bg-warning");
    } else {
      progressBar.classList.add("bg-danger");
    }
  }

  // Optimize Cart via Java 21 Backend
  const optimizeBtn = $("optimizeBtn");
  if (optimizeBtn) {
    optimizeBtn.addEventListener("click", async () => {
      const budgetVal = $("budget")?.value.trim();
      const budget = parseInt(budgetVal, 10);

      if (isNaN(budget) || budget <= 0) {
        showToast("⚠️ Please enter a valid budget greater than ₹0.", "warning");
        $("budget")?.focus();
        return;
      }

      if (!items.length) {
        showToast("🛒 Your cart is empty! Add items first.", "warning");
        $("itemName")?.focus();
        return;
      }

      optimizeBtn.disabled = true;
      const originalText = optimizeBtn.innerHTML;
      optimizeBtn.innerHTML = `<span>⏳ Optimizing with 0/1 Knapsack DP...</span>`;

      try {
        const res = await fetch(`${API_BASE}/optimize`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ budget, items })
        });

        if (!res.ok) {
          const err = await res.json().catch(() => ({ error: "Server returned error " + res.status }));
          throw new Error(err.error || "Optimization request failed");
        }

        const data = await res.json();
        displayResults(data.selectedItems, data.totalPrice, data.totalValue, data.sessionId, budget);
        saveHistory(data.selectedItems, budget, data.totalPrice, data.totalValue, data.sessionId);
        showToast("🎯 Optimization complete! Best items selected.", "success");
      } catch (err) {
        console.error("Optimization failed:", err);
        showToast(`❌ ${err.message}`, "danger");
      } finally {
        optimizeBtn.disabled = false;
        optimizeBtn.innerHTML = originalText;
      }
    });
  }

  function displayResults(selected, totalPrice, totalValue, sessionId, budget) {
    const resultSection = $("resultSection");
    const resultItems = $("resultItems");
    if (!resultItems || !resultSection) return;

    resultItems.innerHTML = "";
    if (!selected || selected.length === 0) {
      resultItems.innerHTML = `<li class="list-group-item text-danger py-3 text-center">❌ None of the items fit within the ₹${budget} budget. Consider increasing your budget.</li>`;
    } else {
      selected.forEach((item) => {
        const li = createEl("li", "list-group-item item-slide-in");
        li.innerHTML = `
          <div class="d-flex align-items-center gap-3">
            <strong>${escapeHtml(item.name)}</strong>
            <span class="text-muted">₹${item.price}</span>
            ${getImportanceBadge(item.value)}
          </div>
          <span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1 rounded">Selected</span>
        `;
        resultItems.appendChild(li);
      });
    }

    const savedMoney = Math.max(0, budget - totalPrice);
    if ($("totalPrice")) $("totalPrice").textContent = `₹${totalPrice.toLocaleString()}`;
    if ($("totalValue")) $("totalValue").textContent = `${totalValue}`;
    if ($("itemCount")) $("itemCount").textContent = `${selected.length} of ${items.length}`;
    if ($("remainingBudget")) $("remainingBudget").textContent = `₹${savedMoney.toLocaleString()}`;
    if ($("sessionId")) $("sessionId").textContent = `#${sessionId || "N/A"}`;

    resultSection.classList.remove("d-none");
    resultSection.scrollIntoView({ behavior: "smooth", block: "nearest" });
  }

  // Copy Summary to Clipboard
  const copySummaryBtn = $("copySummaryBtn");
  if (copySummaryBtn) {
    copySummaryBtn.addEventListener("click", () => {
      const price = $("totalPrice")?.textContent || "0";
      const value = $("totalValue")?.textContent || "0";
      const count = $("itemCount")?.textContent || "0";
      const session = $("sessionId")?.textContent || "";
      const text = `KIOTY Cart Optimization Result (${session})\n- Items Selected: ${count}\n- Total Cost: ${price}\n- Total Importance: ${value}\nOptimized via KIOTY 0/1 Knapsack Optimizer`;

      navigator.clipboard.writeText(text).then(() => {
        showToast("📋 Summary copied to clipboard!", "info");
      }).catch(() => {
        showToast("Failed to copy summary.", "warning");
      });
    });
  }

  function saveHistory(selectedItems, budget, totalPrice, totalValue, sessionId) {
    let history = [];
    try {
      history = JSON.parse(localStorage.getItem(HISTORY_KEY)) || [];
    } catch (e) {
      history = [];
    }

    history.unshift({
      sessionId: sessionId || Math.random().toString(36).slice(2, 8),
      date: new Date().toLocaleString(),
      budget,
      items: selectedItems,
      totalPrice,
      totalValue,
      count: selectedItems.length
    });

    if (history.length > 50) history = history.slice(0, 50);

    try {
      localStorage.setItem(HISTORY_KEY, JSON.stringify(history));
    } catch (e) {
      console.warn("Failed to save history to localStorage:", e);
    }
  }

  function showToast(message, type = "success") {
    const existingToasts = document.querySelectorAll(".toast-message");
    existingToasts.forEach((t) => t.remove());

    const toast = createEl("div", `toast-message bg-${type}`);
    toast.textContent = message;
    document.body.appendChild(toast);

    setTimeout(() => toast.classList.add("show"), 10);
    setTimeout(() => {
      toast.classList.remove("show");
      setTimeout(() => toast.remove(), 350);
    }, 2800);
  }

  function escapeHtml(str) {
    if (!str) return "";
    return str
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#039;");
  }
});
