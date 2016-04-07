package org.ground.palico.gpu.aparapi;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;
import com.amd.aparapi.device.Device;

/**
 * Created by shlee on 16. 3. 30.
 */
public class AparapiBandOperator0 implements AparapiBandOperator {
    @Override
    public void run(int size, float[] band1, float[] band2, float[] band3, float[] result, Kernel.EXECUTION_MODE mode) {
        Kernel kernel = new Kernel() {
            @Override
            public void run() {
                int gid = getGlobalId();
                result[gid] = band1[gid];
            }
        };
        Device device = Device.best();
        Range range = device.createRange(size);

        kernel.setExecutionMode(mode);
        kernel.execute(range);
        kernel.dispose();
    }
}
