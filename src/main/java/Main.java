import com.google.gson.*;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class Main {
    public final static String FILE_SEPARATOR = System.getProperty("file.separator");

    public static void main(String[] args) throws IOException {
        String source = "", destination = "", originalSongs = "";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-src")) {
                source = args[++i];
                continue;
            }
            if (args[i].equals("-des")) {
                destination = args[++i];
                continue;
            }
            if (args[i].equals("--original-songs")) {
                originalSongs = args[++i];
                continue;
            }
        }
        File fSource = new File(source), fDest = new File(destination), fOriginalSongs = new File(originalSongs);
        if (!fSource.exists() || !fSource.isDirectory()) {
            System.out.println("错误：未检测到谱面源文件夹，系统将自动退出");
            return;
        }
        if (!fOriginalSongs.exists() || !fOriginalSongs.isDirectory()) {
            System.out.println("错误：未检测到songs文件夹，系统将自动退出");
            return;
        }
        if (!fDest.exists() || !fDest.isDirectory()) {
            if (fDest.exists()) fDest.delete();
            if (fDest.mkdirs()) {
                System.out.println("警告：未检测到输出文件夹，自动创建");
            } else {
                System.out.println("错误：未检测到输出文件夹并且无法创建，系统将自动退出");
                return;
            }
        }
        readFiles(source, destination, originalSongs);
    }

    private static void readFiles(String source, String destination, String originalSongs) throws IOException {
        if (!source.endsWith(FILE_SEPARATOR)) {
            source += FILE_SEPARATOR;
        }
        if (!destination.endsWith(FILE_SEPARATOR)) {
            destination += FILE_SEPARATOR;
        }
        if (!originalSongs.endsWith(FILE_SEPARATOR)) {
            originalSongs += FILE_SEPARATOR;
        }
        JsonArray songs = fileToJsonObject(originalSongs + "songlist").get("songs").getAsJsonArray();
        for (JsonElement element : songs) {
            JsonObject song = element.getAsJsonObject();
            String sid = song.get("id").getAsString();
            if (sid.equals("arcanaeden") || sid.equals("infinitestrife") || sid.equals("pentiment") || sid.equals("testify") || sid.equals("worldender")) {
                if (!new File(destination + "Final Verdict包PV").exists()) new File(destination + "Final Verdict包PV").mkdirs();
                copyFile(source + sid + "_video.mp4", destination + "Final Verdict包PV" + FILE_SEPARATOR + sid + "_video.mp4");
                copyFile(source + sid + "_video_audio.ogg", destination + "Final Verdict包PV" + FILE_SEPARATOR + sid + "_video_audio.ogg");
            }
            if (song.has("remote_dl") && song.get("remote_dl").getAsBoolean()) {
                if (new File(source + sid).exists()) {
                    copyFile(source + sid, destination + sid + FILE_SEPARATOR + "base.ogg");
                    for (int i = 0; i < 3; i++) {
                        copyFile(source + sid + "_" + i, destination + sid + FILE_SEPARATOR + i + ".aff");
                    }
                } else {
                    for (int i = 0; i < 3; i++) {
                        if (!new File(destination + sid + FILE_SEPARATOR + i + ".aff").exists())
                            System.out.println(song.get("set").getAsString() + "曲包的" + sid + "的主体（base.ogg，0.aff ~ 2.aff）不存在，将被忽略");
                    }
                }
                copyFile(originalSongs + "dl_" + sid + FILE_SEPARATOR + "base.jpg", destination + sid + FILE_SEPARATOR + "base.jpg");
                copyFile(originalSongs + "dl_" + sid + FILE_SEPARATOR + "base_256.jpg", destination + sid + FILE_SEPARATOR + "base_256.jpg");
                copyFile(originalSongs + "dl_" + sid + FILE_SEPARATOR + "preview.ogg", destination + sid + FILE_SEPARATOR + "preview.ogg");
            }
            JsonArray difficulties = song.get("difficulties").getAsJsonArray();
            boolean remoteDownload = song.has("remote_dl") && song.get("remote_dl").getAsBoolean();
            String jdfksjfdlk = remoteDownload ? "_dl" : "";
            for (JsonElement jsonElement : difficulties) {
                JsonObject difficulty = jsonElement.getAsJsonObject();
                if (difficulty.has("audioOverride") && difficulty.get("audioOverride").getAsBoolean()) {
                    int ratingClass = difficulty.get("ratingClass").getAsInt();
                    if (new File(source + sid + "_audio_" + ratingClass).exists()) {
                        copyFile(source + sid + "_audio_" + ratingClass, destination + sid + FILE_SEPARATOR + ratingClass + ".ogg");
                    } else {
                        if (!new File(destination + sid + FILE_SEPARATOR + ratingClass + ".ogg").exists())
                            System.out.println(sid + "的" + getRatingByRatingClass(ratingClass) + "难度的音乐不存在，将被忽略");
                    }
                }
                if (difficulty.has("jacketOverride") && difficulty.get("jacketOverride").getAsBoolean()) {
                    int ratingClass = difficulty.get("ratingClass").getAsInt();
                    copyFile(originalSongs + jdfksjfdlk + sid + FILE_SEPARATOR + ratingClass + ".jpg", destination + sid + FILE_SEPARATOR + ratingClass + ".jpg");
                    copyFile(originalSongs + jdfksjfdlk + sid + FILE_SEPARATOR + ratingClass + "_256.jpg", destination + sid + FILE_SEPARATOR + ratingClass + "_256.jpg");
                }
                if (difficulty.has("jacket_night")) {
                    String nightJacket = difficulty.get("jacket_night").getAsString();
                    copyFile(originalSongs + jdfksjfdlk + sid + FILE_SEPARATOR + nightJacket + ".jpg", destination + sid + FILE_SEPARATOR + nightJacket + ".jpg");
                    copyFile(originalSongs + jdfksjfdlk + sid + FILE_SEPARATOR + nightJacket + "_256.jpg", destination + sid + FILE_SEPARATOR + nightJacket + "_256.jpg");
                }
            }
            if (difficulties.size() > 3) {
                if (new File(source + sid + "_3").exists()) {
                    copyFile(source + sid + "_3", destination + sid + FILE_SEPARATOR + "3.aff");
                } else {
                    if (!new File(destination + sid + FILE_SEPARATOR + "3.aff").exists())
                        System.out.println(sid + "的byd难度不存在，将被忽略");
                }
                copyFile(originalSongs + sid + FILE_SEPARATOR + "3_preview.ogg", destination + sid + FILE_SEPARATOR + "3_preview.ogg");
                if (!remoteDownload) {
                    copyFile(originalSongs + sid + FILE_SEPARATOR + "base.jpg", destination + sid + FILE_SEPARATOR + "base.jpg");
                    copyFile(originalSongs + sid + FILE_SEPARATOR + "base_256.jpg", destination + sid + FILE_SEPARATOR + "base_256.jpg");
                    copyFile(originalSongs + sid + FILE_SEPARATOR + "base.ogg", destination + sid + FILE_SEPARATOR + "base.ogg");
                    copyFile(originalSongs + sid + FILE_SEPARATOR + "0.aff", destination + sid + FILE_SEPARATOR + "0.aff");
                    copyFile(originalSongs + sid + FILE_SEPARATOR + "1.aff", destination + sid + FILE_SEPARATOR + "1.aff");
                    copyFile(originalSongs + sid + FILE_SEPARATOR + "2.aff", destination + sid + FILE_SEPARATOR + "2.aff");
                }
            }
        }
    }

    private static JsonObject fileToJsonObject(String file) throws IOException {
        Gson gson = new Gson();
        FileReader fileReader = new FileReader(file);
        JsonObject jsonObject = gson.fromJson(fileReader, JsonObject.class);
        fileReader.close();
        return jsonObject;
    }

    private static void copyFile(String src, String des) {
        File source = new File(src);
        File destination = new File(des);
        if (!source.exists()) return;
        if (destination.exists()) {
            destination.delete();
        }
        File desFolder = destination.getParentFile();
        if (!desFolder.exists()) {
            desFolder.mkdirs();
        }
        InputStream in;
        OutputStream out;
        try {
            destination.createNewFile();

            in = Files.newInputStream(source.toPath());
            out = Files.newOutputStream(destination.toPath());

            byte[] buf = new byte[2048];
            int i;
            while ((i = in.read(buf)) != -1) {
                out.write(buf, 0, i);
                out.flush();
            }
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getRatingByRatingClass(int ratingClass) {
        switch (ratingClass) {
            case 0:
                return "Past";
            case 1:
                return "Present";
            case 2:
                return "Future";
            case 3:
                return "Beyond";
            default:
                throw new IndexOutOfBoundsException("未知难度：" + ratingClass);
        }
    }
}