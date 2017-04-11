package mase.vrep;

import java.util.HashMap;
import mase.controllers.EncodableAgentController;
import mase.controllers.FixedValuesController;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

public class ControllerFactory {

    private static final HashMap<Integer, EncodableAgentController> CONTROLLERS = new HashMap<>();

    private static final BidiMap<Integer, Class<? extends EncodableAgentController>> CLASS_MAP = new DualHashBidiMap();

    static {
        CLASS_MAP.put(0, FixedValuesController.class);
    }

    public static void loadController(int handle, int type, float[] parameters) {
        if (parameters.length == 0) {
            throw new RuntimeException("Parameters array is empty!");
        }
        Class<? extends EncodableAgentController> clazz = getCorrespondingControllerClass(type);
        try {
            EncodableAgentController newInstance = clazz.newInstance();
            newInstance.decode(floatToDouble(parameters));
            CONTROLLERS.put(handle, newInstance);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static float[] controlStep(int handle, float[] inputs) {
        EncodableAgentController c = CONTROLLERS.get(handle);
        if (c != null) {
            return doubleToFloat(c.processInputs(floatToDouble(inputs)));
        } else {
            System.out.println("Controller is not yet loaded for this handle: " + handle);
            return null;
        }
    }

    protected static Class<? extends EncodableAgentController> getCorrespondingControllerClass(int type) {
        return CLASS_MAP.get(type);
    }

    protected static int getControllerClassType(Class<? extends EncodableAgentController> cl) {
        return CLASS_MAP.getKey(cl);
    }

    protected static float[] doubleToFloat(double[] d) {
        float[] res = new float[d.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = (float) d[i];
        }
        return res;
    }

    protected static double[] floatToDouble(float[] f) {
        double[] res = new double[f.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = (double) f[i];
        }
        return res;
    }
}
