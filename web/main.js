const { createApp, ref, computed, onMounted, nextTick } = Vue;

const API_BASE = "http://localhost:8080";

createApp({
  setup() {
    const prizes = ref([]);
    const records = ref([]);
    const name = ref("");
    const result = ref("准备就绪");
    const spinning = ref(false);
    const wheelCanvas = ref(null);
    let currentRotation = 0;

    const authVisible = ref(false);
    const authMode = ref("login");
    const authUsername = ref("");
    const authPassword = ref("");
    const authTip = ref("");
    const authToken = ref(localStorage.getItem("lottery_token") || "");
    const authUser = ref(localStorage.getItem("lottery_user") || "");

    const isAuthed = computed(() => !!authToken.value);

    const participants = computed(() => {
      const set = new Set(records.value.map((r) => r.name));
      return set.size;
    });

    const recordCount = computed(() => records.value.length);

    async function fetchJson(path, options) {
      const res = await fetch(API_BASE + path, options);
      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || "请求失败");
      }
      return res.json();
    }

    function drawWheel() {
      const canvas = wheelCanvas.value;
      if (!canvas) {
        return;
      }
      const ctx = canvas.getContext("2d");
      const size = canvas.width;
      const radius = size / 2;
      ctx.clearRect(0, 0, size, size);

      if (!prizes.value || prizes.value.length === 0) {
        ctx.fillStyle = "#dfe7ef";
        ctx.beginPath();
        ctx.arc(radius, radius, radius - 6, 0, Math.PI * 2);
        ctx.fill();
        return;
      }

      const segmentAngle = (Math.PI * 2) / prizes.value.length;
      let angle = -Math.PI / 2;

      prizes.value.forEach((prize, index) => {
        const isEven = index % 2 === 0;
        ctx.beginPath();
        ctx.moveTo(radius, radius);
        ctx.fillStyle = prize.color || (isEven ? "#f6c453" : "#f0b13b");
        ctx.arc(radius, radius, radius - 32, angle, angle + segmentAngle);
        ctx.closePath();
        ctx.fill();

        const mid = angle + segmentAngle / 2;
        ctx.save();
        ctx.translate(radius, radius);
        ctx.rotate(mid);
        ctx.textAlign = "right";
        ctx.fillStyle = "#7a4b00";
        ctx.font = "600 15px 'Space Grotesk', 'Noto Sans SC', sans-serif";
        ctx.fillText(prize.name, radius - 58, 5);
        ctx.restore();

        angle += segmentAngle;
      });

      ctx.beginPath();
      ctx.arc(radius, radius, radius - 16, 0, Math.PI * 2);
      ctx.strokeStyle = "#6d3bd2";
      ctx.lineWidth = 12;
      ctx.stroke();

      const dots = 24;
      const dotRadius = radius - 16;
      for (let i = 0; i < dots; i++) {
        const dotAngle = (Math.PI * 2 * i) / dots;
        const x = radius + Math.cos(dotAngle) * dotRadius;
        const y = radius + Math.sin(dotAngle) * dotRadius;
        ctx.beginPath();
        ctx.fillStyle = i % 3 === 0 ? "#45d2ff" : i % 3 === 1 ? "#ffd166" : "#ff6b6b";
        ctx.arc(x, y, 6, 0, Math.PI * 2);
        ctx.fill();
        ctx.strokeStyle = "rgba(255,255,255,0.7)";
        ctx.lineWidth = 2;
        ctx.stroke();
      }

      ctx.beginPath();
      ctx.arc(radius, radius, 18, 0, Math.PI * 2);
      ctx.fillStyle = "#c04b2a";
      ctx.fill();
      ctx.strokeStyle = "rgba(0,0,0,0.15)";
      ctx.lineWidth = 3;
      ctx.stroke();
    }

    function setWheelRotation(deg) {
      const canvas = wheelCanvas.value;
      if (!canvas) {
        return;
      }
      canvas.style.transform = "rotate(" + deg + "deg)";
    }

    function spinToPrize(prizeId) {
      const index = prizes.value.findIndex((p) => p.id === prizeId);
      if (index === -1 || prizes.value.length === 0) {
        return;
      }
      const segment = 360 / prizes.value.length;
      const target = 360 - (index * segment + segment / 2);
      const base = currentRotation % 360;
      const delta = 360 * 5 + target - base;
      currentRotation += delta;
      setWheelRotation(currentRotation);
    }

    async function loadPrizes() {
      prizes.value = await fetchJson("/api/prizes");
      await nextTick();
      drawWheel();
    }

    async function loadRecords() {
      records.value = await fetchJson("/api/records");
    }

    async function draw() {
      const trimmed = name.value.trim();
      if (!trimmed) {
        result.value = "请输入姓名";
        return;
      }
      if (!isAuthed.value) {
        result.value = "请先登录";
        openAuth("login");
        return;
      }
      if (spinning.value) {
        return;
      }
      spinning.value = true;
      result.value = "抽奖中...";

      try {
        const res = await fetchJson("/api/draw", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer " + authToken.value
          },
          body: JSON.stringify({ name: trimmed })
        });

        spinToPrize(res.prize.id);

        setTimeout(async () => {
          result.value = res.record.name + " 抽中 " + res.record.prizeName;
          await loadRecords();
          spinning.value = false;
        }, 4200);
      } catch (err) {
        result.value = "抽奖失败，请重新登录";
        spinning.value = false;
        logout();
      }
    }

    function formatTime(time) {
      return new Date(time).toLocaleString();
    }

    function openAuth(mode) {
      authMode.value = mode;
      authVisible.value = true;
      authTip.value = "";
    }

    function closeAuth() {
      authVisible.value = false;
    }

    function toggleAuth() {
      authMode.value = authMode.value === "login" ? "register" : "login";
      authTip.value = "";
    }

    async function submitAuth() {
      const username = authUsername.value.trim();
      const password = authPassword.value.trim();
      if (!username || !password) {
        authTip.value = "请填写完整信息";
        return;
      }
      const path = authMode.value === "login" ? "/api/auth/login" : "/api/auth/register";
      try {
        const res = await fetchJson(path, {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({ username, password })
        });
        authToken.value = res.token;
        authUser.value = res.username;
        localStorage.setItem("lottery_token", authToken.value);
        localStorage.setItem("lottery_user", authUser.value);
        authVisible.value = false;
        authPassword.value = "";
        authTip.value = "";
      } catch (err) {
        authTip.value = err.message || "操作失败";
      }
    }

    function logout() {
      authToken.value = "";
      authUser.value = "";
      localStorage.removeItem("lottery_token");
      localStorage.removeItem("lottery_user");
    }

    onMounted(async () => {
      await loadPrizes();
      await loadRecords();
    });

    return {
      prizes,
      records,
      name,
      result,
      spinning,
      participants,
      recordCount,
      wheelCanvas,
      draw,
      loadRecords,
      formatTime,
      authVisible,
      authMode,
      authUsername,
      authPassword,
      authTip,
      authUser,
      isAuthed,
      openAuth,
      closeAuth,
      toggleAuth,
      submitAuth,
      logout
    };
  }
}).mount("#app");
