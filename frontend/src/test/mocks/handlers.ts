import { http, HttpResponse } from "msw";

export const handlers = [
  // Auth endpoints
  http.post("/api/auth/register", () => {
    return HttpResponse.json({
      message: "User registered successfully",
      user: {
        id: "1",
        username: "testuser",
        email: "test@example.com",
      },
    });
  }),

  http.post("/api/auth/login", () => {
    return HttpResponse.json({
      token: "mock-jwt-token",
      user: {
        id: "1",
        username: "testuser",
        email: "test@example.com",
      },
    });
  }),

  // User endpoints
  http.get("/api/users/profile", () => {
    return HttpResponse.json({
      id: "1",
      username: "testuser",
      email: "test@example.com",
    });
  }),

  // Foods endpoints
  http.get("/api/foods/search", () => {
    return HttpResponse.json({
      foods: [
        {
          id: "1",
          name: "牛肉（サーロイン）",
          protein: 25.0,
          fat: 20.0,
          carbs: 0.1,
          tags: ["肉類", "低糖質"],
        },
      ],
    });
  }),

  // Meals endpoints
  http.get("/api/meals", () => {
    return HttpResponse.json({
      meals: [],
    });
  }),

  http.post("/api/meals", () => {
    return HttpResponse.json({
      id: "1",
      date: "2024-01-01",
      foods: [],
    });
  }),
];
