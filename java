// ===============================
// UTILIDADES GENERALES
// ===============================
function toHz(value, unit) {
  if (value === "" || value === null || isNaN(value)) return null;
  return unit === "kHz" ? Number(value) * 1000 : Number(value);
}

function toSeconds(value, unit) {
  if (value === "" || value === null || isNaN(value)) return null;
  return unit === "ms" ? Number(value) / 1000 : Number(value);
}

function toMeters(value, unit) {
  if (value === "" || value === null || isNaN(value)) return null;
  return unit === "cm" ? Number(value) / 100 : Number(value);
}

function formatNumber(num, decimals = 6) {
  return Number(num).toFixed(decimals).replace(/\.?0+$/, "");
}

function getSpeedFromTemp(tempValue) {
  if (tempValue === "" || tempValue === null || isNaN(tempValue)) {
    return {
      speed: 340,
      mode: "default"
    };
  }

  const t = Number(tempValue);
  return {
    speed: 331.4 + (0.607 * t),
    mode: "temperature",
    temperature: t
  };
}

function normalizeDegrees(deg) {
  let d = deg % 360;
  if (d < 0) d += 360;
  return d;
}

function classifyPhase(deg) {
  const d = normalizeDegrees(deg);

  const near = (a, b, tol = 10) => Math.abs(a - b) <= tol;

  if (near(d, 0) || near(d, 360)) return "Suma máxima aproximada (+6 dB)";
  if (near(d, 180)) return "Cancelación máxima";
  if (near(d, 90) || near(d, 270)) return "Suma parcial aproximada (+3 dB)";
  if ((d > 90 && d < 180) || (d > 180 && d < 270)) return "Tendencia a cancelación parcial";
  return "Tendencia a suma parcial";
}

function safeSetHTML(id, html) {
  document.getElementById(id).innerHTML = html;
}

function safeSetText(id, text) {
  document.getElementById(id).textContent = text;
}

function speedExplanationHTML(speedData) {
  if (speedData.mode === "temperature") {
    return `
      Como sí hay temperatura, usamos <code>c = 331.4 + (0.607 × T)</code>.<br>
      c = 331.4 + (0.607 × ${formatNumber(speedData.temperature, 3)}) = <strong>${formatNumber(speedData.speed, 6)} m/s</strong>
    `;
  }

  return `
    Como no hay temperatura, usamos <strong>c = 340 m/s</strong>.
  `;
}

// ===============================
// CALCULADORAS
// ===============================
document.getElementById("calcPeriod").addEventListener("click", () => {
  const f = toHz(
    document.getElementById("periodFreqInput").value,
    document.getElementById("periodFreqUnit").value
  );

  if (!f || f <= 0) {
    safeSetText("periodOutput", "Resultado: ingresa una frecuencia válida");
    safeSetHTML("periodSteps", "");
    return;
  }

  const T = 1 / f;
  const Tms = T * 1000;

  safeSetText("periodOutput", `Resultado: ${formatNumber(Tms, 6)} ms (${formatNumber(T, 9)} s)`);

  safeSetHTML("periodSteps", `
    <strong>Paso a paso:</strong><br>
    1. Convertimos la frecuencia a Hz: <strong>${formatNumber(f, 3)} Hz</strong><br>
    2. Aplicamos la fórmula del período: <code>T = 1 / f</code><br>
    3. T = 1 / ${formatNumber(f, 3)} = <strong>${formatNumber(T, 9)} s</strong><br>
    4. Convertimos a milisegundos: ${formatNumber(T, 9)} × 1000 = <strong>${formatNumber(Tms, 6)} ms</strong>
  `);
});

document.getElementById("calcFrequency").addEventListener("click", () => {
  const T = toSeconds(
    document.getElementById("freqPeriodInput").value,
    document.getElementById("freqPeriodUnit").value
  );

  if (!T || T <= 0) {
    safeSetText("freqOutput", "Resultado: ingresa un período válido");
    safeSetHTML("freqSteps", "");
    return;
  }

  const f = 1 / T;

  safeSetText("freqOutput", `Resultado: ${formatNumber(f, 6)} Hz`);

  safeSetHTML("freqSteps", `
    <strong>Paso a paso:</strong><br>
    1. Convertimos el período a segundos: <strong>${formatNumber(T, 9)} s</strong><br>
    2. Aplicamos la fórmula de frecuencia: <code>f = 1 / T</code><br>
    3. f = 1 / ${formatNumber(T, 9)} = <strong>${formatNumber(f, 6)} Hz</strong>
  `);
});

