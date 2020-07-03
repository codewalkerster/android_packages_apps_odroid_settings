package com.hardkernel.odroid.settings.gpu;

import android.content.Context;

import com.hardkernel.odroid.settings.ConfigEnv;
import com.hardkernel.odroid.settings.R;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;


public class GPU {
    private final static String SCALE_MODE = "/sys/class/mpgpu/scale_mode";

    private final String tag;
    private String scale_mode;

    private List<String> scaleModeList = null;

    private GPU (String tag) {
        this.tag = tag;
        scale_mode = ConfigEnv.getGpuScaleMode();

    }

    public void apply() {
        set(SCALE_MODE, scale_mode);
    }

    private static GPU gpu = null;

    public static GPU getGPU(String tag) {
        if (gpu == null)
            gpu = new GPU(tag);

        return gpu;
    }

    public void initList(Context context) {
        if (scaleModeList == null)
            scaleModeList = Arrays.asList(context.getResources()
                .getStringArray(R.array.gpu_scale_mode));
    }

    public List<String> getScaleModeList() {
        return scaleModeList;
    }

    public void setScaleMode(String mode) {
        scale_mode = mode;
        ConfigEnv.setGpuScaleMode(mode);
        set(SCALE_MODE, scale_mode);
    }

    public String getScaleMode() {
        return scaleModeList.get(Integer.valueOf(scale_mode));
    }

    public int getScaleModeIdx() {
        return Integer.parseInt(scale_mode);
    }

    private void set(String node, String value) {
        try {
            BufferedWriter out;
            FileWriter fileWriter = new FileWriter(node);
            out = new BufferedWriter(fileWriter);
            out.write(value);
            out.newLine();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
