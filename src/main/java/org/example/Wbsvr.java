package org.example;

// org.example.Wbsvr
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import io.javalin.http.Header;
import io.javalin.http.UploadedFile;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static org.example.BotBroswer.getBrowser4disabGpu;

import static org.example.implt.OpenCoinMarketCap.*;
import static org.example.ScrSnap.*;
import static uti.Util.iniLogCfg;

public class Wbsvr {
    public static Logger log;

    static {
        iniLogCfg();
        log = LoggerFactory.getLogger(Wbsvr.class);
    }


    //        Handler hdl2crp = ctx -> {
//
//
//        };
   // static LoadingCache<String, Page> cachePage;


    public static void main(String[] args) {
        iniLogCfg();
        Wbsvr.log = LoggerFactory.getLogger(Wbsvr.class);
        iniImgCache();

        //    iniCachePage();

        // Javalin app = Javalin.create().start(8888);
        Map cfgMap=getCfgMap(getPrjDir()+"/cfg/cfg.ini");
        int port = 8888;
        if(cfgMap.containsKey("port"))
            port = Integer.parseInt(cfgMap.get("port").toString());
        Javalin app = Javalin.create(getJavalinConfigCrossdmain()).start(  port);
        setCrsdmnOptionHdl(app);


        // http://13.212.95.142:8888/screenshotK?
        //http://13.212.95.142:8888/screenshotK?url=https%3A%2F%2Fcoinmarketcap.com%2Fcurrencies%2Fbitcoin%2F
        // http://127.0.0.1:8888/screenshotCrpt?currencies=bitcoin
        app.get("/apiv1/screenshotK", Wbsvr::hdl2crp);
        app.get("/screenshotK", Wbsvr::hdl2crp);
      //  app.get("/screenshotGld", Wbsvr::hdl2gld);

        app.post("/upload", ctx -> {
            // 读取文本框中的保存文件名
            String saveName = ctx.formParam("savename");
            if (saveName == null || saveName.isBlank()) {
                ctx.status(400).result("Missing savename");
                return;
            }

            // 获取上传的文件
            UploadedFile uploadedFile = ctx.uploadedFile("file");
            if (uploadedFile == null) {
                ctx.status(400).result("No file uploaded");
                return;
            }

            // 保存到目标路径
            File targetFile = new File("uploads/" + saveName);
            try {
                Files.createDirectories(targetFile.getParentFile().toPath()); // 确保目录存在
                Files.copy(uploadedFile.content(), targetFile.toPath());
                ctx.result("File saved as: " + saveName);
            } catch (IOException e) {
                ctx.status(500).result("Failed to save file: " + e.getMessage());
            }
        });

    }


    /**
     * 得到项目根目录（开发时返回项目目录，打包后返回 JAR 所在目录）
     */
    private static String getPrjDir() {
        try {
            URL targetDir = Main.class.getProtectionDomain().getCodeSource().getLocation();
            File file = new File(targetDir.toURI());

            if (file.getPath().endsWith("/classes") || file.getPath().endsWith("\\classes")) {
                // 开发环境：target/classes → 返回上两级（到项目根目录）
                return file.getParentFile().getParent();
            } else {
                // JAR 包情况：返回 JAR 所在目录
                return file.getParent();
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException("获取项目目录失败", e);
        }
    }


    private static Map getCfgMap(String fileName) {
        Properties properties = new Properties();
        Map<String, String> map = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(fileName)) {
            properties.load(fis);
            for (String key : properties.stringPropertyNames()) {
                map.put(key, properties.getProperty(key));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config file: " + fileName, e);
        }

        return map;
    }

    private static void setCrsdmnOptionHdl(Javalin app) {
        app.options("/*", ctx -> {
            ctx.header(Header.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            ctx.header(Header.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE,OPTIONS");
            ctx.header(Header.ACCESS_CONTROL_ALLOW_HEADERS, "Content-Type,Authorization");
            ctx.header(Header.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");

            ctx.status(204); // No Content
        });
    }

    @NotNull
    private static Consumer<JavalinConfig> getJavalinConfigCrossdmain() {
        return config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });
            });
        };
    }




//    private static void handleOptions(@NotNull HttpExchange exchange) throws Exception {
//        setCrossDomain(exchange);
//        exchange.getResponseHeaders().add("Allow", "GET, POST, PUT, DELETE, OPTIONS");
//
//

    /// /返回状态码 204（无内容）是标准做法。
//        exchange.sendResponseHeaders(204, -1); // No Content
//        exchange.close();
//    }
//    public static void setCrossDomain(HttpExchange exchange) {
//        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
//        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
//        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
//        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
//    }
//    private static void iniCachePage() {
//        // 创建一个自动加载缓存
//        String cacheName = "imgCache";
//        cachePage = CacheBuilder.newBuilder()
//                .maximumSize(9)  // 最多缓存100个条目
//                .expireAfterWrite(600, TimeUnit.SECONDS)  // 缓存项在写入10秒后过期
//                .removalListener(notification ->
//                        // cache.cleanUp() 主动触发清理。
//                        //timer clearup just ok
//                        System.out.println(cacheName + "移除缓存: " + notification.getKey() + " -> " + notification.getCause())
//                )
//                .build(new CacheLoader<String, Page>() {
//                    @Override
//                    public Page load(String k) throws Exception {
//                        // 模拟从数据库或外部系统加载数据
//                        log.info(cacheName + "加载数据start：" + k);
//                        Page pg;
//                        if (k.contains("coinmarketcap"))
//                            pg = getPage4crp();
//                        else
//                            pg = getPage4gld();
//                        log.info(cacheName + "加载数据finish： key=" + k);
//                        return pg;
//                    }
//                });
//    }

    private static Page getPage4gld() {
        return null;
    }

    private static Page getPage4crp() {
        BrowserContext context = getBrowserContextFastOptmz(getBrowser4disabGpu());
        Page page = context.newPage();
        return page;
    }

    public static void hdl2crp(Context ctx) throws IOException, InterruptedException, ExecutionException {
        String url = ctx.queryParam("url");


        //readPic("C:\\Users\\Administrator\\IdeaProjects\\kCandlPrj\\gld1747480497886.png");
        //


        ctx.contentType("image/png");
        assert url != null;
        //    String url = "https://coinmarketcap.com/currencies/" + url + "/";
        ctx.result(cache.get(url));
//        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
//
//        ctx.result(base64Image);
//        ctx.contentType("image/png");

    }


    public static void hdl2gld(Context ctx) throws IOException, InterruptedException {
        String currencies = ctx.queryParam("currencies");

        byte[] imageBytes = getBytesPicFrmCrptsite(browser4crp, currencies);


        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        ctx.result(base64Image);
        ctx.contentType("image/png");
    }
}