document.getElementById("calcLambda").addEventListener("click", () => {
  const f = toHz(
    document.getElementById("lambdaFreqInput").value,
    document.getElementById("lambdaFreqUnit").value
  );
  const tempValue = document.getElementById("lambdaTempInput").value;
  const speedData = getSpeedFromTemp(tempValue);

  if (!f || f <= 0) {
    safeSetText("lambdaOutput", "Resultado: ingresa una frecuencia válida");
    safeSetHTML("lambdaSteps", "");
    return;
  }

  const lambda = speedData.speed / f;
  const lambdaCm = lambda * 100;

  safeSetText("lambdaOutput", `Resultado: ${formatNumber(lambda, 6)} m (${formatNumber(lambdaCm, 4)} cm)`);

  safeSetHTML("lambdaSteps", `
    <strong>Paso a paso:</strong><br>
    1. Frecuencia en Hz: <strong>${formatNumber(f, 3)} Hz</strong><br>
    2. ${speedExplanationHTML(speedData)}<br>
    3. Aplicamos la fórmula: <code>λ = c / f</code><br>
    4. λ = ${formatNumber(speedData.speed, 6)} / ${formatNumber(f, 3)} = <strong>${formatNumber(lambda, 6)} m</strong><br>
    5. En centímetros: ${formatNumber(lambda, 6)} × 100 = <strong>${formatNumber(lambdaCm, 4)} cm</strong>
  `);
});

document.getElementById("calcSpeed").addEventListener("click", () => {
  const tempValue = document.getElementById("speedTempInput").value;

  if (tempValue === "" || isNaN(tempValue)) {
    safeSetText("speedOutput", "Resultado: ingresa una temperatura válida");
    safeSetHTML("speedSteps", "");
    return;
  }

  const speed = 331.4 + (0.607 * Number(tempValue));

  safeSetText("speedOutput", `Resultado: ${formatNumber(speed, 6)} m/s`);

  safeSetHTML("speedSteps", `
    <strong>Paso a paso:</strong><br>
    1. Usamos la fórmula: <code>c = 331.4 + (0.607 × T)</code><br>
    2. c = 331.4 + (0.607 × ${formatNumber(tempValue, 3)})<br>
    3. c = <strong>${formatNumber(speed, 6)} m/s</strong>
  `);
});

document.getElementById("calcDegTime").addEventListener("click", () => {
  const f = toHz(
    document.getElementById("degTimeFreqInput").value,
    document.getElementById("degTimeFreqUnit").value
  );
  const deltaT = toSeconds(
    document.getElementById("degTimeInput").value,
    document.getElementById("degTimeUnit").value
  );

  if (!f || f <= 0 || !deltaT || deltaT < 0) {
    safeSetText("degTimeOutput", "Resultado: ingresa frecuencia y tiempo válidos");
    safeSetHTML("degTimeSteps", "");
    return;
  }

  const T = 1 / f;
  const degrees = (deltaT / T) * 360;
  const degNorm = normalizeDegrees(degrees);

  safeSetText("degTimeOutput", `Resultado: ${formatNumber(degrees, 4)}° (equivale a ${formatNumber(degNorm, 4)}° dentro del ciclo)`);

  safeSetHTML("degTimeSteps", `
    <strong>Paso a paso:</strong><br>
    1. Frecuencia en Hz: <strong>${formatNumber(f, 3)} Hz</strong><br>
    2. Hallamos el período: <code>T = 1 / f</code><br>
    3. T = 1 / ${formatNumber(f, 3)} = <strong>${formatNumber(T, 9)} s</strong><br>
    4. Diferencia de tiempo: <strong>${formatNumber(deltaT, 9)} s</strong><br>
    5. Fórmula de grados con tiempo: <code>° = (Δt / T) × 360</code><br>
    6. ° = (${formatNumber(deltaT, 9)} / ${formatNumber(T, 9)}) × 360 = <strong>${formatNumber(degrees, 4)}°</strong><br>
    7. Interpretación: <strong>${classifyPhase(degrees)}</strong>
  `);
});

