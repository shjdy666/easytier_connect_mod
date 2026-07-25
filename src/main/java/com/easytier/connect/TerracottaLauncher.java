package com.easytier.connect;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class TerracottaLauncher {
    private static TerracottaLauncher INSTANCE;
    private Process process;
    private int port = -1;
    private boolean running = false;

    private TerracottaLauncher() {}
    public static synchronized TerracottaLauncher getInstance() {
        if (INSTANCE == null) INSTANCE = new TerracottaLauncher();
        return INSTANCE;
    }

    /** 获取陶瓦网页地址，如果未启动则先启动 */
    public CompletableFuture<String> getOrStart() {
        if (running && port > 0) {
            return CompletableFuture.completedFuture("http://127.0.0.1:" + port + "/");
        }
        return start();
    }

    private synchronized CompletableFuture<String> start() {
        CompletableFuture<String> future = new CompletableFuture<>();

        new Thread(() -> {
            try {
                Path exePath = extractBinary();
                // 启动 Terracotta（普通模式，会自动打开浏览器）
                ProcessBuilder pb = new ProcessBuilder(exePath.toString());
                pb.directory(new File(System.getProperty("java.io.tmpdir")));
                pb.redirectErrorStream(true);
                process = pb.start();

                // 读取输出，提取端口
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        EasyTierConnectMod.LOGGER.debug("[Terracotta] {}", line);
                        if (line.contains("Rocket has launched from")) {
                            try {
                                int colon = line.lastIndexOf(":");
                                int slash = line.lastIndexOf("/");
                                if (colon > slash) {
                                    port = Integer.parseInt(line.substring(colon + 1).trim());
                                    running = true;
                                    String url = "http://127.0.0.1:" + port + "/";
                                    EasyTierConnectMod.LOGGER.info("Terracotta ready at {}", url);
                                    future.complete(url);
                                    return;
                                }
                            } catch (Exception e) {
                                EasyTierConnectMod.LOGGER.warn("Failed to parse port from: {}", line);
                            }
                        }
                    }
                }
                // 如果循环结束还没找到端口，报错
                if (!running) {
                    future.completeExceptionally(new RuntimeException("Terracotta did not start properly"));
                }
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }, "Terracotta-Launcher").start();

        return future;
    }

    /** 停止陶瓦 */
    public synchronized void stop() {
        if (process != null) {
            process.destroyForcibly();
            try { process.waitFor(5, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
            process = null;
        }
        running = false;
        port = -1;
    }

    public boolean isRunning() { return running; }
    public int getPort() { return port; }

    private Path extractBinary() throws IOException {
        String binaryName = "terracotta.exe";
        Path nativeDir = new File(System.getProperty("java.io.tmpdir"), "easytier_connect").toPath();
        Files.createDirectories(nativeDir);
        Path exePath = nativeDir.resolve(binaryName);
        try (InputStream in = getClass().getResourceAsStream("/natives/terracotta.exe")) {
            if (in == null) throw new FileNotFoundException("Missing terracotta.exe in mod jar");
            Files.copy(in, exePath, StandardCopyOption.REPLACE_EXISTING);
        }
        // VC++ 运行库
        try (InputStream in = getClass().getResourceAsStream("/natives/VCRUNTIME140.DLL")) {
            if (in != null) {
                Files.copy(in, nativeDir.resolve("VCRUNTIME140.DLL"), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ignored) {}
        return exePath;
    }
}