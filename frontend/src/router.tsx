// ルーティング設定
import { createBrowserRouter } from "react-router-dom";

// ページコンポーネントをここでimportして設定
export const router = createBrowserRouter([
  {
    path: "/",
    element: <div>Home Page</div>, // 後でHomePageコンポーネントに置き換え
  },
  {
    path: "*",
    element: <div>404 Not Found</div>, // 後でNotFoundPageコンポーネントに置き換え
  },
]);