document.getElementById("calcDegDistance").addEventListener("click", () => {
  const f = toHz(
    document.getElementById("degDistFreqInput").value,
    document.getElementById("degDistFreqUnit").value
  );
  const d = toMeters(
    document.getElementById("degDistInput").value,
    document.getElementById("degDistUnit").value
  );
  const tempValue = document.getElementById("degDistTempInput").value;
  const speedData = getSpeedFromTemp(tempValue);

  if (!f || f <= 0 || !d || d < 0) {
    safeSetText("degDistOutput", "Resultado: ingresa frecuencia y distancia válidas");
    safeSetHTML("degDistSteps", "");
    return;
  }

  const lambda = speedData.speed / f;
  const degrees = (d / lambda) * 360;
  const degNorm = normalizeDegrees(degrees);

  safeSetText("degDistOutput", `Resultado: ${formatNumber(degrees, 4)}° (equivale a ${formatNumber(degNorm, 4)}° dentro del ciclo)`);

  safeSetHTML("degDistSteps", `
    <strong>Paso a paso:</strong><br>
    1. Frecuencia en Hz: <strong>${formatNumber(f, 3)} Hz</strong><br>
    2. ${speedExplanationHTML(speedData)}<br>
    3. Longitud de onda: <code>λ = c / f</code><br>
    4. λ = ${formatNumber(speedData.speed, 6)} / ${formatNumber(f, 3)} = <strong>${formatNumber(lambda, 9)} m</strong><br>
    5. Diferencia de distancia: <strong>${formatNumber(d, 9)} m</strong><br>
    6. Fórmula de grados con distancia: <code>° = (Δd / λ) × 360</code><br>
    7. ° = (${formatNumber(d, 9)} / ${formatNumber(lambda, 9)}) × 360 = <strong>${formatNumber(degrees, 4)}°</strong><br>
    8. Interpretación: <strong>${classifyPhase(degrees)}</strong>
  `);
});

// ===============================
// SIMULADOR DE FASE
// ===============================
const canvas = document.getElementById("waveCanvas");
const ctx = canvas.getContext("2d");
const phaseBarFill = document.getElementById("phaseBarFill");
const phaseValue = document.getElementById("phaseValue");
const phaseEffect = document.getElementById("phaseEffect");

function drawWaveScene(degrees) {
  const w = canvas.width;
  const h = canvas.height;
  ctx.clearRect(0, 0, w, h);

  ctx.fillStyle = "rgba(255,255,255,0.03)";
  ctx.fillRect(0, 0, w, h);

  ctx.strokeStyle = "rgba(255,255,255,0.08)";
  ctx.lineWidth = 1;
  for (let i = 1; i < 4; i++) {
    const y = (h / 4) * i;
    ctx.beginPath();
    ctx.moveTo(0, y);
    ctx.lineTo(w, y);
    ctx.stroke();
  }

  ctx.strokeStyle = "rgba(255,255,255,0.18)";
  ctx.beginPath();
  ctx.moveTo(0, h / 2);
  ctx.lineTo(w, h / 2);
  ctx.stroke();

  const cycles = 2.2;
  const amplitude = 52;
  const shift = (degrees * Math.PI) / 180;

  ctx.strokeStyle = "#45c4ff";
  ctx.lineWidth = 3;
  ctx.beginPath();
  for (let x = 0; x <= w; x++) {
    const t = (x / w) * Math.PI * 2 * cycles;
    const y = h / 2 + Math.sin(t) * amplitude;
    if (x === 0) ctx.moveTo(x, y);
    else ctx.lineTo(x, y);
  }
  ctx.stroke();

  ctx.strokeStyle = "#ffd166";
  ctx.lineWidth = 3;
  ctx.beginPath();
  for (let x = 0; x <= w; x++) {
    const t = (x / w) * Math.PI * 2 * cycles;
    const y = h / 2 + Math.sin(t + shift) * amplitude;
    if (x === 0) ctx.moveTo(x, y);
    else ctx.lineTo(x, y);
  }
  ctx.stroke();

  ctx.fillStyle = "rgba(237,246,255,0.9)";
  ctx.font = "14px Inter, Arial";
  ctx.fillText("Onda 1", 14, 24);
  ctx.fillStyle = "#45c4ff";
  ctx.fillRect(72, 14, 34, 4);

  ctx.fillStyle = "rgba(237,246,255,0.9)";
  ctx.fillText("Onda 2", 130, 24);
  ctx.fillStyle = "#ffd166";
  ctx.fillRect(188, 14, 34, 4);
}

