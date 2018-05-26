package com.mmmmar.helmsman;

import android.util.Log;

import java.util.Arrays;

public class ToyLogic {

    private static final String TAG = "ToyLogic";

    private static final int NUM_LEVEL = 11;
    private static final int LEVEL_DEFAULT = NUM_LEVEL / 2;

    private CommandConsumer commandConsumer;

    private int levelLeft = LEVEL_DEFAULT;
    private int levelRight = LEVEL_DEFAULT;

    private byte[][] commandTable = {
            {0x12, 0x4F, 0x5F},
            {0x12, 0x4D, 0x5D},
            {0x12, 0x4A, 0x5A},
            {0x12, 0x46, 0x56},
            {0x12, 0x43, 0x53},
            {0x11, 0x00, 0x00},
            {0x13, 0x43, 0x53},
            {0x13, 0x46, 0x56},
            {0x13, 0x4A, 0x5A},
            {0x13, 0x4D, 0x5D},
            {0x13, 0x4F, 0x5F}
    };

    private float[][] turnTable = {
            {2, 1.0f},
            {2, 0.8f},
            {2, 0.6f},
            {2, 0.4f},
            {2, 0.2f},
            {0, 0  },
            {1, 0.2f},
            {1, 0.4f},
            {1, 0.6f},
            {1, 0.8f},
            {1, 1.0f},
    };

    public void handleLeft(float progress) {
        levelLeft = (int) (NUM_LEVEL * progress);
        makeCommand();
    }

    public void handleRight(float progress) {
        levelRight = (int) (NUM_LEVEL * progress);
        // 静止时左右转无意义。
        if (levelLeft != LEVEL_DEFAULT) {
            makeCommand();
        }
    }

    private void makeCommand() {
        byte[] forward = commandTable[levelLeft];
        float[] turn = turnTable[levelRight];

        if (commandConsumer != null) {

            int wheel = (int) turn[0];
            float reduceRatio = turn[1];
            int speed = forward[wheel] % 0x10;
            int reduce = (int) (speed * reduceRatio);
            // 防止创建大量byte[]对象。
            forward[wheel] -= reduce;
            commandConsumer.onCommand(forward);
            forward[wheel] += reduce;
        }
    }

    public void setCommandConsumer(CommandConsumer consumer) {
        commandConsumer = consumer;
    }

    public interface CommandConsumer {
        void onCommand(byte[] data);
    }


}
