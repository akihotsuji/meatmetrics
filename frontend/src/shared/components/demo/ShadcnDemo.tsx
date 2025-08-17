import { Button } from "@/shared/components/ui/button";
import { Input } from "@/shared/components/ui/input";
import { Label } from "@/shared/components/ui/label";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/shared/components/ui/card";
import { Badge } from "@/shared/components/ui/badge";
import {
  Alert,
  AlertDescription,
  AlertTitle,
} from "@/shared/components/ui/alert";
import { Separator } from "@/shared/components/ui/separator";
import { Textarea } from "@/shared/components/ui/textarea";

export function ShadcnDemo() {
  return (
    <div className="container mx-auto p-6 space-y-8">
      <div className="text-center">
        <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100">
          MeatMetrics shadcn/ui デモ
        </h1>
        <p className="text-gray-600 dark:text-gray-400 mt-2">
          カスタムテーマが適用されたshadcn/uiコンポーネント
        </p>
      </div>

      <Separator />

      {/* ボタンバリアント */}
      <Card>
        <CardHeader>
          <CardTitle>ボタンバリアント</CardTitle>
          <CardDescription>
            MeatMetricsカスタムカラーが適用されたボタンコンポーネント
          </CardDescription>
        </CardHeader>
        <CardContent className="flex flex-wrap gap-4">
          <Button variant="default">デフォルト</Button>
          <Button variant="destructive">削除</Button>
          <Button variant="success">成功</Button>
          <Button variant="warning">警告</Button>
          <Button variant="outline">アウトライン</Button>
          <Button variant="secondary">セカンダリ</Button>
          <Button variant="ghost">ゴースト</Button>
          <Button variant="link">リンク</Button>
        </CardContent>
      </Card>

      {/* フォーム要素 */}
      <Card>
        <CardHeader>
          <CardTitle>フォーム要素</CardTitle>
          <CardDescription>統合されたフォームコンポーネント</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="email">メールアドレス</Label>
            <Input
              id="email"
              type="email"
              placeholder="example@meatmetrics.com"
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="message">メッセージ</Label>
            <Textarea
              id="message"
              placeholder="ここにメッセージを入力してください..."
            />
          </div>
        </CardContent>
        <CardFooter>
          <Button type="submit">送信</Button>
        </CardFooter>
      </Card>

      {/* バッジ */}
      <Card>
        <CardHeader>
          <CardTitle>バッジ</CardTitle>
          <CardDescription>
            ステータスや分類を表示するバッジコンポーネント
          </CardDescription>
        </CardHeader>
        <CardContent className="flex flex-wrap gap-2">
          <Badge variant="default">デフォルト</Badge>
          <Badge variant="secondary">セカンダリ</Badge>
          <Badge variant="destructive">エラー</Badge>
          <Badge variant="outline">アウトライン</Badge>
        </CardContent>
      </Card>

      {/* アラート */}
      <div className="space-y-4">
        <Alert>
          <AlertTitle>情報</AlertTitle>
          <AlertDescription>
            これは情報アラートです。重要な情報をユーザーに伝えます。
          </AlertDescription>
        </Alert>

        <Alert variant="destructive">
          <AlertTitle>エラー</AlertTitle>
          <AlertDescription>
            これはエラーアラートです。問題が発生した場合に表示されます。
          </AlertDescription>
        </Alert>
      </div>

      {/* カラーパレット表示 */}
      <Card>
        <CardHeader>
          <CardTitle>MeatMetrics カラーパレット</CardTitle>
          <CardDescription>適用されているカスタムカラーの確認</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="text-center">
              <div className="w-16 h-16 bg-primary rounded-lg mx-auto mb-2"></div>
              <span className="text-sm font-medium">Primary</span>
            </div>
            <div className="text-center">
              <div className="w-16 h-16 bg-secondary rounded-lg mx-auto mb-2"></div>
              <span className="text-sm font-medium">Secondary</span>
            </div>
            <div className="text-center">
              <div className="w-16 h-16 bg-success rounded-lg mx-auto mb-2"></div>
              <span className="text-sm font-medium">Success</span>
            </div>
            <div className="text-center">
              <div className="w-16 h-16 bg-warning rounded-lg mx-auto mb-2"></div>
              <span className="text-sm font-medium">Warning</span>
            </div>
            <div className="text-center">
              <div className="w-16 h-16 bg-destructive rounded-lg mx-auto mb-2"></div>
              <span className="text-sm font-medium">Destructive</span>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