function updatePhaseDisplay(degrees) {
  const normalized = normalizeDegrees(degrees);
  phaseValue.textContent = `${formatNumber(degrees, 3)}°`;
  phaseEffect.textContent = classifyPhase(degrees);
  phaseBarFill.style.width = `${(normalized / 360) * 100}%`;
  drawWaveScene(normalized);
}

document.getElementById("simulateByTime").addEventListener("click", () => {
  const f = toHz(
    document.getElementById("simFreqInput").value,
    document.getElementById("simFreqUnit").value
  );
  const deltaT = toSeconds(
    document.getElementById("simTimeInput").value,
    document.getElementById("simTimeUnit").value
  );

  if (!f || f <= 0 || !deltaT || deltaT < 0) {
    safeSetText("simOutput", "Resultado: ingresa frecuencia y tiempo válidos");
    safeSetHTML("simSteps", "");
    return;
  }

  const T = 1 / f;
  const degrees = (deltaT / T) * 360;

  safeSetText("simOutput", `Resultado: ${formatNumber(degrees, 4)}° → ${classifyPhase(degrees)}`);

  safeSetHTML("simSteps", `
    <strong>Paso a paso:</strong><br>
    1. Hallamos el período: <code>T = 1 / f</code><br>
    2. T = 1 / ${formatNumber(f, 3)} = <strong>${formatNumber(T, 9)} s</strong><br>
    3. Usamos la fórmula de grados con tiempo: <code>° = (Δt / T) × 360</code><br>
    4. ° = (${formatNumber(deltaT, 9)} / ${formatNumber(T, 9)}) × 360 = <strong>${formatNumber(degrees, 4)}°</strong><br>
    5. Efecto esperado: <strong>${classifyPhase(degrees)}</strong>
  `);

  updatePhaseDisplay(degrees);
});

document.getElementById("simulateByDistance").addEventListener("click", () => {
  const f = toHz(
    document.getElementById("simFreqInput").value,
    document.getElementById("simFreqUnit").value
  );
  const d = toMeters(
    document.getElementById("simDistInput").value,
    document.getElementById("simDistUnit").value
  );
  const tempValue = document.getElementById("simTempInput").value;
  const speedData = getSpeedFromTemp(tempValue);

  if (!f || f <= 0 || !d || d < 0) {
    safeSetText("simOutput", "Resultado: ingresa frecuencia y distancia válidas");
    safeSetHTML("simSteps", "");
    return;
  }

  const lambda = speedData.speed / f;
  const degrees = (d / lambda) * 360;

  safeSetText("simOutput", `Resultado: ${formatNumber(degrees, 4)}° → ${classifyPhase(degrees)}`);

  safeSetHTML("simSteps", `
    <strong>Paso a paso:</strong><br>
    1. ${speedExplanationHTML(speedData)}<br>
    2. Hallamos la longitud de onda: <code>λ = c / f</code><br>
    3. λ = ${formatNumber(speedData.speed, 6)} / ${formatNumber(f, 3)} = <strong>${formatNumber(lambda, 9)} m</strong><br>
    4. Aplicamos la fórmula de grados con distancia: <code>° = (Δd / λ) × 360</code><br>
    5. ° = (${formatNumber(d, 9)} / ${formatNumber(lambda, 9)}) × 360 = <strong>${formatNumber(degrees, 4)}°</strong><br>
    6. Efecto esperado: <strong>${classifyPhase(degrees)}</strong>
  `);

  updatePhaseDisplay(degrees);
});

updatePhaseDisplay(0);

