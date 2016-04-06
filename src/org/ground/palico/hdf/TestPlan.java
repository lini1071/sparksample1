package org.ground.palico.hdf;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;
import com.amd.aparapi.device.Device;

class TestPlan {
    String modeName;
    Kernel.EXECUTION_MODE mode;

    public TestPlan(Kernel.EXECUTION_MODE mode) {
        this.mode = mode;
    }

    public String getModeName() {
        if (mode.equals(Kernel.EXECUTION_MODE.CPU)) {
            modeName = "CPU";
        } else if (mode.equals(Kernel.EXECUTION_MODE.GPU)) {
            modeName = "GPU";
        } else if (mode.equals(Kernel.EXECUTION_MODE.JTP)) {
            modeName = "JTP";
        }
        return modeName;
    }

    public void perform() throws Exception {
        HDFReader hdfReader = new HDFReader("/home/shlee/GOCI/20160301_GOCIdata/LV1B/COMS_GOCI_L1B_GA_20160301001642.he5");

        int[] band3 = hdfReader.getDataset(3);
        int[] band4 = hdfReader.getDataset(4);

        int size = hdfReader.getSize();
        double[] result = new double[size];

        for (int i = 0; i < result.length; i++) {
            result[i] = 0.0;
        }

        double c0 = -0.0929;
        double c1 = 0.2974;
        double c2 = -2.2429;
        double c3 = 0.8358;
        double c4 = -0.0077;

        Kernel kernel = new Kernel() {
            @Override
            public void run() {
                int gid = getGlobalId();

                if (band3[gid] == 0.0 || band4[gid] == 0.0) {
                    result[gid] = 0.0f;
                } else {
                    double r1 = log((band3[gid] / band4[gid]));
                    double r2 = r1 * r1;
                    double r3 = r2 * r1;
                    result[gid] = (float)(c0 + pow(10., c1 + (c2 * r1) + (c3 * r3) + (c4 * r3)));
                }
            }
        };
        Device device = Device.best();
        Range range = device.createRange(size);

        kernel.setExecutionMode(mode);
        kernel.execute(range);
        kernel.dispose();


        String fileName = "doc/CHL.he5";
        String dataSetName = "CHL dataset";
        HDFCreator hdfCreator = new HDFCreator(fileName, dataSetName, result);
        hdfCreator.HDFCreate();
    }
}