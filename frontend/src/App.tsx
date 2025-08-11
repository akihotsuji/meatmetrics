import { useEffect, useState } from "react";
import reactLogo from "./assets/react.svg";
import viteLogo from "/vite.svg";
import "./App.css";

type Health = { status?: string; time?: string; error?: string };

function App() {
  const [count, setCount] = useState(0);
  const [health, setHealth] = useState<Health>({});
  const [dbHealth, setDbHealth] = useState<Health>({});
  const [loading, setLoading] = useState(false);
  const [lastUpdated, setLastUpdated] = useState<string>("");

  const fetchHealth = async () => {
    setLoading(true);
    try {
      const r1 = await fetch("/api/health");
      const h1: Health = await r1.json().catch(() => ({}));
      setHealth(h1);

      const r2 = await fetch("/api/health/db");
      const h2: Health = await r2.json().catch(() => ({}));
      setDbHealth(h2);

      setLastUpdated(new Date().toLocaleString());
    } catch (e) {
      // noop: 画面に現状値を残す
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHealth();
  }, []);

  return (
    <>
      <div>
        <a href="https://vite.dev" target="_blank">
          <img src={viteLogo} className="logo" alt="Vite logo" />
        </a>
        <a href="https://react.dev" target="_blank">
          <img src={reactLogo} className="logo react" alt="React logo" />
        </a>
      </div>
      <h1>Vite + React</h1>
      <div className="card">
        <button onClick={() => setCount((count) => count + 1)}>
          count is {count}
        </button>
        <p>
          Edit <code>src/App.tsx</code> and save to test HMR
        </p>
      </div>

      <div className="card" style={{ textAlign: "left" }}>
        <h2>API Health Check</h2>
        <button onClick={fetchHealth} disabled={loading}>
          {loading ? "Checking..." : "Recheck"}
        </button>
        <p style={{ marginTop: 8 }}>Last updated: {lastUpdated || "-"}</p>
        <pre>{JSON.stringify({ health, dbHealth }, null, 2)}</pre>
      </div>

      <p className="read-the-docs">
        Click on the Vite and React logos to learn more
      </p>
    </>
  );
}

export default App;