// ===============================
// DATOS DE EJERCICIOS
// ===============================
const exercises = [
  {
    id: 1,
    topic: "Período",
    title: "Ejercicio 1",
    question: "El período de 9 kHz es:",
    options: ["0,000111 ms", "0,111 ms", "111 ms", "0,011 s"],
    correctIndex: 1,
    explanation: `
      <strong>Respuesta correcta: B. 0,111 ms</strong><br><br>
      <strong>Desarrollo paso a paso:</strong><br>
      1. Convertimos 9 kHz a Hz: <strong>9 kHz = 9000 Hz</strong><br>
      2. Aplicamos la fórmula del período: <code>T = 1 / f</code><br>
      3. T = 1 / 9000 = <strong>0,000111 s</strong><br>
      4. Pasamos a milisegundos: 0,000111 × 1000 = <strong>0,111 ms</strong><br>
      5. Por eso la alternativa correcta es <strong>0,111 ms</strong>.
    `
  },
  {
    id: 2,
    topic: "Longitud de onda",
    title: "Ejercicio 2",
    question: "La longitud de onda de 3 kHz es:",
    options: ["11,33 cm", "1,13 cm", "0,113 cm", "113 cm"],
    correctIndex: 0,
    explanation: `
      <strong>Respuesta correcta: A. 11,33 cm</strong><br><br>
      <strong>Desarrollo paso a paso:</strong><br>
      1. Como no se da temperatura, usamos <strong>c = 340 m/s</strong><br>
      2. Convertimos 3 kHz a Hz: <strong>3000 Hz</strong><br>
      3. Aplicamos la fórmula: <code>λ = c / f</code><br>
      4. λ = 340 / 3000 = <strong>0,1133 m</strong><br>
      5. Pasamos a centímetros: 0,1133 × 100 = <strong>11,33 cm</strong><br>
      6. La alternativa correcta es <strong>11,33 cm</strong>.
    `
  },
  {
    id: 3,
    topic: "Período",
    title: "Ejercicio 3",
    question: "El período de 20 kHz es:",
    options: ["0,05 ms", "0,5 ms", "5 ms", "0,0005 s"],
    correctIndex: 0,
    explanation: `
      <strong>Respuesta correcta: A. 0,05 ms</strong><br><br>
      <strong>Desarrollo paso a paso:</strong><br>
      1. 20 kHz = <strong>20000 Hz</strong><br>
      2. Aplicamos <code>T = 1 / f</code><br>
      3. T = 1 / 20000 = <strong>0,00005 s</strong><br>
      4. En milisegundos: 0,00005 × 1000 = <strong>0,05 ms</strong><br>
      5. La respuesta correcta es <strong>0,05 ms</strong>.
    `
  },
  {
    id: 4,
    topic: "Velocidad del sonido",
    title: "Ejercicio 4",
    question: "La velocidad del sonido a 40°C corresponde a:",
    options: ["336,256 m/s", "343,54 m/s", "355,68 m/s", "358,715 m/s"],
    correctIndex: 2,
    explanation: `
      <strong>Respuesta correcta: C. 355,68 m/s</strong><br><br>
      <strong>Desarrollo paso a paso:</strong><br>
      1. Como sí hay temperatura, usamos: <code>c = 331.4 + (0.607 × T)</code><br>
      2. c = 331.4 + (0.607 × 40)<br>
      3. c = 331.4 + 24.28<br>
      4. c = <strong>355,68 m/s</strong><br>
      5. La alternativa correcta es <strong>355,68 m/s</strong>.
    `
  },
  {
    id: 5,
    topic: "Longitud de onda",
    title: "Ejercicio 5",
    question: "¿Cuál es la longitud de onda de 6 kHz a la temperatura de 20°C?",
    options: ["5,7256 cm", "56 cm", "0,56 cm", "0,056 cm"],
    correctIndex: 0,
    explanation: `
      <strong>Respuesta correcta: A. 5,7256 cm</strong><br><br>
      <strong>Desarrollo paso a paso con la corrección aplicada:</strong><br>
      1. Como sí hay temperatura, usamos <code>c = 331.4 + (0.607 × T)</code><br>
      2. c = 331.4 + (0.607 × 20) = <strong>343,54 m/s</strong><br>
      3. 6 kHz = <strong>6000 Hz</strong><br>
      4. Aplicamos la fórmula: <code>λ = c / f</code><br>
      5. λ = 343,54 / 6000 = <strong>0,057256 m</strong><br>
      6. En centímetros: 0,057256 × 100 = <strong>5,7256 cm</strong><br>
      7. Por lo tanto, la alternativa correcta es <strong>A. 5,7256 cm</strong>.
    `
  },
  {
    id: 6,
    topic: "Frecuencia",
    title: "Ejercicio 6",
    question: "El período de 0,4 ms corresponde a la frecuencia de:",
    options: ["25 Hz", "250 Hz", "2500 Hz", "4000 Hz"],
    correctIndex: 2,
    explanation: `
      <strong>Respuesta correcta: C. 2500 Hz</strong><br><br>
      <strong>Desarrollo paso a paso:</strong><br>
      1. Convertimos el período a segundos: 0,4 ms = <strong>0,0004 s</strong><br>
      2. Aplicamos la fórmula de frecuencia: <code>f = 1 / T</code><br>
      3. f = 1 / 0,0004 = <strong>2500 Hz</strong><br>
      4. La alternativa correcta es <strong>2500 Hz</strong>.
    `
  },
  {
    id: 7,
    topic: "Período y longitud de onda",
    title: "Ejercicio 7",
    question: "El período y longitud de onda de 7,5 kHz a 34°C es:",
    options: [
      "0,133 ms y 4,69 cm",
      "1,33 ms y 4,69 cm",
      "0,133 ms y 0,046 m",
      "0,000133 ms y 0,046 m"
    ],
    correctIndex: 0,
    explanation: `
      <strong>Respuesta correcta: A. 0,133 ms y 4,69 cm</strong><br><br>
      <strong>Desarrollo paso a paso:</strong><br>
      1. 7,5 kHz = <strong>7500 Hz</strong><br>
      2. Período: <code>T = 1 / f</code> = 1 / 7500 = <strong>0,000133 s</strong> = <strong>0,133 ms</strong><br>
      3. Como sí hay temperatura, usamos: <code>c = 331.4 + (0.607 × 34)</code><br>
      4. c = 331.4 + 20.638 = <strong>352,038 m/s</strong><br>
      5. Longitud de onda: <code>λ = c / f</code><br>
      6. λ = 352,038 / 7500 = <strong>0,0469384 m</strong><br>
      7. En centímetros: 0,0469384 × 100 = <strong>4,69384 cm</strong><br>
      8. Aproximando, la respuesta es <strong>4,69 cm</strong><br>
      9. Por eso la alternativa correcta es <strong>0,133 ms y 4,69 cm</strong>.
    `
  },
  {
    id: 8,
    topic: "Grados con distancia",
    title: "Ejercicio 8",
    question: "¿Cuántos grados recorre la frecuencia de 500 Hz en 51 cm?",
    options: ["180°", "270°", "360°", "540°"],
    correctIndex: 1,
    explanation: `
      <strong>Respuesta correcta: B. 270°</strong><br><br>
      <strong>Desarrollo paso a paso:</strong><br>
      1. Como no hay temperatura, usamos <strong>340 m/s</strong><br>
      2. Hallamos la longitud de onda: <code>λ = c / f</code><br>
      3. λ = 340 / 500 = <strong>0,68 m</strong> = <strong>68 cm</strong><br>
      4. Fórmula de grados con distancia: <code>° = (Δd / λ) × 360</code><br>
      5. ° = (51 / 68) × 360 = <strong>270°</strong><br>
      6. La alternativa correcta es <strong>270°</strong>.
    `
  },
  {
    id: 9,
    topic: "Grados con tiempo",
    title: "Ejercicio 9",
    question: "¿Cuántos grados recorre la frecuencia de 2 kHz en 0,125 ms?",
    options: ["45°", "90°", "180°", "360°"],
    correctIndex: 1,
    explanation: `
      <strong>Respuesta correcta: B. 90°</strong><br><br>
      <strong>Desarrollo paso a paso:</strong><br>
      1. 2 kHz = <strong>2000 Hz</strong><br>
      2. Hallamos el período: <code>T = 1 / f</code><br>
      3. T = 1 / 2000 = <strong>0,0005 s</strong> = <strong>0,5 ms</strong><br>
      4. Fórmula de grados con tiempo: <code>° = (Δt / T) × 360</code><br>
      5. ° = (0,125 / 0,5) × 360 = <strong>90°</strong><br>
      6. La alternativa correcta es <strong>90°</strong>.
    `
  },
  {
    id: 10,
    topic: "Cancelación por distancia",
    title: "Ejercicio 10",
    question: "Para cancelar la frecuencia de 12 kHz, ¿a qué distancia tiene que estar una onda correlacionada respecto a la otra?",
    options: ["1,4 cm", "2,8 cm", "5,6 cm", "0,7 cm"],
    correctIndex: 0,
    explanation: `
      <strong>Respuesta correcta: A. 1,4 cm</strong><br><br>
      <strong>Desarrollo paso a paso:</strong><br>
      1. Como no hay temperatura, usamos <strong>340 m/s</strong><br>
      2. 12 kHz = <strong>12000 Hz</strong><br>
      3. Longitud de onda: λ = 340 / 12000 = <strong>0,02833 m</strong> = <strong>2,833 cm</strong><br>
      4. Para cancelación máxima necesitamos <strong>180°</strong>, o sea media longitud de onda<br>
      5. Distancia = 2,833 / 2 = <strong>1,416 cm</strong><br>
      6. Aproximando, la respuesta es <strong>1,4 cm</strong>.
    `
  },
  {
    id: 11,
    topic: "Cancelación por tiempo",
    title: "Ejercicio 11",
    question: "Para cancelar la frecuencia de 3,5 kHz, ¿cuánto tiempo tiene que recorrer una onda correlacionada respecto a la otra?",
    options: ["0,071 ms", "0,142 ms", "0,285 ms", "0,035 ms"],
    correctIndex: 1,
    explanation: `
      <strong>Respuesta correcta: B. 0,142 ms</strong><br><br>
      <strong>Desarrollo paso a paso:</strong><br>
      1. 3,5 kHz = <strong>3500 Hz</strong><br>
      2. Hallamos el período: <code>T = 1 / f</code><br>
      3. T = 1 / 3500 = <strong>0,0002857 s</strong> = <strong>0,2857 ms</strong><br>
      4. Para cancelación máxima usamos <strong>180°</strong>, es decir medio período<br>
      5. Tiempo = 0,2857 / 2 = <strong>0,14285 ms</strong><br>
      6. Aproximando, la respuesta es <strong>0,142 ms</strong>.
    `
  },
  {
    id: 12,
    topic: "Ángulo de cancelación",
    title: "Ejercicio 12",
    question: "¿A qué grados debe estar una onda con respecto a otra para que ocurra una cancelación de -100 dB?",
    options: ["90°", "270°", "180°", "360°"],
    correctIndex: 2,
    explanation: `
      <strong>Respuesta correcta: C. 180°</strong><br><br>
      <strong>Desarrollo paso a paso:</strong><br>
      1. Cuando dos ondas correlacionadas están opuestas en fase, se produce la mayor cancelación posible.<br>
      2. Esa oposición de fase ocurre a <strong>180°</strong>.<br>
      3. Por eso la alternativa correcta es <strong>180°</strong>.
    `
  },
  {
    id: 13,
    topic: "Suma por tiempo",
    title: "Ejercicio 13",
    question: "Para que la frecuencia de 6 kHz tenga una suma de +6 dB, ¿cuánto tiempo tiene que recorrer una onda correlacionada respecto a la otra?",
    options: ["1 ms", "0,083 ms", "0,166 ms", "0,041 ms"],
    correctIndex: 2,
    explanation: `
      <strong>Respuesta correcta: C. 0,166 ms</strong><br><br>
      <strong>Desarrollo paso a paso:</strong><br>
      1. 6 kHz = <strong>6000 Hz</strong><br>
      2. Hallamos el período: <code>T = 1 / f</code><br>
      3. T = 1 / 6000 = <strong>0,0001667 s</strong> = <strong>0,1667 ms</strong><br>
      4. Para una coincidencia de ciclo completo, el valor corresponde a <strong>360°</strong><br>
      5. Entonces el tiempo es un período completo: <strong>0,166 ms</strong><br>
      6. La alternativa correcta es <strong>0,166 ms</strong>.
    `
  },
  {
    id: 14,
    topic: "Suma por distancia",
    title: "Ejercicio 14",
    question: "Para que la frecuencia de 4 kHz tenga una suma de +3 dB, ¿a qué distancia tiene que estar una onda correlacionada respecto a la otra?",
    options: ["6,375 cm", "4,25 cm", "8,5 cm", "0,085 m"],
    correctIndex: 0,
    explanation: `
      <strong>Respuesta correcta: A. 6,375 cm</strong><br><br>
      <strong>Desarrollo paso a paso:</strong><br>
      1. Como no hay temperatura, usamos <strong>340 m/s</strong><br>
      2. 4 kHz = <strong>4000 Hz</strong><br>
      3. λ = 340 / 4000 = <strong>0,085 m</strong> = <strong>8,5 cm</strong><br>
      4. En este ejercicio se trabaja la condición equivalente a <strong>270°</strong><br>
      5. 270° corresponde a <strong>3/4 de la longitud de onda</strong><br>
      6. Distancia = 8,5 × 3/4 = <strong>6,375 cm</strong><br>
      7. La alternativa correcta es <strong>6,375 cm</strong>.
    `
  }
];

