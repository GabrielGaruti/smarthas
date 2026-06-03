import express, { type Express, type Request, type Response } from "express";
import cors from "cors";
import pinoHttp from "pino-http";
import path from "path";
import http from "http";
import { fileURLToPath } from "url";
import { logger } from "./lib/logger";

const __dirname = path.dirname(fileURLToPath(import.meta.url));

const app: Express = express();

app.use(
  pinoHttp({
    logger,
    serializers: {
      req(req) {
        return {
          id: req.id,
          method: req.method,
          url: req.url?.split("?")[0],
        };
      },
      res(res) {
        return {
          statusCode: res.statusCode,
        };
      },
    },
  }),
);
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

function proxyToPython(prefix: string) {
  return (req: Request, res: Response) => {
    const bodyStr =
      req.body && Object.keys(req.body).length > 0
        ? JSON.stringify(req.body)
        : "";
    const bodyBuf = Buffer.from(bodyStr, "utf-8");

    const options: http.RequestOptions = {
      hostname: "localhost",
      port: 8000,
      path: prefix + (req.url ?? ""),
      method: req.method,
      headers: {
        "content-type": "application/json",
        "content-length": bodyBuf.length,
        ...(req.headers["authorization"]
          ? { authorization: req.headers["authorization"] }
          : {}),
      },
    };

    const proxyReq = http.request(options, (proxyRes) => {
      res.writeHead(proxyRes.statusCode ?? 502, proxyRes.headers);
      proxyRes.pipe(res);
    });
    proxyReq.on("error", (err) => {
      res.status(502).json({ error: "Backend Python indisponível", detail: (err as Error).message });
    });
    if (bodyBuf.length > 0) proxyReq.write(bodyBuf);
    proxyReq.end();
  };
}

app.use("/api/auth", proxyToPython("/auth"));
app.use("/api/measurements", proxyToPython("/measurements"));
app.get("/api/health", proxyToPython("/health"));
app.get("/api/healthz", (_req, res) => {
  res.json({ status: "ok" });
});

const flutterBuildPath = path.resolve(__dirname, "../../../flutter_app/build/web");

if (process.env["NODE_ENV"] !== "production") {
  app.use((_req, res, next) => {
    res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate");
    res.setHeader("Pragma", "no-cache");
    res.setHeader("Expires", "0");
    next();
  });
}

app.use(express.static(flutterBuildPath));
app.get("/{*path}", (_req, res) => {
  res.sendFile(path.join(flutterBuildPath, "index.html"));
});

export default app;
