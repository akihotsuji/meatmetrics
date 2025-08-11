# React用Dockerfile (本番環境)
FROM node:22-bookworm-slim AS build

WORKDIR /app

# 依存関係のインストール
COPY package*.json ./
RUN npm ci

# ソースコードのコピー
COPY . .

# 本番ビルド
ENV NODE_ENV=production
RUN npm run build

# Nginxでの配信用
FROM nginx:alpine

# ビルド成果物をNginxにコピー
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