// ===============================
// RENDER EJERCICIOS
// ===============================
const exerciseList = document.getElementById("exerciseList");
let score = 0;
let answered = new Set();

function renderExercises() {
  exerciseList.innerHTML = "";

  exercises.forEach((exercise, exerciseIndex) => {
    const optionsHtml = exercise.options
      .map((option, optionIndex) => {
        return `
          <button class="option-btn" data-exercise="${exerciseIndex}" data-option="${optionIndex}">
            ${String.fromCharCode(65 + optionIndex)}. ${option}
          </button>
        `;
      })
      .join("");

    const card = document.createElement("article");
    card.className = "exercise-card glass";
    card.innerHTML = `
      <div class="exercise-head">
        <div>
          <div class="exercise-topic">${exercise.topic}</div>
          <h3 class="exercise-title">${exercise.title}</h3>
        </div>
      </div>

      <p class="exercise-question">${exercise.question}</p>
      <div class="options">${optionsHtml}</div>
      <div class="exercise-feedback" id="feedback-${exerciseIndex}"></div>
    `;

    exerciseList.appendChild(card);
  });

  bindExerciseEvents();
}

function updateScoreUI() {
  const total = exercises.length;
  safeSetText("quizScoreText", `${score} / ${total} correctas`);
  document.getElementById("quizScoreFill").style.width = `${(score / total) * 100}%`;
}

