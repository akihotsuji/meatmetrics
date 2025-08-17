import { useEffect, useState } from "react";
import reactLogo from "./assets/react.svg";
import viteLogo from "/vite.svg";
import { ShadcnDemo } from "./shared/components/demo/ShadcnDemo";

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
    } catch {
      // noop: 画面に現状値を残す
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHealth();
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-secondary-100 py-8">
      <div className="container mx-auto px-4">
        {/* ヘッダーセクション */}
        <div className="text-center mb-8">
          <div className="flex justify-center items-center gap-8 mb-6">
            <a
              href="https://vite.dev"
              target="_blank"
              className="hover:scale-110 transition-transform duration-200"
            >
              <img src={viteLogo} className="w-16 h-16" alt="Vite logo" />
            </a>
            <a
              href="https://react.dev"
              target="_blank"
              className="hover:scale-110 transition-transform duration-200"
            >
              <img src={reactLogo} className="w-16 h-16" alt="React logo" />
            </a>
          </div>
          <h1 className="text-4xl font-bold text-gray-900 mb-2">MeatMetrics</h1>
          <p className="text-lg text-gray-600">
            Vite + React + TailwindCSS（Docker環境）
          </p>
        </div>

        {/* メインコンテンツ */}
        <div className="max-w-4xl mx-auto space-y-6">
          {/* カウンターカード */}
          <div className="card animate-fade-in">
            <div className="card-header">
              <h2 className="card-title">インタラクティブカウンター</h2>
            </div>
            <div className="text-center">
              <button
                className="btn-primary text-lg px-8 py-3 mb-4"
                onClick={() => setCount((count) => count + 1)}
              >
                count is {count}
              </button>
              <p className="text-gray-600">
                <code className="bg-gray-100 px-2 py-1 rounded text-sm">
                  src/App.tsx
                </code>
                を編集してHMRをテスト
              </p>
            </div>
          </div>

          {/* TailwindCSSテストカード */}
          <div className="card animate-fade-in">
            <div className="card-header">
              <h2 className="card-title">TailwindCSS 動作確認</h2>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <button className="btn-primary">Primary</button>
              <button className="btn-secondary">Secondary</button>
              <button className="btn-success">Success</button>
              <button className="btn-warning">Warning</button>
              <button className="btn-error">Error</button>
              <button className="btn-outline">Outline</button>
              <div className="col-span-2">
                <input
                  className="input"
                  type="text"
                  placeholder="入力フィールドのテスト"
                />
              </div>
            </div>
          </div>

          {/* ヘルスチェックカード */}
          <div className="card animate-fade-in">
            <div className="card-header">
              <h2 className="card-title">API ヘルスチェック</h2>
            </div>
            <div className="text-left">
              <button
                className={`mb-4 ${loading ? "btn-outline" : "btn-primary"}`}
                onClick={fetchHealth}
                disabled={loading}
              >
                {loading ? (
                  <span className="flex items-center gap-2">
                    <div className="w-4 h-4 border-2 border-gray-300 border-t-gray-600 rounded-full animate-spin"></div>
                    Checking...
                  </span>
                ) : (
                  "再チェック"
                )}
              </button>
              <p className="text-sm text-gray-600 mb-4">
                最終更新: {lastUpdated || "-"}
              </p>
              <div className="bg-gray-50 rounded-lg p-4 overflow-auto">
                <pre className="text-sm text-gray-800">
                  {JSON.stringify({ health, dbHealth }, null, 2)}
                </pre>
              </div>
            </div>
          </div>

          {/* shadcn/ui デモセクション */}
          <div className="mt-12">
            <ShadcnDemo />
          </div>
        </div>

        {/* フッター */}
        <div className="text-center mt-12">
          <p className="text-gray-500">
            ViteとReactのロゴをクリックして詳細を確認
          </p>
        </div>
      </div>
    </div>
  );
}

export default App;
