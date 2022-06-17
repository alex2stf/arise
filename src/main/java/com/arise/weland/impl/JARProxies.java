package com.arise.weland.impl;

import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.Mole;
import com.arise.core.tools.ReflectUtil;

import java.awt.image.BufferedImage;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import static com.arise.cargo.management.DependencyManager.withJar;
import static com.arise.cargo.management.Locations.snapshot;
import static com.arise.core.tools.ReflectUtil.getMethod;
import static com.arise.core.tools.ReflectUtil.getStaticMethod;
import static java.lang.Class.forName;
import static java.util.Collections.unmodifiableList;
import static java.util.UUID.nameUUIDFromBytes;
import static javax.imageio.ImageIO.write;

public class JARProxies {


    private static void withWebcamCapture(final Handler<Class> h) {
        withJar("WEBCAM_CAPTURE", new Handler<URLClassLoader>() {
            @Override
            public void handle(URLClassLoader classLoader) {
                try {
                    Class webcam = forName("com.github.sarxos.webcam.Webcam", true, classLoader);
                    h.handle(webcam);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void takeSnapshot(String id) {
        if (id != null && camIds != null) {
            for (Tuple2<String, String> t : camIds) {
                if (t.first().equalsIgnoreCase(id) || t.second().equalsIgnoreCase(id)) {
                    id = t.second();
                }
            }
        }

        final String fid = id;
        withWebcamCapture(new Handler<Class>() {
            @Override
            public void handle(Class clz) {
                try {
                    Object webcam;
                    if (fid != null) {
                        webcam = getStaticMethod(clz, "getWebcamByName", String.class).call(fid);
                    } else {
                        webcam = getStaticMethod(clz, "getDefault").call();
                    }
                    if (webcam == null) {
                        Mole.logWarn("No camera found");
                        return;
                    }
                    boolean isOpened = getMethod(webcam, "isOpen").callForBoolean();
                    if (!isOpened) {
                        getMethod(webcam, "open").call();
                    }
                    BufferedImage buf = (BufferedImage) getMethod(webcam, "getImage").call();
                    if (buf != null) {
                        write(buf, "jpg", snapshot(
                                nameUUIDFromBytes((fid != null ? fid : "default").getBytes()) + ".jpg"
                        ));
                    }
                    getMethod(webcam, "close").call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    static List<Tuple2<String, String>> camIds = null;

    public static void getCamIds(final Handler<List<Tuple2<String, String>>> h) {
        if (camIds != null) {
            h.handle(camIds);
            return;
        }

        withWebcamCapture(new Handler<Class>() {
            @Override
            public void handle(Class clz) {
                List<Object> cams = (List<Object>) ReflectUtil.getStaticMethod(clz, "getWebcams").call();

                if (cams != null) {
                    List<Tuple2<String, String>> tmp = new ArrayList<>();
                    for (int i = 0; i < cams.size(); i++) {
                        String nam = getMethod(cams.get(i), "getName").callForString();
                        tmp.add(new Tuple2<>(
                                i + "",
                                nam
                        ));
                    }
                    camIds = unmodifiableList(tmp);
                    h.handle(camIds);
                }
                else {
                   OSProxies.findWebcamIds(h);
                }
            }
        });
    }


}