function bindExerciseEvents() {
  document.querySelectorAll(".option-btn").forEach((button) => {
    button.addEventListener("click", () => {
      const exerciseIndex = Number(button.dataset.exercise);
      const selectedOption = Number(button.dataset.option);
      const exercise = exercises[exerciseIndex];
      const feedback = document.getElementById(`feedback-${exerciseIndex}`);

      if (answered.has(exerciseIndex)) return;
      answered.add(exerciseIndex);

      const buttons = document.querySelectorAll(`.option-btn[data-exercise="${exerciseIndex}"]`);
      buttons.forEach((btn) => {
        btn.disabled = true;
        const optionIndex = Number(btn.dataset.option);

        if (optionIndex === exercise.correctIndex) {
          btn.classList.add("correct");
        }

        if (optionIndex === selectedOption && selectedOption !== exercise.correctIndex) {
          btn.classList.add("wrong");
        }
      });

      if (selectedOption === exercise.correctIndex) {
        score += 1;
        feedback.innerHTML = `<strong>Correcto.</strong><br><br>${exercise.explanation}`;
      } else {
        feedback.innerHTML = `<strong>Incorrecto.</strong><br><br>${exercise.explanation}`;
      }

      feedback.style.display = "block";
      updateScoreUI();
    });
  });
}

renderExercises();
updateScoreUI();
updatePhaseDisplay(0);
